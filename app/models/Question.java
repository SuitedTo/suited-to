package models;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import javax.persistence.Transient;

import logic.questions.scoring.QuestionScoreCalculator;
import newsfeed.listener.AcceptedQuestionNewsFeedListener;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import play.data.validation.MaxSize;
import play.data.validation.Required;
import play.db.jpa.JPABase;
import scheduler.TaskArgs;
import scheduler.TaskScheduler;
import tasks.CategoryCountTask;
import tasks.StreetCredUpdateTask;
import utils.XLSUtil;
import controllers.Security;
import enums.Difficulty;
import enums.QuestionStatus;
import enums.RoleValue;
import enums.Timing;


@Entity
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Question extends ModelBase implements Restrictable {

    private static final DecimalFormat df = new DecimalFormat("#.#");
   

    @Required
    @MaxSize(1000)//characters
    @Column(length = 65535)//bytes
    public String text;

    @Column(columnDefinition = "LONGTEXT")
    public String answers;

    @Column(columnDefinition = "LONGTEXT")
    public String prepAnswers;

    @Column(columnDefinition = "LONGTEXT")
    public String tips;

    @Enumerated(EnumType.STRING)
    public Timing time;

    @Enumerated(EnumType.STRING)
    public Difficulty difficulty;

    @ManyToOne(optional = false)
    public User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinTable(name = "QUESTION_CATEGORY",
            joinColumns = {@JoinColumn(name = "Question_id", unique = true)},
            inverseJoinColumns = {@JoinColumn(name = "categories_id")})
    @Fetch(FetchMode.SELECT)
    public Category category;

    /**
     * Indicates if the question should be excluded from use in Prepado,
     * regardless if the category is allowed in Prepado.
     */
    public boolean excludeFromPrep = false;

    /**
     * The category as it existed when this Question was loaded from the database.
     */
    @Transient
    public Category categoryAsLoaded;

    /**
     * Indicates that the question is active. When a question is
     * deleted it becomes inactive until it is either permanently
     * deleted or activated again.
     */
    public boolean active;

    /**
     * The active indicator as it existed when this Question was loaded from the database.
     */
    @Transient
    public boolean activeAsLoaded;

    /**
     * The status of the question indicating if the question is a private, quick, or is somewhere along the public
     * question review process
     */
    @Enumerated(EnumType.STRING)
    public QuestionStatus status;

    @Transient
    public QuestionStatus statusAsLoaded;

    @ManyToOne
    @Fetch(FetchMode.SELECT)
    public QuestionDuplication duplication;

    /**
     * Indicates that someone has said that this is an inappropriate question
     * facilitate completion of the calculation of street cred
     * includes an optional field to explain why the question has been marked
     * inappropriate
     */
    public boolean flaggedAsInappropriate;


    @Column(columnDefinition = "LONGTEXT")
    public String flaggedReason;

    /**
     * Gets the Time of this question.  If no time is present returns MEDIUM as a default
     * @return the Timing of the question or MEDIUM if none present.
     */
    public Timing getTime() {
        return time;
    }


    /***********************************************************************************
     * Rating and Score Related Fields                                                 *
     ***********************************************************************************/

    /**
     * Defined as the total of the rating (thumbs up/down) for the question + the total number of interviews.  Each
     * component may be multiplied by some factor to arrive at the standard score.
     */
    public int standardScore;
    
    /**
     * This is the component of the interview score that is not user based - it's
     * based on the number of interviews that this question is included in.
     */
    public int interviewPoints;

    /**
     * The simple sum of all the ratings (+1 or -1) of this question
     */
    public int totalRating;

    /**
     * The number of interviews that this question is included in
     */
    public int totalInterviews;


    /***********************************************************************************
     * Relationships to Child Entities                                                 *
     ***********************************************************************************/

    @OneToMany(mappedBy = "question", cascade = CascadeType.REMOVE)
    public List<InterviewQuestion> interviewQuestionList = new ArrayList<InterviewQuestion>();

    /**
     * List of company-specific notes for this Question.
     */
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    public List<QuestionNote> questionNotes = new ArrayList<QuestionNote>();

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL)
    @OrderBy("created DESC")
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    public List<QuestionWorkflow> workflows = new ArrayList<QuestionWorkflow>();

    @OneToMany(mappedBy = "question", cascade = CascadeType.REMOVE)
    public List<QuestionMetadata> metadata = new ArrayList<QuestionMetadata>();


    public Question() {
        this(null, null);
    }

    public Question(String text, User user) {

    	//do not set an initial status

        this.text = text;
        this.user = user;
        this.active = true;
    }


    /**
     * Create an initial workflow for this public question.  If the comment argument is null, the toString() value of
     * the assigned status will be used as the comment.
     * 
     * @param initialComment Optional
     */
    public void initPublicWorkflow(String initialComment){

    	QuestionStatus status;
    	
    	if(category == null){
        	status = QuestionStatus.AWAITING_CATEGORY;
        }else if( (difficulty == null) || (time == null) ||
        		(StringUtils.isBlank(answers) && 
                         StringUtils.isBlank(tips))) {
        	status = QuestionStatus.AWAITING_COMPLETION;
        } else {
            status = QuestionStatus.OUT_FOR_REVIEW;
            
            //     ????????????????????
            User connected = Security.connectedUser();
            this.user = (connected == null)?user:connected; // change question owner to the reviewer
        }
    	
    	String comment = initialComment != null ? initialComment : status.toString();
    	
    	updateStatus(user, status, comment);
    }


    /**
     * Mark this question as being comparable to the given one.
     *
     * @param otherQuestion
     */
    public void setDuplicateOf(Question otherQuestion) {

        if (otherQuestion == null) {
            throw new IllegalArgumentException();
        }

        QuestionDuplication duplication = this.duplication;
        if (duplication == null) {
            duplication = otherQuestion.duplication;
            if (duplication == null) {
                duplication = new QuestionDuplication().save();
                otherQuestion.duplication = duplication;
                otherQuestion.save();
            }
            this.duplication = duplication;
            save();
        } else {
            if (otherQuestion.duplication == null) {
                otherQuestion.duplication = this.duplication;
                otherQuestion.save();
            } else {
                //if the other question has a different set of duplicate questions
                //then we merge both sets into one
                if (otherQuestion.duplication.id != this.duplication.id) {
                    List<Question> otherDuplicates = otherQuestion.getDuplicates();
                    for (Question q : otherDuplicates) {
                        q.duplication = this.duplication;
                        q.save();
                    }
                }
            }
        }

    }
    
    /**
     * Determines whether or not the given question is a duplicate of this
     * one.
     * 
     * @param question
     * @return True if the given question is a duplicate of this
     * one.
     */
    public boolean duplicates(Question question){
    	if(question == null){
    		return false;
    	}
    	
    	return getDuplicates().contains(question);    	
    }

    /**
     * Find all questions that have been marked as comparable to this one.
     * Results will always include this question.
     */
    public List<Question> getDuplicates() {
        List<Question> results = new ArrayList<Question>();
        if (duplication == null) {
            results.add(this);
        } else {
            results = Question.find("byDuplication", duplication).fetch();
        }
        return results;
    }

    /**
     * Determine whether the given question appears to already be in the
     * database.This is an attempt to automatically detect duplicates.
     *
     * @param question
     * @return True if question appears to already exists
     */
    public static boolean looksFamiliar(Question question) {

        return (question != null) && (Question.find("byText", question.text).first() != null);
    }
    
    
    /**
     * Gets the meta data associated with this question and the
     * connected user.Create if necessary;
     * 
     * @return
     */
    public QuestionMetadata safeGetMetadata(){
    	User user = Security.connectedUser();
    	QuestionMetadata md = QuestionMetadata.findFirst("byQuestionAndUser", this, user);
    	if(md == null){
    		md = new QuestionMetadata(this,user).save();
    	}
    	return md;
    }

    /**
     * Get a single question note for the given Company. Returns null if none exists for the Company.
     *
     * @param company Company to find a note for.
     * @return A questionNote for the Company or null if none found.
     */
    public QuestionNote getQuestionNote(final Company company) {
        for (QuestionNote questionNote : questionNotes) {
            if (questionNote.company.equals(company)) {
                return questionNote;
            }
        }

        return null;
    }

    /**
     * Build a Question entity from the given spreadsheet question object and save it.
     *
     * @param xlsQuestion The spreadsheet question
     * @param user        The user associated with the question
     * @return
     */
    public static Question createFromXLSQuestion(XLSUtil.XLSQuestionIn xlsQuestion, User user) {
        if ((xlsQuestion != null) && (user != null)) {
            Question newQuestion = new Question(xlsQuestion.text, user);
            if (!looksFamiliar(newQuestion)) {
                if (xlsQuestion.tips != null) {
                    newQuestion.tips = xlsQuestion.tips.trim();
                }

                if (xlsQuestion.answers != null) {
                    newQuestion.answers = xlsQuestion.answers.trim();
                }

                if (xlsQuestion.time != null) {

                    try {
                        newQuestion.time = Timing.valueOf(xlsQuestion.time.toUpperCase().trim());
                    } catch (Exception e) {

                        if (newQuestion.time == null) {
                            int ceiling = Integer.MAX_VALUE;

                            Pattern ceilingPattern = Pattern.compile("(<)(\\s*)(\\d+)");
                            Matcher ceilingPatternMatcher = ceilingPattern.matcher(xlsQuestion.time);
                            if (ceilingPatternMatcher.find()) {
                                ceiling = new Integer(ceilingPatternMatcher.group(3));
                            } else {
                                Pattern rangePattern = Pattern.compile("(\\d+)(\\s*)(-)(\\s*)(\\d+)");
                                Matcher rangePatternMatcher = ceilingPattern.matcher(xlsQuestion.time);
                                if (rangePatternMatcher.find()) {
                                    ceiling = new Integer(ceilingPatternMatcher.group(5));
                                }
                            }

                            if (ceiling <= 1) {
                                newQuestion.time = Timing.SHORT;
                            } else if (ceiling <= 5) {
                                newQuestion.time = Timing.MEDIUM;
                            } else {
                                newQuestion.time = Timing.LONG;
                            }
                        }
                    }

                }

                if (xlsQuestion.difficulty != null) {
                    newQuestion.difficulty = Difficulty.valueOf(xlsQuestion.difficulty.toUpperCase().trim());
                }

                if (xlsQuestion.category != null) {

                    Category category = Category.find("byNameIlike", xlsQuestion.category.trim()).first();
                    if (category == null) {
                        category = new Category(xlsQuestion.category.trim());
                        final Company company = Security.connectedUser().company;
                        category.companyName = company != null ? company.name : null;
                        category.updateWorkflow(Category.CategoryStatus.NEW);
                        category.save();
                    }

                    newQuestion.category = category;
                }


                final String authorString = xlsQuestion.user;
                if (StringUtils.isNotEmpty(authorString)) {
                    User author = User.lookup(authorString);
                    if (author != null) {
                        newQuestion.user = author;
                    }
                }

                //if we weren't able to lookup the author, use the current user for the owner - we can't have questions
                //w/o owners
                if (newQuestion.user == null) {
                    newQuestion.user = user;
                }
                
                newQuestion.save();

                newQuestion.updateStatus(user, QuestionStatus.OUT_FOR_REVIEW, "Uploaded From Spreadsheet");

                return newQuestion;
            }
        }
        return null;
    }

    public XLSUtil.XLSQuestionOut toXLSQuestion() {

        final DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        XLSUtil.XLSQuestionOut xlsQuestion = new XLSUtil.XLSQuestionOut();

        try {
            xlsQuestion.id = String.valueOf(this.id);
            xlsQuestion.text = this.text;
            xlsQuestion.tips = this.tips;
            xlsQuestion.answers = this.answers;

            if (this.category == null) {
                xlsQuestion.category = "";
            } else {

                xlsQuestion.category = category.name;
            }

            xlsQuestion.difficulty = (this.difficulty == null) ? "" : this.difficulty.toString();
            xlsQuestion.time = (this.time == null) ? "" : this.time.toString();
            xlsQuestion.user = this.user.fullName;
            xlsQuestion.standardScore = String.valueOf(this.standardScore);
            xlsQuestion.creationDate = dateFormat.format(this.created);
            xlsQuestion.company = (user.company == null) ? "" : user.company.name;
            xlsQuestion.visibility = QuestionStatus.PRIVATE.equals(this.status) ? "Private" : "Public";
            xlsQuestion.status = (this.status == null) ? "" : this.status.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return xlsQuestion;
    }

    public Double getAverageRating() {
        List<QuestionMetadata> metadata = QuestionMetadata.find("byQuestion", this).fetch();
        if (metadata == null || metadata.size() == 0) {
            return null;
        }
        DescriptiveStatistics stats = new DescriptiveStatistics();
        for (QuestionMetadata questionMetadata : metadata) {
            if (questionMetadata.rating != null) {
                stats.addValue(questionMetadata.rating);
            }
        }

        return stats.getMean();
    }


    public void updateTotalRating() {
        this.totalRating = QuestionScoreCalculator.calculateTotalRating(this);
        save();
    }

    /**
     * A collection of all the Companies that have interviews that include this question.
     *
     * @return Set of Company objects.
     */
    public Set<Company> getCompanies() {
        Set<Company> companies = new HashSet<Company>();

        for (InterviewQuestion interviewQuestion : interviewQuestionList) {
            companies.add(interviewQuestion.interview.company);
        }

        /* Remove the null company caused by an APP_ADMIN creating an interview */
        companies.remove(null);

        return companies;
    }
    
    public int getDuration(){
    	if(time == null){
    		return Timing.MEDIUM.getDuration();
    	}else{
    		return time.getDuration();
    	}    	
    }

    public String getDescriptiveString() {
        Collection<String> values = new ArrayList<String>();

        values.add("question id: " + id);

        //rating, timing, difficulty
        if (totalRating != 0) {
            values.add("rating: " + df.format(totalRating));
        }

        if (this.time != null) {
            values.add("time: " + time.toString());
        }

        if (this.difficulty != null) {
            values.add("difficulty: " + difficulty.toString());
        }

        if (this.category != null) {
            values.add("category: " + category.name);
        }

        values.add("standard score: " + this.standardScore);

        return StringUtils.join(values, " | ");
    }

    /**
     * Sets the question's status field to the value of the status argument and does not save the question.
     * A new QuestionWorkflow for this question with the given data will be created and saved in a separate job.
     * Any appropriate Question notifications will be created and saved in a separate job.
     * 
     * @param user
     * @param status
     * @param comment
     */
    public void updateStatus(final User user, final QuestionStatus status, final String comment) {
    	
    	QuestionStatus statusFrom = null;
		QuestionWorkflow previousWorkflow = workflows.isEmpty() ? null : workflows.get(0);
		if (previousWorkflow != null) {
			statusFrom = previousWorkflow.status;
		}
		
    	this.eventContext.add(new QuestionWorkflow(this, user, statusFrom, status, comment));
    	
    	
    	this.status = status;
    }


    /**
     * The Date/Time that this public question was first submitted to the public pool
     *
     * @return Date of submission or null if the question is currently private
     */
    public Date getTimeOfSubmission() {

        if (!status.isPubliclyVisible()) {
            return null;
        }
        //try and figure out when the question was created from the workflow, but we need to handle cases where the
        //workflow does not exist
        Date workflowCreatedDate = null;
        for (QuestionWorkflow workflow : workflows) {
            if (workflow.status.equals(QuestionStatus.OUT_FOR_REVIEW)) {
                if (workflowCreatedDate == null || workflow.created.before(workflowCreatedDate)) {
                    workflowCreatedDate = workflow.created;
                }
            }
        }

        //use the date from the workflow if we can get it, otherwise just return the date that the question was created
        return workflowCreatedDate != null ? workflowCreatedDate : created;
    }

    public static void initMetrics() {

        List<Question> questions = Question.findAll();
        for (Question question : questions) {
        	initMetrics(question);
        }
    }

    /**
     * Recalculates metrics for the given question and saves the question.
     * @param question Question to recalculate metrics for
     */
    public static void initMetrics(Question question){
    	if(question != null){
            question.updateTotalInterviews();
            question.updateTotalRating();
            question.updateStandardScore();
            question.save();
    	}
    }

    public void updateTotalInterviews() {
        totalInterviews = QuestionScoreCalculator.calculateTotalInterviews(this);
        interviewPoints = QuestionScoreCalculator.calculateQuestionInterviewPoints(this);
        save();
    }

    public void updateStandardScore() {
        standardScore = QuestionScoreCalculator.calculateStandardScore(this);
        user.metrics.updateTotalQuestionScore();
        ((Question)merge()).save();
    }

    /**
     * Get the rating for the question associated with this object by the given user.
     *
     * @param user
     * @return rating
     */
    public int getRating(User user) {
        if ((user != null) && (this != null)) {
            QuestionMetadata metadata = QuestionMetadata.find("byQuestionAndUser", this, user).first();
            if (metadata != null) {
                return metadata.rating;
            }
        }
        return 0;
    }


    /**
     * Get the number of interviews created by the given company that
     * contain the question associated with this object.
     *
     * @return interviews
     */
    public int getInterviews(Company company) {
        int total = 0;
        if ((this != null) && (company != null)) {

            Set<Interview> interviews = company.getInterviews();
            for (Interview interview : interviews) {
                List<InterviewQuestion> iqList =
                        InterviewQuestion.find("byInterviewAndQuestion", interview, this).fetch();
                if (iqList != null) {
                    //size should only be 1 or 0
                    total += iqList.size();
                }
            }
        }
        return total;
    }


    /**
     * Get the rating for the question associated with this object by all users
     * who are associated with the given company.
     *
     * @param company
     * @return rating
     */
    public int getRating(Company company) {
        int total = 0;
        if ((this != null) && (company != null)) {
            List<User> users = company.getUsers();
            for (User user : users) {
                QuestionMetadata md = QuestionMetadata.find("byQuestionAndUser", this, user).first();
                if (md != null) {
                    total += md.rating;
                }
            }
        }
        return total;
    }


    /**
     * A list of the users who have participated in the review process for this Question
     * @return
     */
    public List<User> getReviewers(){
        List<User> reviewers = new ArrayList<User>();
        for (QuestionWorkflow workflow : workflows) {
            final QuestionStatus status = workflow.status;
            if((status.equals(QuestionStatus.RETURNED_TO_SUBMITTER) || status.equals(QuestionStatus.ACCEPTED))
                    && !reviewers.contains(workflow.user)){
                reviewers.add(workflow.user);
            }
        }
        return reviewers;
    }


    /**
     * Indicates that the given user has access to this question.
     * User has access to a question if:
     * 1. the question is a public question
     * 2. he is the owner of the question
     * 3. he is associated with the same company as the owner of the question
     * 4. he is an application admin
     * Note: there may be more restrictions involving what a user can actually do with a question but these checks will
     * verify if the user can view the question at all
     *
     * @param user User to check for access.
     * @return Whether the user has access to the question.
     */
    @Override
    public boolean hasAccess(final User user) {
        if (user == null) {
            return false;
        }

        if (user.hasRole(RoleValue.APP_ADMIN)) {
            return true;
        }

        if (this.active) {

            if ((status == null) || status.isPubliclyVisible()) {
                return true;
            }

            if (this.user.equals(user)) {
                return true;
            }

            if (user.company != null && user.company.equals(this.user.company)) {
                return true;
            }

        }


        return false;
    }

    /**
     * Determines if the user can access the question AND has the ability to edit the question contents.  The results
     * are based on question ownership as well as current workflow state of the question.
     * Questions can be edited by: the question creator, an application admin, or a company admin for the question
     * creator's company.
     * The users above can only edit a question when the question has not been used in an interview for another company.
     * IE. If the question creator works for company A, once someone in company B adds that question to an interview it
     * is no longer editable
     *
     * @param checkUser User to check for edit capabilities.
     * @return Whether the given user can edit the question.
     */
    public boolean canEdit(final User checkUser) {
        if (checkUser == null || !hasAccess(checkUser)) {
            return false;
        }

        boolean userQualified = false;
        if (checkUser.equals(user)) {
            userQualified = true;
        }

        if (checkUser.hasRole(RoleValue.APP_ADMIN)) {
            return true;
        }

        if (!userQualified && checkUser.hasRole(RoleValue.COMPANY_ADMIN)
                && checkUser.company != null && checkUser.company.equals(user.company)) {
            userQualified = true;
        }

        // reviewer for category
        if (!userQualified && status == QuestionStatus.AWAITING_COMPLETION &&
                checkUser.reviewCategories.contains(category)) {
            return true;
        }
        // super reviewer
        else if (checkUser.superReviewer && status == QuestionStatus.AWAITING_COMPLETION) {
            return true;
        }

        if (!userQualified) {
            return false;
        }


        if (interviewQuestionList.isEmpty()) {
            return true;
        }

        Company questionCompany = user.company;
        //handle the case where the user is not a member of a company, from previous check we already know there are
        //interviews associated with the question
        if (questionCompany == null) {
            return false;
        }

        //the first time we find an interview from a different company return false
        for (InterviewQuestion interviewQuestion : interviewQuestionList) {
            Interview interview = interviewQuestion.interview;
            if (!questionCompany.equals(interview.company)) {
                return false;
            }

        }

        return true;
    }

    /**
     * Determines is the given user is able to review the question.
     *
     * @param user User to check for review capabilites
     * @return whether the user can review the question
     */
    public boolean canReview(User user) {
        if (user == null || !hasAccess(user)) {
            return false;
        }

        //only questions that are "Out For Review" can be reviewed regardless of the user's privileges.
        if (!QuestionStatus.OUT_FOR_REVIEW.equals(this.status)) {
            return false;
        }

        //no private or quick questions
        if (!this.status.isPubliclyVisible()) {
            return false;
        }


        if (user.superReviewer) {
            return true;
        }

        //non superReviewers can't review their own questions
        if (user.equals(this.user)) {
            return false;
        }


        if (!user.reviewCategories.contains(category)) {
            return false;
        }

        return true;
    }

    public boolean canSeeHistory(User user) {
        if (user == null || !hasAccess(user)) {
            return false;
        }

        if (this.workflows.isEmpty()) {
            return false;
        }

        //suitedTo admin
        if (user.hasRole(RoleValue.APP_ADMIN)) {
            return true;
        }

        //owner
        if (this.user == user) {
            return true;
        }

        //can review
        if (this.canReview(user)) {
            return true;
        }

        //has existing workflow record (maybe they pushed back to submitter and someone else ended up accepting)
        return QuestionWorkflow.count("byQuestionAndUser", this, user) > 0;
    }

    /*
     * returns true if a question has been flagged and returned to the user for edits
     *  by an app_admin
     */

    public boolean flaggedAndReturned() {
        if (QuestionStatus.RETURNED_TO_SUBMITTER.equals(this.status) && flaggedAsInappropriate) {
            return true;
        }
        return false;
    }



    @PostPersist
    @PostUpdate
    private void doPostCreateOrUpdateActions() {
        TaskArgs args = new TaskArgs();
        args.add(StreetCredUpdateTask.QUESTION_ID_LIST_ARG, Arrays.asList(this.id));
        TaskScheduler.schedule(StreetCredUpdateTask.class, args, CronTrigger.getASAPTrigger());

        updateNecessaryCategoryCounts();
    }

    @PostRemove
    private void doPostDeleteActions(){
        TaskArgs args = new TaskArgs();
        args.add(StreetCredUpdateTask.USER_ID_ARG, this.user.id);
        TaskScheduler.schedule(StreetCredUpdateTask.class, args, CronTrigger.getASAPTrigger());

        updateNecessaryCategoryCounts();
    }

    private void updateNecessaryCategoryCounts(){
        if(this.category != null || categoryAsLoaded != null) {
            Set<Long> categoryIdSet = new LinkedHashSet<Long>();
            if(category != null) {
                categoryIdSet.add(category.id);
            }
            if(categoryAsLoaded != null){
                categoryIdSet.add(categoryAsLoaded.id);
            }
            TaskArgs categoryUpdateArgs = new TaskArgs();

            categoryUpdateArgs.add(CategoryCountTask.CATEGORY_ID_SET_ARG, categoryIdSet);
            TaskScheduler.schedule(CategoryCountTask.class, categoryUpdateArgs, CronTrigger.getASAPTrigger());
        }
    }

    /**
     * Deletes the question and handles any relationships that are not handled by cascading deletes.
     *
     * @param <T> Question to be deleted.
     * @return Question that was deleted.
     */
    @Override
    public <T extends JPABase> T delete() {
    	
    	if(!isDeletable()){
    		return (T) this;
    	}
    		
        //query notifications to load into session not sure why but deletes aren't working correctly with
        //cascades and orphanRemoval = true, could be a bug with hibernate
        QuestionNotification.getQuestionNotifications(this);
        List<QuestionMetadata> metadata = QuestionMetadata.find("byQuestion", this).fetch();
        for (QuestionMetadata questionMetadata : metadata) {
            questionMetadata.delete();
        }

        return super.delete();
    }


    /**
     * As soon as the Question is loaded copy category to the categoryAsLoaded transient field for
     * later comparison.
     */
    @PostLoad
    protected void populateAsLoadedFields() {
        addPropertyChangeListener(new AcceptedQuestionNewsFeedListener());
        listenersEnabled = true;
        categoryAsLoaded = category;
        statusAsLoaded = status;
        activeAsLoaded = active;
    }

    /**
     * Utility to handle question lookup. This is useful because the the Categories field on Question is not being
     * properly initialized prior to the invocation of any @PostLoad annotated methods even though the categories
     * relationship is defined as FetchMode.EAGER when using the findById method.
     *
     * @param id Id for which to lookup a Question
     * @return Question that matches the id
     */
    public static Question getQuestion(final Long id) {
    	//Enhancer now makes the appropriate adjustments - should
    	//be ok to call findById
        return Question.findById(id);
    }
}
