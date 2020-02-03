package models;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import play.Logger;
import scheduler.TaskArgs;
import scheduler.TaskScheduler;
import tasks.UpdateQuestionMetricsTask;
import utils.TimingUtil;
import enums.RoleValue;

@Entity
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Inheritance(strategy=InheritanceType.JOINED)
public class Interview extends ModelBase implements Restrictable {


    public String name;

    /**
     * This field exists only to support search
     * functionality
     */
    public String categories;

    /**
     * The user that created this interview
     */
    @ManyToOne
    public User user;

    @ManyToOne
    @Fetch(FetchMode.SELECT)
    public Company company;

    @OneToMany(mappedBy = "interview", cascade = CascadeType.ALL)
    @Fetch(FetchMode.SELECT)
    @OrderBy("sortOrder ASC")
    public List<InterviewQuestion> interviewQuestions = new ArrayList<InterviewQuestion>();
    
    /**
     * <p>Indicates that this Interview is active--which by default is true.
     * Inactive interviews can't be viewed or assigned to candidates.</p>
     */
    public boolean active;

    public Interview() {
        
    }
    
    public Interview(User user) {
        this();
        this.user = user;
        this.company = user.company;
        active = true;
    }
    
    /**
     * <p>Inactivates an interview.  Inactive interviews do no appear on the
     * interview list, but remain to be linked to by historical feedback.</p>
     */
    public void inactivate() {
        active = false;

        //Ultimately this might not be the right way to go, but for the 
        //moment there's no way to "reactivate" an interview, nor to view an
        //inactive interview's questions, so we clear these (unviewable) 
        //questions so that we don't get into trouble with questions 
        //"belonging" to an interview despite that interview being inactive.
        interviewQuestions.clear();
        
        save();
    }
    
    public Company getCompany() {
        return company;
    }

    public List<Question> getQuestions() {
        //todo: improve this query so that its joining through InterviewQuestion instead of looping over results
        List<Question> questions = new ArrayList<Question>(this.interviewQuestions.size());
        for (InterviewQuestion interviewQuestion : this.interviewQuestions) {
            questions.add(interviewQuestion.question);
        }

        return questions;
    }

    /**
     * Builds the total duration of the interview based on the time field of all included questions.  If a Question
     * does not have a time field set assumes a value of Timing.MEDIUM
     * @return the total Duration of the interview in minutes
     */
    public int getDuration() {
        int result = 0;
        for (InterviewQuestion interviewQuestion : this.interviewQuestions) {
            result += interviewQuestion.question.getDuration();
        }

        return result;
    }

    public static void init() {
        List<Interview> interviews = Interview.findAll();
        for (Interview interview : interviews) {
            interview.updateCategories();
            interview.save();
        }
    }

    /**
     * Update the categories field to support
     * searching.
     */
    public void updateCategories() {
        Set<String> categories = new TreeSet<String>();
        List<Question> questions = getQuestions();
        if (questions != null) {
            for (Question question : questions) {
                if (question.category != null) {
                    String category = question.category.name;
                    if (category != null) {
                        categories.add(category);
                    }
                }
            }
        }
        this.categories = StringUtils.join(categories, ", ");
    }

    /**
     *
     * The distinct list of Categories represented by this interview
     * @return a List of Categories, may be empty but should never be null
     */
    public List<Category> getCategoriesInUse(){
        List<Category> categories = new ArrayList<Category>();
        List<Question> questions = getQuestions();
        for (Question question : questions) {
            Category category = question.category;
            if(category != null && !categories.contains(category)){
                categories.add(category);
            }
        }

        return categories;

    }

    public void addQuestionToInterview(Question question, Integer sortOrder) {
        InterviewQuestion interviewQuestion = new InterviewQuestion();
        interviewQuestion.question = question;
        interviewQuestion.interview = this;
        
        
        if (this.interviewQuestions == null) {
            this.interviewQuestions = new ArrayList<InterviewQuestion>();
            interviewQuestion.sortOrder = 1;
            interviewQuestions.add(interviewQuestion);
        }else{
        	Iterator<InterviewQuestion> it = interviewQuestions.iterator();
            int i = 1;
            while(it.hasNext()){
            	InterviewQuestion next = it.next();
            	if(next.sortOrder == sortOrder){
            		i++;
            	}
            	next.sortOrder = i++; 
            }
            if((sortOrder == null) || (sortOrder < 1) ||
            		(sortOrder > interviewQuestions.size())){
            	interviewQuestion.sortOrder = interviewQuestions.size() + 1;
            	interviewQuestions.add(interviewQuestion);
            }else{
            	interviewQuestion.sortOrder = sortOrder;
            	interviewQuestions.add(sortOrder - 1, interviewQuestion);
            }
        }

        updateCategories();

    }

    /**
     * Adds the given interviewQuestion to the list of interview questions at the end of the list
     *
     * @param question
     */
    public void addQuestionToInterview(Question question) {
        addQuestionToInterview(question, null);
    }
    
    public int getQuestionCount() {
        return interviewQuestions != null ? interviewQuestions.size() : 0;
    }

    public void removeQuestionFromInterview(Question question) {
        if (this.interviewQuestions == null) {
            return;
        }
        boolean removed = false;
        for (Iterator<InterviewQuestion> iterator = interviewQuestions.iterator(); iterator.hasNext(); ) {
            InterviewQuestion interviewQuestion = iterator.next();

            if (interviewQuestion.question.equals(question)) {
                iterator.remove();
                removed = true;
            }

            //adjust the sort order of remaining interview questions
            if (removed) {
                interviewQuestion.sortOrder = interviewQuestion.sortOrder - 1;
            }
        }
        updateCategories();
    }

    public void reorderQuestion(Question question, Integer newOrder) {
        if ((question == null) ||
        		(interviewQuestions == null) ||
        		(newOrder > interviewQuestions.size()) ||
        		(newOrder < 1)) {
            return;
        }
        
        InterviewQuestion interviewQuestion = null;
        for(InterviewQuestion iq : interviewQuestions){
        	if(iq.question.equals(question)){
        		interviewQuestion = iq;
        		break;
        	}
        }
        if(interviewQuestions.remove(interviewQuestion)){
        	interviewQuestions.add(newOrder - 1, interviewQuestion);
        	Iterator<InterviewQuestion> it = interviewQuestions.iterator();
        	int i = 1;
        	while(it.hasNext()){
        		it.next().sortOrder = i++; 
        	}
        }
    }

    /**
     * Get the sortOrder of the given Question in the interview.  Returns null if the question is not found in the
     * interview
     * @param question
     * @return
     */
    public Integer getOrderOfQuestion(Question question){
        InterviewQuestion interviewQuestion = InterviewQuestion.find("byQuestionAndInterview", question, this).first();
        return interviewQuestion != null ? interviewQuestion.sortOrder : null;
    }

    public boolean canEdit(final User checkUser) {
        if (checkUser == null || !hasAccess(checkUser)) {
            return false;
        }

        return true;
    }

    public boolean hasAccess(final User user) {
        return (user != null) &&
                ((this.user.company != null && 
                        this.user.company.hasAccess(user)) ||
                    user.hasRole(RoleValue.APP_ADMIN));
    }

    @Override
    public boolean isDeletable() {
    	return true;
    }

    @Override
    public Interview save() {
    	final Interview interview = super.save();

    	List<Long> updatedQuestions = new ArrayList<Long>();
    	List<InterviewQuestion> questions = InterviewQuestion.find("byInterview", interview).fetch();

    	if(questions!= null){
    		for (final InterviewQuestion question : questions) {

    			updatedQuestions.add(question.question.id);

    			if (!interviewQuestions.contains(question)) {
    				question.delete();
    			} else {
    				question.save();
    				if(play.Play.runingInTestMode()){
    					QuestionMetadata md = question.question.safeGetMetadata();
    					md.lastActivity = new Date();
    					md.save();
    				}else{
    					new play.jobs.Job(){
    						public void doJob(){
    							QuestionMetadata md = question.question.safeGetMetadata();
    							md.lastActivity = new Date();
    							md.save();
    						}
    					}.now();
    				}

    			}    		
    		}

    		if(play.Play.runingInTestMode()){
    			TaskArgs args = new TaskArgs();
    			args.add("questionIds", updatedQuestions);
    			try {
    				TaskScheduler.runTask(
    						UpdateQuestionMetricsTask.class, 
    						args).get();
    			} catch (Exception e) {
    				e.printStackTrace();
    			}
    		}else{
    			TaskArgs args = new TaskArgs();
    			args.add("questionIds", updatedQuestions);
    			TaskScheduler.schedule(
    					UpdateQuestionMetricsTask.class, 
    					args, 
    					CronTrigger.getASAPTrigger());
    		}
    	}
    	return interview;
    }
    
    public static ActiveInterview getCandidateInterviewAssociationFromMagicID(
                String magicID) {
        
        ActiveInterview result;
        
        if (magicID == null || magicID.equals("-1")) {
            result = null;
        }
        else {
            magicID = magicID.trim();
            char descriminator = magicID.charAt(0);

            if (descriminator != 'a' && descriminator != 'c') {
                throw new IllegalArgumentException("Magic ID must begin with " +
                        "either 'a' or 'c', indicating an ActiveInterview or " +
                        "a CandidateInterview, respectively.");
            }

            long idPart = 
                    Long.parseLong(magicID.substring(1, magicID.length()));

            if (idPart == -1) {
                result = null;
            }
            else {
                if (descriminator != 'a') {
                    throw new RuntimeException("Magic ID must begin with 'a'.");
                }
                
                result = ActiveInterview.findById(idPart);
                
                if (result == null) {
                    throw new NoSuchElementException("No ActiveInterview " +
                            " with ID " + idPart + ".");
                }
            }
        }
        
        return result;
    }
    
    @Override
    public boolean equals(Object o) {
        boolean result = (o instanceof Interview);
        
        if (result) {
            Interview oAsInterview = (Interview) o;
            
            result = ObjectUtils.equals(oAsInterview.name, name) &&
                    ObjectUtils.equals(oAsInterview.company, company);
        }
        
        return result;
    }
    
    @Override
    public int hashCode() {
        int hash = 17;
        hash = 83 * hash + ObjectUtils.hashCode(name);
        hash = 83 * hash + ObjectUtils.hashCode(company);
        
        return hash;
    }
}
