package controllers;

import controllers.deadbolt.Deadbolt;
import controllers.deadbolt.RestrictedResource;
import controllers.deadbolt.RoleHolderPresent;
import data.binding.types.FilterBinder;
import data.binding.types.QueryBinder;
import enums.*;
import exceptions.CacheMiss;
import logic.questions.scoring.QuestionScoreCalculator;
import models.*;
import models.cache.InterviewCache;
import models.filter.question.*;
import models.query.QueryBase;
import models.query.QueryFilterListBinder;
import models.query.QueryResult;
import models.query.SearchQuery;
import models.query.question.AccessibleQuestions;
import models.query.question.QuestionQuery;
import notifiers.Mails;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.data.binding.As;
import play.data.validation.Required;
import play.data.validation.Valid;
import play.i18n.Messages;
import play.mvc.With;
import utils.SafeStringArrayList;
import utils.XLSUtil;
import utils.XLSUtil.XLSQuestionIn;
import utils.XLSUtil.XLSQuestionOut;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

/**
 * Controller for managing Questions including all basic CRUD operations
 */
@With(Deadbolt.class)
@RoleHolderPresent
@RestrictedResource(name="")
public class Questions extends ControllerBase {

	/**
	 * Navigate to basic question list page
	 * @see Questions#questionTableData for actually populating the data
	 */
	public static void list(){
        boolean isReviewer = Security.connectedUser().hasReviewCapability();
        String status = flash.get("status");
		render(status, isReviewer);
	}

    /**
     * Navigate to basic question review page
     */
    public static void review(){
        boolean isReviewer = Security.connectedUser().hasReviewCapability();
        String status = flash.get("status");
        render(status, isReviewer);
    }

	/**
	 * Used to show an existing question for editing (question owner) or display only, if no id is specified will show
	 * the page to create a new question the question entry page to create a new question.
	 * @param id optional Question id.  If present the id will be used to locate a question to be viewed or edited
	 * @param addToInterview True if this question will be added to the inprogress interview when it is saved.
	 */
	@RestrictedResource(name = {"models.Question"}, staticFallback = true)
	public static void show(final Long id, final boolean addToInterview,
                final boolean addToInterviewHasUnsavedChanges){

        if (id != null) {
            Question question = Question.getQuestion(id);
            prepForViewOrShow(question);
            if (question.canEdit(Security.connectedUser())) {
                render(addToInterview, addToInterviewHasUnsavedChanges);
            } else {
                render("@view");
            }
        }

        boolean isReviewer = Security.connectedUser().hasReviewCapability();

        render(addToInterview, addToInterviewHasUnsavedChanges, isReviewer);
	}

	/**
	 * Used to show the question in view only mode.  Differs from show() in that this will always take the user directly
	 * to the view page while the show action will route the user to edit or view pages depending on question
     * editability
	 * @param id required primary key of the Question entity to load into view
	 */
	@RestrictedResource(name = {"models.Question"}, staticFallback = true)
	public static void view(@Required final Long id) {
		Question question = Question.getQuestion(id);
		if(question != null) {
			prepForViewOrShow(question);
			List<Question> duplicates = question.getDuplicates();
			duplicates.remove(question);
			renderArgs.put("duplicates", duplicates);
		}

		render();
	}
	
	/**
	 * Render the duplicateSelection view so the user can select a duplicate
	 * for the question with the given id.
	 * 
	 * @param id Question id
	 */
	@RestrictedResource(name = {"models.Question"}, staticFallback = true)
	public static void findDuplicate(final Long id) {
		Question question = Question.getQuestion(id);
		
		if(question != null) {
			renderArgs.put("question", question);
			renderArgs.put("duplicates", question.getDuplicates());
		}
        render("@duplicateSelection");
	}
	
	/**
	 * Mark the given question ids as duplicates of each other.
	 * 
	 * @param questionId One question id.
	 * @param duplicateId The other question id.
	 */
	@RestrictedResource(name = {"models.Question"}, staticFallback = true)
	public static void setDuplicate(@Required final Long questionId, @Required final Long duplicateId) {
		Question question = Question.getQuestion(questionId);
		Question duplicate = Question.getQuestion(duplicateId);
		if((question != null) && (duplicate != null)) {
			duplicate.setDuplicateOf(question);
		}
		view(duplicateId);
	}
	
	/**
	 * Remove the question with the given id from it's duplication group.
	 * 
	 * @param id Question id
	 */
	@RestrictedResource(name = {"models.Question"}, staticFallback = true)
	public static void unSetDuplicate(@Required final Long id) {
		Question question = Question.getQuestion(id);
		if(question != null) {
			question.duplication = null;
			question.save();
		}
	}

	/**
	 * Upload questions from a spreadsheet.
	 * @param spreadsheet required The spreadsheet that contains questions.
	 */
	@RestrictedResource(name = {"models.Question"}, staticFallback = true)
	public static void uploadSpreadsheet(@Required File spreadsheet) {
		User user = Security.connectedUser();
		try {
			InputStream is = new FileInputStream(spreadsheet);
			List<XLSQuestionIn> xlsQuestions = XLSUtil.importQuestions(is);
			for (XLSQuestionIn xlsQuestion : xlsQuestions){
				Question.createFromXLSQuestion(xlsQuestion, user);
			}
		} catch (Exception e) {
			flash.error("questions.upload.error");
			Logger.error("Unable to upload spreadsheet. %s", e.getMessage());
		}
    	list();
    }
	
	/**
	 * Download questions to a spreadsheet.
	 */
	@RestrictedResource(name = {"models.Question"}, staticFallback = true)
	public static void downloadSpreadsheet() {
		try {
			List<Question> questions = new AccessibleQuestions().executeQuery().getList();
			List<XLSQuestionOut> xlsQuestions = new ArrayList<XLSQuestionOut>();
			for (Question question : questions){
				xlsQuestions.add(question.toXLSQuestion());
			}
			response.setHeader("Content-Type","application/vnd.ms-excel");
			response.setHeader("Content-Disposition","attachment; filename=questions.xls");
			XLSUtil.exportQuestions(xlsQuestions,response.out);
		} catch (Exception e) {
			flash.error("questions.download.error");
			Logger.error("Unable to download spreadsheet. %s", e.getMessage());
		}
    }


    /**
     * Performs validations and saves the Question.
     * @param question Question to be saved.
     * @param category category name that will be used to assign a category to the question.  If no category exists
     *                   matching the name then a new one will be created.
     * @param submitComment an optional comment that can be passed in when resubmitting the question for review
     * @param addToInterview true if the question is to be added to the in progress interview.
     * @param companyNotes company-specific notes for this question
     * @param privateQuestion indicates that this is Question's status should be set to PRIVATE
     * @param saveOtr indicates that this question is being saved "Off the Record" no workflow or change in ownership should occur
     */
    @RestrictedResource(name = {"models.Question"}, staticFallback = true)
    public static void save(@Valid Question question, final String category, final String submitComment,
                            final boolean addToInterview, final String companyNotes, final boolean privateQuestion, final boolean saveOtr) {

        final User user = Security.connectedUser();

        //only admins can save a question "Off the Record"
        if((saveOtr && !user.hasRole(RoleValue.APP_ADMIN)) || (saveOtr && !question.hasBeenSaved())){
            unauthorized();
        }

        if(question.user == null) {
            question.user = user;
        }

        List<Category> categoryList = Categories.categoriesFromStandardRequestParam(category);
        question.category = !categoryList.isEmpty() ? categoryList.get(0) : null;

        //perform custom validations for public questions
        //App admin can save questions with only text
        if(!privateQuestion && !user.hasRole(RoleValue.APP_ADMIN)) {
            //At least 1 tag/category has been entered
            Category questionCategory = question.category;
            validation.required(questionCategory);

            //Difficulty has been set
            Difficulty difficulty = question.difficulty;
            validation.required(difficulty);

            //Timing has been set
            Timing time = question.time;
            validation.required(time);

            //Text is present in one of the "possible answers" or "tips" fields
            String possibleAnswers = question.answers;
            String tips = question.tips;
            
            if (StringUtils.isBlank(possibleAnswers) && 
                    StringUtils.isBlank(tips)) {
                ControllerBase.validationError("");
            }
        } else if (question.statusAsLoaded != null && question.statusAsLoaded.isPubliclyVisible() &&
                !AccountLimitedAction.CREATE_PRIVATE_QUESTION.canPerform((AccountHolder) user)) {

            validationError(Messages.get("limits.questions.cannot_create"));
        }


        Company company = user.company;
        if(company != null){
            QuestionNote note = question.getQuestionNote(company);
            if(note == null) {
                note = new QuestionNote();
                note.company = company;
                note.question = question;
                question.questionNotes.add(note);
            }
            note.text = companyNotes;
        }

        //create any new categories
        if (question.category != null && !question.category.hasBeenSaved()) {
            question.category.updateWorkflow(Category.CategoryStatus.NEW);
            question.category.save();
        }

        QuestionStatus previousStatus = question.status;
        if(!saveOtr){
            //create the workflow record indicating that the question's review status has changed
            if(privateQuestion) {
                question.status = QuestionStatus.PRIVATE;
            }else if (previousStatus != null && previousStatus.equals(QuestionStatus.QUICK)){
                question.status = QuestionStatus.QUICK;
            }else{
                question.initPublicWorkflow(submitComment);
            }
        }
        
      //if check to make sure the Question is valid before saving, return back to the show page to correct any errors
        if (validation.hasErrors()) {
            prepForViewOrShow(question);
            render("@show");
        }
        
        question.save();
        
        user.metrics.updateNumberOfSubmittedQuestions();
        user.metrics.save();


        QuestionStatus questionStatus = question.status;
        
        String status;
        if (QuestionStatus.OUT_FOR_REVIEW.equals(questionStatus) && QuestionStatus.RETURNED_TO_SUBMITTER.equals(previousStatus)) {
            status = "resubmit";
        } else if (QuestionStatus.OUT_FOR_REVIEW.equals(questionStatus)) {
            status = "review";
        } else {
            status = "saved";
        }
        
        if(addToInterview) {
        	Interviews.addQuestion(question.id);
        }

        flash.put("status", status);
        list();
    }
    
    @RestrictedResource(name = {"models.Question"}, staticFallback = true)
    public static void saveReview(final Long id, final String acceptComment, final String rejectComment) {
        String status = null;
        Question question = Question.getQuestion(id);
        final User user = Security.connectedUser();
        
        if(params._contains("acceptButton")) {
        	
            status = "accepted";
            
            question.updateStatus(user, QuestionStatus.ACCEPTED, acceptComment);
            
        } else if(params._contains("rejectButton")) {
        	
            status = "rejected";
            
        	question.updateStatus(user, QuestionStatus.RETURNED_TO_SUBMITTER, rejectComment);
        	
        }
        
        question.save();
        
        
        UserMetrics reviewerMetrics = UserMetrics.find("byUser", Security.connectedUser()).first();
        if(reviewerMetrics != null){
        	reviewerMetrics.incrementNumberOfQuestionsReviewed();
        	reviewerMetrics.save();
        }

        boolean isReviewer = Security.connectedUser().hasReviewCapability();
        render("@list", status, isReviewer);
    }

    /*
    *Added functionality to switch questions to and from a flagged state
    * only app_admin can change a question back to an appropriate unflagged state
    */
    @RestrictedResource(name = {"models.Question"}, staticFallback = true)
    public static void flagAsInappropriate(final Long id, String flaggedText){

        Question question = Question.getQuestion(id);
        if(params._contains("acceptFlaggedButton")) {
        question.flaggedReason = flaggedText;
        question.flaggedAsInappropriate = true;
        question.save();
        Mails.flaggedAsInappropriate(question.user, question);
        view(id);
        }

    }

    @RestrictedResource(name = {"models.Question"}, staticFallback = true)
    public static void setAsAppropriate(final Long id) {
        Question question = Question.getQuestion(id);
        question.flaggedAsInappropriate = false;
        question.save();
        view(id);
    }


    /**
     * Saves either an existing or new QuestionNote object for the current user's company and the question matching the
     * id parameter.
     * @param id PrimaryKey of a question to associate with the QuestionNote.
     * @param text Text to use for the QuestionNote.
     */
    @RestrictedResource(name = {"models.Question"}, staticFallback = true)
    public static void saveQuestionComment(Long id, String text) {
    	Question question = Question.getQuestion(id);

    	if(question != null){
    		Company company = Security.connectedUser().company;
    		QuestionNote note = null;
    		if (company != null) {
    			note = QuestionNote.find("byCompanyAndQuestion", company, question).first();
    			if (note == null) {
    				note = new QuestionNote();
    				note.company = company;
    				note.question = question;
    				question.questionNotes.add(note);
    				question.save();
    			}
    			note.text = text;
    			note.save();
    		}
    	}
    }



    /**
     * Prepares the necessary request parameters for the "view" or "show" views which can be accessed directly or be
     * the target for redirection after a failed validation on save
     * @param question Question to use to populate values
     */
    private static void prepForViewOrShow(final Question question) {
        question.getTimeOfSubmission(); //run this before going to the view so that the appropriate lazy collections will be pre-fetched
        renderArgs.put("question", question);
        //Haven't dug too deeply but there's an apparent bug in the template
        //renderer. It's picking up a question from the duplicates list to bind
        //in the rate function. Search for bug in the view.html to see where
        //this is happening.
        renderArgs.put("bug", question);
        Integer userRating = null;
        final User user = Security.connectedUser();
        if(question.hasBeenSaved()) {
            userRating = question.getRating(user);
        }
        renderArgs.put("userRating", userRating);

        renderArgs.put("resubmit", QuestionStatus.RETURNED_TO_SUBMITTER.equals(question.status));

        Company company = user.company;
        QuestionNote companyNotes = question.getQuestionNote(company);
        renderArgs.put("companyNotes", companyNotes);

        renderArgs.put("flaggedReason", question.flaggedReason);

    }

    public static void cancelEdit(boolean addToInterview, boolean addToInterviewHasUnsavedChanges){
        if (addToInterview) {
            Interviews.show(null, false, addToInterviewHasUnsavedChanges);
        }
        else {
            list();
        }
    }
    
    /**
     * Delete the question with the given id from the database.
     * 
     * @param questionId
     */
    @RestrictedResource(name = {"models.Question"}, staticFallback = true)
    public static void deleteForever(final Long questionId) {

        Question question = Question.getQuestion(questionId);
        /*reload the question directly from the database in order to ensure that all related entities are loaded correctly.
        We saw the issue described in https://www.pivotaltracker.com/story/show/43080773 due to newly created workflows
        not always being present on the cached Question object which causes problems when cascading deletes*/
        question = question.refresh();
        question.delete();
        list();
    }
    
    /**
     * Deactivate the question associated with the given id and
     * remove it from all interviews.
     * 
     * @param questionId
     */
    @RestrictedResource(name = {"models.Question"}, staticFallback = true)
    public static void deepDelete(final Long questionId) {
    	Question question = Question.getQuestion(questionId);
        question = question.refresh();
        if(question != null){
        	Iterator<InterviewQuestion> it = question.interviewQuestionList.iterator();
        	while(it.hasNext()){
        		InterviewQuestion iq = it.next();
        		if(iq.question.equals(question)){
        			iq.delete();
        			it.remove();
        		}
        	}
        	question.active = false;
        	question.save();
        }
        
        list();
    }

    /**
     * Deactivate the question associated with the given id.
     * 
     * @param questionId
     */
    @RestrictedResource(name = {"models.Question"}, staticFallback = true)
    public static void delete(final Long questionId){
        Question question = Question.getQuestion(questionId);
        if(question != null){
        	question.active = false;
        	question.save();
        }
        
        list();
    }

    @RestrictedResource(name = {"models.Question"}, staticFallback = true)
    public static void rateQuestion(final Long id, final Integer rating) {
        Question question = Question.getQuestion(id);
        User user = Security.connectedUser();

        QuestionMetadata metadata = QuestionMetadata.find("byQuestionAndUser", question, user).first();

        if (metadata == null) {
            metadata = new QuestionMetadata(question, user);
        }
        
        boolean alreadyRated = metadata.rating != 0;

        /* addresses a security issue which allows the user to pass in any value they'd like into this method via
        Javascript. This will prevent them from setting ratings other than 0, 1, -1 regardless of what they pass in.
        A more elegant solution might be to replace the Javascript calls with distinct functions to rate up or rate
        down but this is quicker and easier under a time crunch.*/
        int adjustedRating;
        if(rating > 0){
            adjustedRating = 1;
        } else {
            adjustedRating = -1;
        }
        
        metadata.rating = adjustedRating;
        
        metadata.lastActivity = new Date();

        metadata.save();
        
        if(!alreadyRated){
        	new IncrementUserRateCount(user.id).now();
        }

        if(!play.Play.runingInTestMode()){
        	//I don't know why this doesn't work in test mode but the StandardScoreTest
        	//fails if I await instead of get directly. I think it should work with await!
        	question = await(new AfterQuestionRated(question.id).now());
        } else {
        	try {
        		new AfterQuestionRated(question.id).now().get();
        	} catch (Exception e) {
        		e.printStackTrace();
        	}
        }
        
        ((Question)question.merge()).refresh();

        renderText(question.standardScore);
    }

    
 	/**
	 * Build the basic list of questions
	 * not specific to any <code>Interview</code> all parameters are optional
	 * @param sSearch search term to use
	 * @param sEcho used by Datatables plugin just echo back in the response
	 * @param iSortCol_0 the column to sort by (optional)
	 * @param sSortDir_0 the direction 'ASC' or 'DESC' to sort by
	 * @param iDisplayStart equivalent to setFirstResult() in JPA
	 * @param iDisplayLength equivalent to setMaxResults() in JPA
	 * @param query Base query
	 * @param filters Question filters
	 */
	public static void questionTableData(String sSearch,
			Long sEcho,  
			Integer iSortCol_0,
			String sSortDir_0, 
			Integer iDisplayStart, 
			Integer iDisplayLength,
                        Boolean bSearchable_6,
			@As(binder=QueryBinder.class) QuestionQuery query,
			@As(value=",", binder=FilterBinder.class) List<QuestionFilter> filters){

		QueryBase toExecute = (query == null)?QuestionQuery.getDefaultQuery():query;
		
		if(filters != null){
			toExecute = new QueryFilterListBinder<Question,QuestionFilter>(toExecute,filters);			
		}
		
		if((sSearch != null) && (sSearch.length() > 0)){			
			
			SearchQuery<Question> search = new SearchQuery<Question>(toExecute, sSearch);
			
			search.init(iSortCol_0, sSortDir_0, iDisplayStart, iDisplayLength);
			
			QueryResult<Question> searchResults = search
				.addFilter(new ByText(), false)
				.addFilter(new ById(), false)
				.addFilter(new ByStatus(), true)
				.addFilter(new ByDifficulty(), false)
				.addFilter(new ByTime(), false)
				.addFilter(new ByCategoryName(), false)
				.executeQuery();
			
			renderJSON(new DataTablesSource(
                    searchResults.getList(), searchResults.getTotal(), sEcho).toJson());
		}else{
			
			toExecute.init(iSortCol_0, sSortDir_0, iDisplayStart, iDisplayLength);
			
			QueryResult<Question> result = toExecute.executeQuery();

			renderJSON(new DataTablesSource(result.getList(), result.getTotal(), sEcho).toJson());
		}		
                	
	}
    
    public static void interviewQuestionTableData(Long interviewId, Long sEcho){
        Interview interview;
		try {
			interview = InterviewCache.getInProgressInterview();
		} catch (CacheMiss e) {
			interview = Interview.findById(interviewId);
		}
        List<InterviewQuestion> interviewQuestions = interview.interviewQuestions;
        

        
        renderJSON(new DataTablesSource(interviewQuestions, null, sEcho).toJson());
    }
    
    public static void questionsForInterviewInclusionTableData(String sSearch,
                                                               Long sEcho,
                                                               Integer iSortingCols,
                                                               Integer iSortCol_0,
                                                               String sSortDir_0,
                                                               Integer iDisplayStart,
                                                               Integer iDisplayLength){
    	
    	QueryBase toExecute = new AccessibleQuestions();
    	
		try {
			Interview inProgress = InterviewCache.getInProgressInterview();
			ById filter = new ById();
    		for(Question q : inProgress.getQuestions()){
    			filter.exclude(String.valueOf(q.id));

    		}
    		
    		List<QuestionFilter> filters = new ArrayList<QuestionFilter>();
    		filters.add(filter);
    		toExecute = new QueryFilterListBinder<Question,QuestionFilter>(toExecute,filters);		
		} catch (CacheMiss e) {
    	}
		
		if((sSearch != null) && (sSearch.length() > 0)){			
			
			SearchQuery<Question> search = new SearchQuery<Question>(toExecute, sSearch);
			
			search.init(iSortCol_0, sSortDir_0, iDisplayStart, iDisplayLength);
			
			QueryResult<Question> searchResults = search
				.addFilter(new ByText(), false)
				.addFilter(new ById(), false)
				.addFilter(new ByStatus(), true)
				.addFilter(new ByDifficulty(), false)
				.addFilter(new ByTime(), false)
				.addFilter(new ByCategoryName(), false)
				.executeQuery();
			
			renderJSON(new DataTablesSource(searchResults.getList(), searchResults.getTotal(), sEcho).toJson());
		}else{
			
			toExecute.init(iSortCol_0, sSortDir_0, iDisplayStart, iDisplayLength);
			
			QueryResult<Question> result = toExecute.executeQuery();
			

			renderJSON(new DataTablesSource(result.getList(), result.getTotal(), sEcho).toJson());
		}		
    }

    /**
     * To handle the custom conversion of Questions to specific format required by the jQuery DataTables plugin
     */
    private static class DataTablesSource extends utils.DataTablesSource {

        DataTablesSource(final Collection objects,
                         Long iTotalDisplayRecords,
                         Long sEcho) {

            super(iTotalDisplayRecords, sEcho);
            //for application admins show the total question count in the database, this is helpful for debugging
            //purposes.
            if(Security.connectedUser().hasRole(RoleValue.APP_ADMIN)){
                this.iTotalRecords = Question.count();
            }

            for (Object object : objects) {
                Question question;
                InterviewQuestion interviewQuestion = null;
                if(object instanceof Question){
                    question = (Question)object;
                } else if(object instanceof InterviewQuestion){
                    interviewQuestion = (InterviewQuestion)object;
                    question = (interviewQuestion).question;
                } else {
                    throw new IllegalArgumentException(this.getClass() + " can only be created with a list of " +
                            "Questions or InterviewQuestions");
                }

                SafeStringArrayList questionData = new SafeStringArrayList();
                
                if(interviewQuestion != null) {
                	//Hack to match remove column.
                	questionData.add("");
                }
                
                questionData.add(question.id);
                
                if(interviewQuestion == null) {
                	questionData.add(Messages.get(question.status));
                }
                
                questionData.add(question.text);
                questionData.add(Messages.get(question.time));
                questionData.add(Messages.get(question.difficulty));

                
                int points = QuestionScoreCalculator.calculateStandardScore(question);
                questionData.add(points);
                
                String categoryLabel = (question.category == null)?"":question.category.name +
                		" (" + question.category.questionCount + ")";
                questionData.add(categoryLabel);
                
                if(interviewQuestion != null) {
                	questionData.add(interviewQuestion.sortOrder);
                }
                aaData.add(questionData);
            }
        }
    }
    
    private static class IncrementUserRateCount extends play.jobs.Job {
        private long userId;
        
        public IncrementUserRateCount(long userId){
        	this.userId = userId;
        }
        
        @Override
        public void doJob() {
            User user = User.findById(userId);
            
            user.metrics.incrementNumberOfQuestionsRated();
            user.metrics.save();
        }
    }
    
    /**
     * Update any data that is derived from a question's rating.
     *
     */
    private static class AfterQuestionRated extends play.jobs.Job<Question> {
    	private long questionId;
    	
    	public AfterQuestionRated(long questionId){
    		this.questionId = questionId;
    	}
        
        @Override
        public Question doJobWithResult() {
            Question question = Question.findById(questionId);
            question.updateStandardScore();
            question.updateTotalRating();
            QuestionMetadata.updateRatingPoints(question);
            return question;

        }
    }
}
