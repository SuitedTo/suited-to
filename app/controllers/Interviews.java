package controllers;

import com.google.gson.*;
import controllers.deadbolt.Deadbolt;
import controllers.deadbolt.RestrictedResource;
import controllers.deadbolt.RoleHolderPresent;
import data.binding.types.ListBinder;
import enums.AccessGroup;
import enums.AccountLimitedAction;
import enums.ActiveInterviewState;
import enums.Difficulty;
import exceptions.CacheMiss;
import logic.interviews.InterviewBuilder;
import logic.interviews.InterviewRequest;
import logic.interviews.InterviewRequest.InterviewCategory;
import models.*;
import models.cache.CachedEntity;
import models.cache.InterviewCache;
import models.cache.InterviewCache.*;
import models.filter.interview.ByCategoryName;
import models.filter.interview.ById;
import models.filter.interview.ByName;
import models.query.QueryResult;
import models.query.SearchQuery;
import models.query.interview.AccessibleInterviews;
import org.hibernate.Hibernate;
import play.data.binding.As;
import play.data.validation.Min;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.db.jpa.JPA;
import play.i18n.Messages;
import play.mvc.With;
import utils.SafeStringArrayList;

import java.lang.reflect.Type;
import java.util.*;

import static models.cache.InterviewCache.*;


@With(Deadbolt.class)
@RoleHolderPresent
@RestrictedResource(name="")
public class Interviews extends ControllerBase {

    private static final Gson EVENT_SERIALIZER;
    private static final Gson FEEDBACK_SERIALIZER;
    
    static {
        GsonBuilder b = new GsonBuilder();
        b.registerTypeHierarchyAdapter(ActiveInterviewEvent.class, 
                new EventSerializer());
        b.registerTypeAdapter(User.class, new EventUserSerializer());
        
        EVENT_SERIALIZER = b.create();

        b = new GsonBuilder();
        b.registerTypeAdapter(InterviewQuestion.class, new InterviewQuestionFeedbackSerializer());

        FEEDBACK_SERIALIZER = b.create();
    }
    
    //Note that this must match what this variable is called in 
    //views/Interview/show.html
    private static final String LOADED_WITH_UNSAVED_CHANGES = 
            "loadedWithUnsavedChanges";

    public static void live(Long id){
        ActiveInterview interview = ActiveInterview.findById(id);
        render(interview);
    }

    /**
     * Shows the list of existing interviews
     */
    public static void list(){
    	removeInProgressInterviewFromCache();
        render();
    }

    /**
     * Shows the list of existing candidate interviews
     */
    public static void candidates(){
        removeInProgressInterviewFromCache();
        render();
    }

    /**
     * Removes any inProgress interview from the Cache
     */
    public static void reset(){
        removeInProgressInterviewFromCache();
    }
    
    /**
     * <p>Show the Interview create/edit page, will use the inProgressInterview from the cache if present otherwise will
     * try to look up an existing interview using the id parameter</p>
     * 
     * <p><strong>N.B.</strong> Note that 
     * <code>candidateName&nbsp;==&nbsp;null</code>
     * <strong>iff</strong> <code>dateString&nbsp;==&nbsp;null</code>.</p>
     * 
     * @param id Interview Id - will only be used for lookup if 
     *              inProgressInterview is not present in Cache.
     * @param fresh If true, clears the cache before anything else.  Otherwise,
     *              leaves the cache as is.
     * @param loadedWithUnsavedChanges If we're in the middle of a workflow that
     *            has modified the current (working) interview, this should be
     *            set to <code>true</code>, otherwise <code>false</code>.
     */
    @RestrictedResource(name = {"models.Interview"}, staticFallback = true)
    public static void show(Long id, boolean fresh,
            boolean loadedWithUnsavedChanges){
        Interview interview = null;
        
        if (fresh) {
            removeInProgressInterviewFromCache();
        }
        else {
            try {
                interview = getInProgressInterview();
            } catch (CacheMiss e) {
                //checking for null elsewhere
            }
        }
        
        if(id != null){
        	
        	if(interview == null){
        		interview = interview.findById(id);
        		createInProgressInterview(interview);
        	}else{
        		if(!interview.id.equals(id)){
        			interview = Interview.findById(id);
        			createInProgressInterview(interview);
        		}
        	}
        }else{
        	if(interview == null){
                    
                    User user = Security.connectedUser();
                    if (!AccountLimitedAction.CREATE_INTERVIEW.canPerform(
                            (AccountHolder)user)) {
                        
                        validationError(Messages.get(
                                "limits.interviews.cannot_create"));
                        Validation.keep();
                        list();
                    }
                    
                    interview = createInProgressInterview();
        	}
        }

        /*when scheduling an ActiveInterview we want to keep the section on candidates as saving or cancelling will
        return back to the candidate page */
        if(interview instanceof ActiveInterview) {
            activateSection(Candidates.class.getName());
        }
        
        if (interview.active) {        
            String existingCategories = Categories.getCategoryNamesJSON();

            render(interview, existingCategories, loadedWithUnsavedChanges);
        }
        else {
            validationError("That interview has been deleted.");
            Validation.keep();
            list();
        }
    }


    
    /**
     * Save the in progress interview to cache
     * 
     * @param interviewName The name of the interview
     */
    public static void saveInProgressInterview(@Required String interviewName, @As("MM/dd/yyyy") Date anticipatedDate, Long interviewerId, String interviewNotes){
    	Interview interview = safeGetInProgressInterview();

        interview.name = interviewName;
        
        if(interview instanceof ActiveInterview){
        	if(anticipatedDate != null){
        		((ActiveInterview)interview).anticipatedDate = anticipatedDate;
        	}
        	if(interviewerId != null){
        		User interviewer = User.findById(interviewerId);
        		((ActiveInterview)interview).interviewer = interviewer;
        	}

            ((ActiveInterview)interview).notes = interviewNotes;
            
            setInProgressInterview(interview,
            		new InProgressInterviewContext(((ActiveInterview) interview).interviewee));
        }else{
        	setInProgressInterview(interview);
        }
    }


    
    @RestrictedResource(name = {"models.Interview"}, staticFallback = true)
    public static void getActiveInterviewHistory(@Required Long id) {
        
        Map<String, Object> result = new HashMap<String, Object>();
        
        ActiveInterview i = ActiveInterview.findById(id);
        
        List<ActiveInterviewEvent> events = 
                ActiveInterviewEvent.find("byActiveInterview", i).fetch();
        
        result.put("result", events);
        
        renderJSON(EVENT_SERIALIZER.toJson(result));
    }
    
    @RestrictedResource(name = {"models.Candidate"}, staticFallback = true)
    public static void createActiveInterview(@Required Long id) {
        Candidate candidate = Candidate.findById(id);

        final User user = Security.connectedUser();
        ActiveInterview i = new ActiveInterview(user, candidate);
        i.interviewer = user; //default the interviewer to the connected user
        
        i.name = ActiveInterview.getNextName(candidate, user.company);
        
        setInProgressInterview(i,
        		new InProgressInterviewContext(candidate));
        
        show(null, false, false);
    }

    /**
     * Saves the current in progress interview. Parameters are explicitly defined as opposed to using automatic JPA
     * binding because the Interview is retrieved from the cache on this method.  Its done this way because of the need
     * to maintain the state of an interview across multiple requests without persisting.
     * @param interviewName The name of the interview
     * @param anticipatedDate for ActiveInterviews only, the date the interview is scheduled to take place may be null
     * @param interviewerId for ActiveInterviews only, key for the interviewer to be assigned to this interview
     */
    public static void save(@Required String interviewName, @As("MM/dd/yyyy")Date anticipatedDate, Long interviewerId,
    		String addingQuestions, String interviewNotes){

    	boolean addingQuestionsMode = addingQuestions != null;

    	Interview interview = safeGetInProgressInterview();

    	interview.name = interviewName;
        if(interview instanceof ActiveInterview){
            ((ActiveInterview) interview).anticipatedDate = anticipatedDate;
            ((ActiveInterview) interview).interviewer = User.findById(interviewerId);
            ((ActiveInterview) interview).notes = interviewNotes;
            setInProgressInterview(interview,
            		new InProgressInterviewContext(((ActiveInterview) interview).interviewee));
        }else{
        	setInProgressInterview(interview);
        }

        if (interview.id == null &&
                !AccountLimitedAction.CREATE_INTERVIEW.canPerform(
                        (AccountHolder)Security.connectedUser())) {
                
            validationError(Messages.get(
                                "limits.interviews.cannot_create"));
        }
        
    	if(addingQuestionsMode){
    		if(interview instanceof ActiveInterview){
    			renderArgs.put("candidate", ((ActiveInterview) interview).interviewee);
    		}
    		renderArgs.put("inProgressInterview", interview);
    		renderArgs.put(LOADED_WITH_UNSAVED_CHANGES, Boolean.TRUE);
    		render("@questionSelection");
    	}

    	if(validation.hasErrors()) {
            renderArgs.put(LOADED_WITH_UNSAVED_CHANGES, Boolean.TRUE);
    		render("@show", interview);
    	}

        /*merge the interview back into the session as it may have been loaded from the cache as the in progress
        interview and not associated with the current Hibernate session*/
    	interview = interview.merge();
    	
    	Hibernate.initialize(interview.interviewQuestions);


        /*update the status of the ActiveInterview after the initial save, changeStatus will actually persist the
        interview as well but we need the interview to be saved prior to this call due to queries that are run as
        part of the change status call */
        if(interview instanceof ActiveInterview){
            ((ActiveInterview) interview).changeStatus(Security.connectedUser(), ActiveInterviewState.NOT_STARTED);
        }
        

        interview.save();

        //if scheduling an active interview render the candidate show page that owns the interview, otherwise render the
        //interview list
        if(interview instanceof ActiveInterview){
            if(Boolean.valueOf(params.get("liveInterview"))){
                if(((ActiveInterview) interview).getStatus().equals(ActiveInterviewState.NOT_STARTED)){
                    ((ActiveInterview) interview).changeStatus(Security.connectedUser(), ActiveInterviewState.STARTED);
                }
                LiveInterviews.live(interview.id);
            } else {
                Candidate candidate = ((ActiveInterview)interview).interviewee;
                Candidates.show(candidate.id, null, null, null, false, null);
            }
        } else {
    	    list();
        }

    }

    /**
     * Just redirects to the list page.  List action will handle removal of in progress interview from the cache
     */
    public static void cancelEdit(){
        Interview interview = safeGetInProgressInterview();
        if(interview instanceof ActiveInterview){
            Candidates.show(((ActiveInterview) interview).interviewee.id, null, null, null, false, null);
        }
        list();
    }

    /**
     * Deletes an existing interview using the id of the current in progress interview
     */
    @RestrictedResource(name = {"models.Interview"}, staticFallback = true)
    public static void delete(Long id){
        
        Interview interview = Interview.findById(id);
        
        interview.inactivate();
        
        if (interview instanceof ActiveInterview) {
            ActiveInterview interviewAsActive = (ActiveInterview) interview;
            
            redirect("Candidates.show", interviewAsActive.interviewee.id);
        }
        else {
            list();
        }
    }
    
    /**
     * Add the questions with the given questionIds to the current in progress interview
     * @param questionIds primary key of each Question to be added to the interview
     */
    public static void addQuestions(@As(value=",") List<Long> questionIds){
    	
		try {
			Interview inProgressInterview = getInProgressInterview();
			if((questionIds != null) && (inProgressInterview != null)){
	    		for(Long id : questionIds){
	    			Question question = Question.getQuestion(id);
	    			if(question != null){
	    				inProgressInterview.addQuestionToInterview(question);
	    			}
	    		}
	    		setInProgressInterview(inProgressInterview);
	    	}
		} catch (CacheMiss e) {
			
		}    	
        
        show(null, false, true); //just pass null - the Cache will be queried to get the current in progress interview
    }
    
    /**
     * Get the estimated duration of the current interview. Will return
     * zero if there is no current interview.
     * 
     * @return minutes
     */
    public static int getInProgressInterviewDuration(){
    	Interview inProgressInterview;
		try {
			inProgressInterview = getInProgressInterview();
			return inProgressInterview.getDuration();
		} catch (CacheMiss e) {
			return 0;
		}
    	
    }

    /**
     * Add the question with the given questionId to the current in progress interview
     * @param questionId primary key of the Question to be added to the interview
     */
    public static void addQuestion(Long questionId){
        Question question = Question.getQuestion(questionId);        
		try {
			Interview inProgressInterview = getInProgressInterview();
			inProgressInterview.addQuestionToInterview(question);
        	setInProgressInterview(inProgressInterview);
		} catch (CacheMiss e) {
        }
        
        show(null, false, true); //just pass null - the Cache will be queried to get the current in progress interview
    }
    
    /**
     * Add the question with the given questionId to the current in-progress
     * interview, putting it in the given sort position and adjusting any 
     * questions at or after that position up one sort position.  If the given
     * sort position is too big or too small, the question will be placed at the
     * end or beginning of the list, respectively.
     * 
     * @param questionID primary key of the Question to be added to the 
     *            interview
     * @param sortPosition sort position where the Question should be inserted
     */
    public static void addQuestionInPosition(Long questionID, 
                Integer sortPosition) {
        Question question = Question.getQuestion(questionID);
        
        if (question == null) {
            throw new RuntimeException("No such question.");
        }
        
        try {
            Interview inProgressInterview = getInProgressInterview();
            inProgressInterview.addQuestionToInterview(question);
            
            int questionCount = inProgressInterview.getQuestionCount();
            sortPosition = Math.min(questionCount + 1, sortPosition);
            sortPosition = Math.max(0, sortPosition);
            
            inProgressInterview.reorderQuestion(question, sortPosition);
            setInProgressInterview(inProgressInterview);
        } catch (CacheMiss e) {
        
        }
        
        show(null, false, true); //just pass null - the Cache will be queried to get the current in progress interview
    }

    /**
     * remove the question with the given Id from the interview, adjust sort order values accordingly
     * @param questionId primary key of the Question to be removed from the interview
     */
    public static void removeQuestion(Long questionId){
        //interview questions might not have been saved yet but all interview questions will correspond to a saved
        //question with a not-null id value, so find the interviewQuestion that matches the questionid and then remove
        //from the collection
        Question question = Question.getQuestion(questionId);

        Interview inProgressInterview = safeGetInProgressInterview();

        inProgressInterview.removeQuestionFromInterview(question);
        
        setInProgressInterview(inProgressInterview);
        
        renderArgs.put("interview", inProgressInterview);
        renderArgs.put(LOADED_WITH_UNSAVED_CHANGES, Boolean.TRUE);
        render("@show");
    }

    /**
     * Reorders a Question in the list of InterviewQuestions
     * @param questionId primary key of the Question to be reordered
     * @param newOrder the new order of the Question in the list of InterviewQuestions
     */
    public static void reorderQuestion(Long questionId, Integer newOrder){
		try {
			Interview interview = getInProgressInterview();
			interview.reorderQuestion(Question.<Question>findById(questionId), newOrder);
                        setInProgressInterview(interview);
		} catch (CacheMiss e) {
        }
    }

    /**
     * Renders a printable version of an interview
     * @param id primary key of the Interview to be displayed.  Will be ignored if an in-progress interview is present
     *           in the cache
     * @param candidateInterviewCandidateName Name of the candidate to display 
     *            on the printed version, or null if we're printing a template
     */
    public static void printable(Long id, 
            String candidateInterviewCandidateName) {
        
        Interview interview;
		try {
			
			interview = getInProgressInterview();//use the potentially unsaved in-progress interview if present
			//merge the interview back into the Hibernate session so that we can access lazily initialized fields later in this method
            interview = interview.merge();
		} catch (CacheMiss e) {
			interview = Interview.findById(id);
		}

        //access notes field on each question in the interview to initialize the collection prior to rendering the view. This is necessary to avoid a LazyInitializationException.
        Company company = Security.connectedUser().company;
        for (InterviewQuestion interviewQuestion : interview.interviewQuestions) {
            Question question = interviewQuestion.question;
            question.getQuestionNote(company);
        }
        JPA.setRollbackOnly();
        render(interview, candidateInterviewCandidateName, company, false);
    }

    
    /**
     * Builds a list of questions based on the given time and difficulty constraints. Questions
     * with higher "interview scores" are preferred.
     * 
     * @param interviewDuration
     * @param categoryList
     */
    public static void findQuestionsForInterview(
    		@Required @Min(1) final int interviewDuration,
    		@Required @As(value=",", binder=ListBinder.class) List<String> categoryList){
    	
    	int totalContribution = 0;
    	List<Integer> contributions = new ArrayList<Integer>();
    	for(int i = 0; i < categoryList.size(); ++i){
    		Long id = Long.valueOf(categoryList.get(i));
    		String contributionStr = params.get("contribution_" + id);
    		if(contributionStr != null){
    			
    			try{
    				int contribution = Integer.parseInt(contributionStr);
    				totalContribution += contribution;
    				contributions.add(contribution);
    			}catch(NumberFormatException nfe){
    				validation.addError("contribution", "validation.invalid");
    			}
    		}else{
    			validation.addError("contribution", "validation.required");
    		}
    	}

    	List<InterviewCategory> icList = new ArrayList<InterviewCategory>();
    	for(int i = 0; i < categoryList.size(); ++i){
    		
    		Long categoryId = Long.valueOf(categoryList.get(i));
    		Category category = Category.findById(categoryId);
    		

    		String easy = params.get("difficulty_EASY_" + categoryId);
    		String medium = params.get("difficulty_MEDIUM_" + categoryId);
    		String hard = params.get("difficulty_HARD_" + categoryId);
    		
    		if((easy == null) && (medium == null) && (hard == null)){
    			validation.addError(categoryId + " difficulty", "validation.required");
    			break;
    		}
    		
    		List<Difficulty> difficulties = new ArrayList<Difficulty>();
    		if(easy != null){
    			difficulties.add(Difficulty.EASY);
    		}
    		if(medium != null){
    			difficulties.add(Difficulty.MEDIUM);
    		}
    		if(hard != null){
    			difficulties.add(Difficulty.HARD);
    		}
    		
    		float percent = (((float)contributions.get(i))/((float)totalContribution)) * 100f;
    		
    		icList.add(new InterviewCategory(category, percent, difficulties));
    	}
    	
    	if(validation.hasErrors()){
			Map<String,Object> args = new HashMap<String,Object>();
			try {
				args.put("interview", getInProgressInterview());
			} catch (CacheMiss e) {
			}

			String existingCategories = Categories.getCategoryNamesJSON();
			args.put("existingCategories", existingCategories);

			args.put(LOADED_WITH_UNSAVED_CHANGES, Boolean.TRUE);

			renderTemplate("@show",args);
		}
    	
    	CachedEntity<Interview, InProgressInterviewContext> ce = InterviewCache.safeGetInProgressInterviewEntity();
		Interview interview = ce.getEntity();
		InProgressInterviewContext context = ce.getContext();
		final Candidate targetCandidate = (context == null)?null:context.getCandidate();
    	
    	List<Question> questions = await(InterviewBuilder.buildInterview(
    			new InterviewRequest(
    					AccessGroup.INTERNAL,
    					targetCandidate,
    					interview.getQuestions(),
    					null,
    					interviewDuration,
    					icList)));
    	
    	for (Question q : questions) {
			if(!interview.getQuestions().contains(q)){
				interview.addQuestionToInterview(q);
			}
		}

		InterviewCache.setInProgressInterview(interview);

    	show(null, false, true);
    }

    /**
     * Adds questions from the existingInterview to the interview currently being edited.  Does not render a page as
     * this is intended to be called as an ajax request
     * @param id for the existing interview to use questions from
     */
    public static void addQuestionsFromExistingInterview(Long id){
        Interview interview = safeGetInProgressInterview();
        Interview sourceInterview = Interview.findById(id);
        List<Question> sourceInterviewQuestions = sourceInterview.getQuestions();
        for (Question sourceInterviewQuestion : sourceInterviewQuestions) {
            if(!interview.getQuestions().contains(sourceInterviewQuestion)){
                interview.addQuestionToInterview(sourceInterviewQuestion);
            }
        }
        setInProgressInterview(interview);

    }

    /**
     * Retrieves a list of interviews that can be used as source interviews when adding questions from another interview
     * @return list of Interviews
     */
    public static List<Interview> getPossibleSourceInterviews(){
    	
    	AccessibleInterviews query = new AccessibleInterviews();
    	
    	query.init(1, "asc", 0, Integer.MAX_VALUE);
    	
    	QueryResult<Interview> result = query.executeQuery();
    	
        List<Interview> allInterviews = result.getList();
		try {
			
			allInterviews.remove(getInProgressInterview());
			
		} catch (CacheMiss e) {
		}
        
        return allInterviews;
    }

    /**
     * Standard method for populating the data table on Interview List
     * @param sSearch
     * @param sEcho
     * @param iSortingCols
     * @param iSortCol_0
     * @param sSortDir_0
     * @param iDisplayStart
     * @param iDisplayLength
     */
    public static void interviewTableData(String sSearch, Long sEcho, Integer iSortingCols, Integer iSortCol_0,
                                          String sSortDir_0, Integer iDisplayStart, Integer iDisplayLength){

    	AccessibleInterviews query = new AccessibleInterviews();

    	if((sSearch != null) && (sSearch.length() > 0)){			
			
			SearchQuery<Interview> search = new SearchQuery<Interview>(query, sSearch);
			
			search.init(iSortCol_0, sSortDir_0, iDisplayStart, iDisplayLength);
			
			QueryResult<Interview> searchResults = search
				.addFilter(new ById())
				.addFilter(new ByName())
				.addFilter(new ByCategoryName())
				.executeQuery();
			renderJSON(new DataTablesSource(searchResults.getList(), searchResults.getTotal(), sEcho).toJson());
		}else{
    	
			query.init(iSortCol_0, sSortDir_0, iDisplayStart, iDisplayLength);
    	
			QueryResult<Interview> result = query.executeQuery();
			
			renderJSON(new DataTablesSource(result.getList(), result.getTotal(), sEcho).toJson());
		}
    }

    /**
     * Get a JSON representation of the Feedback associated with the given Interview for the currently logged in user
     * @param id Primary key of the ActiveInterview to build feedback response for
     */
    @RestrictedResource(name = {"models.ActiveInterview"}, staticFallback = true)
    public static void getUserFeedback(@Required Long id){
        notFoundIfNull(id);
        ActiveInterview interview = ActiveInterview.find("byIdAndInterviewer", id, Security.connectedUser()).first();
        notFoundIfNull(interview);

        Map<String, Object> response = new HashMap<String, Object>();
        response.put("interviewNotes", interview.notes);
        response.put("totalQuestions", interview.getQuestionCount());
        response.put("questionsRated", interview.getRatedQuestionCount());
        response.put("averageRating", interview.averageQuestionRating);
        response.put("questions", interview.interviewQuestions);

        renderJSON(FEEDBACK_SERIALIZER.toJson(response));
    }

    private static class InterviewQuestionFeedbackSerializer implements JsonSerializer<InterviewQuestion>{
        @Override
        public JsonElement serialize(InterviewQuestion interviewQuestion, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject result = new JsonObject();
            result.add("questionText", new JsonPrimitive(interviewQuestion.question.text));
            result.add("comment", new JsonPrimitive(interviewQuestion.comment));
            result.add("rating", interviewQuestion.rating != null ? new JsonPrimitive(interviewQuestion.rating.toInteger()) : null);
            return result;
        }
    }


    /************************************************************
     * Private Utility Methods                                  *
     ************************************************************/

    /**
     * Used to format interview data for display on the list page
     */
    private static class DataTablesSource extends utils.DataTablesSource {


        DataTablesSource(final List<Interview> interviews,
        		Long iTotalDisplayRecords,
                Long sEcho) {

            super(iTotalDisplayRecords, sEcho);

            //id, name, time, avg. question score, categories, created date
           for (Interview interview : interviews) {
            	List<Question> questions = interview.getQuestions();
            	int size = questions.size();
            	int avgScore = 0; 
            	int time = 0;
            	if((questions != null) && (size > 0)){
            		int totalScore = 0;
            		for(Question question : questions){
            			totalScore += question.standardScore;
            			time += question.getDuration();
            		}
            		avgScore = totalScore/size;
            	}
            	
                SafeStringArrayList interviewData = new SafeStringArrayList();
                interviewData.add(interview.id);
                interviewData.add(interview.name);
                interviewData.add(time);
                interviewData.add(avgScore);
                interviewData.add(interview.categories);
                interviewData.add(dateFormat.format(interview.created));

                aaData.add(interviewData);
            }
        }
    }

    private static class EventUserSerializer implements JsonSerializer<User> {
        public JsonElement serialize(User t, Type type, 
                JsonSerializationContext jsc) {
            
            JsonObject result = new JsonObject();
            result.add("name", new JsonPrimitive(t.getPublicDisplayName()));
            result.add("id", new JsonPrimitive(t.id));
            
            return result;
        }
    }
    
    private static class EventSerializer 
            implements JsonSerializer<ActiveInterviewEvent> {

        public JsonElement serialize(ActiveInterviewEvent t, Type type, 
                JsonSerializationContext jsc) {
            
            JsonObject result = new JsonObject();
            result.add("timestamp", new JsonPrimitive(t.created.getTime()));
            
            JsonArray event = new JsonArray();
            event.add(new JsonPrimitive(t.getClass().getSimpleName()));
            event.add(t.serialize());
            
            result.add("event", event);
            result.add("user", jsc.serialize(t.initiatingUser));
            result.add("id", new JsonPrimitive(t.id));
                        
            return result;
        }
    }
}
