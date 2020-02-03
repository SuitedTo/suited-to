package controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import controllers.api.serialization.InterviewQuestionSerializer;
import controllers.api.serialization.QuestionSerializer;
import controllers.deadbolt.Deadbolt;
import controllers.deadbolt.RestrictedResource;
import controllers.deadbolt.RoleHolderPresent;
import enums.ActiveInterviewState;
import enums.Difficulty;
import enums.QuestionRating;
import enums.QuestionStatus;
import logic.questions.finder.ListQualifier;
import logic.questions.finder.ListStopper;
import logic.questions.finder.QuestionFinder;
import logic.questions.finder.WeightedTable;
import logic.questions.finder.WeightedTable.Winner;
import models.*;
import models.filter.question.ByDifficulty;
import models.filter.question.ById;
import models.filter.question.IsFlaggedAsInappropriate;
import models.filter.question.QuestionFilter;
import play.Play;
import play.data.binding.As;
import play.data.validation.Required;
import play.mvc.Util;
import play.mvc.With;

import java.util.*;

/**
 * Controller for Live Interview functionality
 */
@With(Deadbolt.class)
@RoleHolderPresent
public class LiveInterviews extends ControllerBase {

    /**
     * Adds the given question to the given interview and saves
     * @param questionId key of the question to add to the interview
     * @param interviewId key of the interview to add the question to
     * @param order order of the question in the interview (1-based) subsequent questions will be adjusted automatically
     */
    public static void addQuestionToInterview(
            @Required final Long questionId,
            @Required final Long interviewId,
            @Required final Integer order){

        Question question = Question.findById(questionId);
        Interview interview = Interview.findById(interviewId);
        User user = Security.connectedUser();

        if(question == null || !question.hasAccess(user) || interview == null || !interview.hasAccess(user)){
            unauthorized();
        }

        interview.addQuestionToInterview(question, order);
        interview.save();
        renderJSON(buildSuccessResponseMap());
    }
    
    /**
     * Creates and saves a new active interview. Redirects to live.
     */
    /**
     * @param candidateId The interviewee
     */
    public static void startInterview(@Required final Long candidateId) {
    	Candidate interviewee = Candidate.findById(candidateId);
    	notFoundIfNull(interviewee);
    	
    	final User user = Security.connectedUser();
        final ActiveInterview interview = (ActiveInterview) new ActiveInterview(
        		user,
        		interviewee);
        
        interview.name = ActiveInterview.getNextName(interviewee, user.company);

        interview.interviewer = user;
        interview.save();
        
        //change status which triggers a save
        interview.changeStatus(user, ActiveInterviewState.STARTED);
        
        live(interview.id);
    }

    /**
     * Ends the interview by processing the new order of the questions in the interview, updating the status of the
     * interview, prompting for feedback, and redirecting to the candidate's profile page
     * @param id key of the ActiveInterview being ended
     * @param questionOrder Array of question ids in the order in which they should be preserved in the interview
     */
    public static void endInterview(@Required final Long id, @Required @As(",") final List<Long> questionOrder) {
        ActiveInterview interview = ActiveInterview.findById(id);
        List<InterviewQuestion> interviewQuestions = interview.interviewQuestions;
        List<InterviewQuestion> newInterviewQuestions = new ArrayList<InterviewQuestion>();

        //todo: this could potentially take a little while, see about implementing as a Job/Promise
        questionOrder.removeAll(Collections.singletonList(null));//remove any null values from the list

        //modify the InterviewQuestions so that only asked questions remain and ratings and comments are preserved
        for (int i=0; i<questionOrder.size(); i++) {
            Long questionId = questionOrder.get(i);
            for (InterviewQuestion interviewQuestion : interviewQuestions) {
                if(interviewQuestion.question.id.equals(questionId)){
                    interviewQuestion.sortOrder = i+1;
                    newInterviewQuestions.add(interviewQuestion);
                    break;
                }
            }
        }

        interview.interviewQuestions = newInterviewQuestions;

        //go ahead and call save here.  Problems arise downstream in changeStatus which also calls interview.save() if
        //if the interviewQuestions are not already saved.  I think it has something to do with transactions and having
        //a second method down the chain calling save() as opposed to doing it right here but have not yet had a chance
        //to thoroughly debug.
        interview.save();

        //change status which triggers a save
        interview.changeStatus(Security.connectedUser(), ActiveInterviewState.FINISHED);

        flash.put("fromLiveInterview", "true");
        Feedbacks.create(interview.interviewee.id, interview.id, null);
    }
    
    /**
     * Builds a limited list of questions that are in the given category and adds them to the
     * given interview.
     * Renders the newly added questions as JSON using the QuestionSerializer
     *
     * @param interviewId The interview that will get the new questions
     * @param categoryId The category
     * @param difficulties Difficulties to include (optional)
     * @param limit Maximum number of questions in the resulting list (optional)
     */
    public static void addQuestionsToInterview(
            @Required final Long interviewId,
            @Required final Long categoryId,
            @As(value=",") final List<Difficulty> difficulties,
            final Integer limit){
    
    	ActiveInterview interview = ActiveInterview.findById(interviewId);
    	notFoundIfNull(interview);
    	
    	Candidate interviewee = interview.interviewee;
    	notFoundIfNull(interviewee);
    	
    	Category category = Category.findById(categoryId);
    	notFoundIfNull(category);
    	
    	List<Question> excludedQuestions = interview.getQuestions();
    	List<Question> questionsToAdd = findQuestions(
        		interviewee,
                category,
                difficulties,
                excludedQuestions,
                limit
                );
    	
    	for(Question question : questionsToAdd){
    		interview.addQuestionToInterview(question);
    	}
    	
    	interview.save();
    	
    	renderJSON(new GsonBuilder()
        	.serializeNulls()
        	.registerTypeAdapter(Question.class, new QuestionSerializer(null, null))
        	.create()
        	.toJson(questionsToAdd));
    }
    
    /**
     * Builds a limited list of questions that are in the same category as the
     * given question. Resulting questions are intended for the given interview and candidate. Renders the resulting
     * question list to JSON using the QuestionSerializer
     *
     * @param candidateId The candidate
     * @param categoryId The category
     * @param difficulties Difficulties to include (optional)
     * @param excludedQuestions Questions to exclude (optional)
     * @param limit Maximum number of questions in the resulting list (optional)
     */
    @Util
    public static List<Question> findQuestions(
    		final Candidate candidate,
            final Category category,
            final List<Difficulty> difficulties,
            final List<Question> excludedQuestions,
            final Integer limit){
    	
    	final int numberOfQuestions = (limit == null)
    			?Integer.parseInt(Play.configuration.getProperty("defaultInterviewQuestionLimit", "5"))
    			:limit;

        if(numberOfQuestions <= 0){
            renderJSON("{}");
        }


        ListQualifier<String,Question, List<WeightedTable.Winner<String,Question>>> categoryQualifier =
                new ListQualifier<String,Question, List<WeightedTable.Winner<String,Question>>>(){

                    @Override
                    public boolean qualifies(final String categoryName, final Question question,
                                             final List<WeightedTable.Winner<String,Question>> winningQuestionsSoFar) {

                        /*
                       * If a candidate is associated with this interview then don't allow any
                       * questions in that are in any of that candidate's other interviews.
                       */
                        if(candidate != null){
                            List<ActiveInterview> interviews = candidate.activeInterviews;
                            if(interviews != null){
                                for(ActiveInterview ci : interviews){
                                    List<Question> alreadyAsked = ci.getQuestions();
                                    List<Question> alreadyAskedPlusDups = new ArrayList<Question>();
                                    alreadyAskedPlusDups.addAll(alreadyAsked);
                                    for(Question q : alreadyAsked){
                                        alreadyAskedPlusDups.addAll(q.getDuplicates());
                                    }
                                    if(alreadyAskedPlusDups.contains(question)){
                                        return false;
                                    }
                                }
                            }
                        }
                        
                        if (winningQuestionsSoFar.size() >= limit){
                        	return false;
                        }
                        
                        for(Winner<String,Question> winner : winningQuestionsSoFar){
    						if((winner.getContext() != null) && winner.getContext().equals(categoryName)){
    							if(question.duplicates(winner.getValue())){
    								return false;
    							}
    						}
    					}
                        
                        return true;
                    }
                };

        List<QuestionFilter> filters = new ArrayList<QuestionFilter>();

        //exclude any questions that are marked flaggedAsInappropriate
        IsFlaggedAsInappropriate flaggedFilter = new IsFlaggedAsInappropriate();
        flaggedFilter.exclude(Boolean.TRUE.toString());
        filters.add(flaggedFilter);

//        InterviewBuilderQuery's executeQuery() handles this - question doesn't work with ByCategory currently
//        if(category != null){
//            //include only questions from this category
//            ByCategory categoryFilter = new ByCategory();
//            categoryFilter.include(String.valueOf(category.id));
//            filters.add(categoryFilter);
//
//        }
        
        if(difficulties != null){
        	ByDifficulty dFilter = new ByDifficulty();
        	for(Difficulty difficulty : difficulties){
        		dFilter.include(difficulty.name());
        	}
        	filters.add(dFilter);
        }
        
        if(excludedQuestions != null){
        	ById idFilter = new ById();
        	for(Question q : excludedQuestions){
        		idFilter.exclude("" + q.id);
        	}
        	filters.add(idFilter);
        }

        QuestionFinder.Request qfRequest = new QuestionFinder.Request(category,
                filters,
                categoryQualifier);

        List<List<QuestionFinder.Request>> groups = new ArrayList<List<QuestionFinder.Request>>();
        List<QuestionFinder.Request> group = new ArrayList<QuestionFinder.Request>();
        group.add(qfRequest);

        if(!groups.contains(group)){
            groups.add(group);
        }




        ListStopper<String,Question,List<WeightedTable.Winner<String,Question>>> stopper =
                new ListStopper<String,Question, List<WeightedTable.Winner<String,Question>>>(){

                    @Override
                    public boolean qualifies(final String category, final Question question, final List<WeightedTable.Winner<String,Question>> questionsSoFar) {
                        return true;
                    }

                    @Override
                    public int cap() {
                        return numberOfQuestions * 3;
                    }

                };


        return QuestionFinder.findQuestions(

                groups,

                stopper,

                false,

                "Live_find");


    }

    /**
     * Builds a limited list of questions that are in the same category as the
     * given question. Resulting questions are intended for the given interview and candidate. Renders the resulting
     * question list to JSON using the QuestionSerializer
     *
     * @param questionId The question used to match category
     * @param interviewId The interview for which this list is intended
     * @param candidateId The candidate for which this list is intended
     * @param limit Maximum number of questions in the resulting list
     */
    public static void findSimilarQuestions(
            @Required final Long questionId,
            @Required final Long interviewId,
            @Required final Long candidateId,
            @Required final int limit){

        final Question question = Question.findById(questionId);
        final Interview interview = Interview.findById(interviewId);
        final Candidate candidate = Candidate.findById(candidateId);
        notFoundIfNull(question);
        notFoundIfNull(interview);
        notFoundIfNull(candidate);

        if(limit <= 0){
        	renderJSON("{}");
        }


        ListQualifier<String,Question, List<WeightedTable.Winner<String,Question>>> categoryQualifier =
                new ListQualifier<String,Question, List<WeightedTable.Winner<String,Question>>>(){

                    @Override
                    public boolean qualifies(final String categoryName, final Question question,
                                             final List<WeightedTable.Winner<String,Question>> winningQuestionsSoFar) {

                        /*
                       * If a candidate is associated with this interview then don't allow any
                       * questions in that are in any of that candidate's other interviews.
                       */
                        if(candidate != null){
                            List<ActiveInterview> interviews = candidate.activeInterviews;
                            if(interviews != null){
                                for(ActiveInterview ci : interviews){
                                    List<Question> alreadyAsked = ci.getQuestions();
                                    List<Question> alreadyAskedPlusDups = new ArrayList<Question>();
                                    alreadyAskedPlusDups.addAll(alreadyAsked);
                                    for(Question q : alreadyAsked){
                                        alreadyAskedPlusDups.addAll(q.getDuplicates());
                                    }
                                    if(alreadyAskedPlusDups.contains(question)){
                                        return false;
                                    }
                                }
                            }
                        }
                        
                        if (winningQuestionsSoFar.size() >= limit){
                        	return false;
                        }

    					for(Winner<String,Question> winner : winningQuestionsSoFar){
    						if((winner.getContext() != null) && winner.getContext().equals(categoryName)){
    							if(question.duplicates(winner.getValue())){
    								return false;
    							}
    						}
    					}
                        return true;
                    }
                };

        List<QuestionFilter> filters = new ArrayList<QuestionFilter>();

        //exclude any questions that are marked flaggedAsInappropriate
        IsFlaggedAsInappropriate flaggedFilter = new IsFlaggedAsInappropriate();
        flaggedFilter.exclude(Boolean.TRUE.toString());
        filters.add(flaggedFilter);

        Category category = question.category;
        if(category != null){
            //exclude any questions that are already part of the given
            //interview
            models.filter.question.ById byId = new models.filter.question.ById();
            List<Question> questions = interview.getQuestions();
            for(Question q : questions){
                if((q.category != null) && (q.category.id.equals(category.id))){
                    byId.exclude(String.valueOf(q.id));
                }
            }
            if(byId.getExcludes().size() > 0){
                filters.add(byId);
            }
        }

        QuestionFinder.Request qfRequest = new QuestionFinder.Request(category,
                filters,
                categoryQualifier);

        List<List<QuestionFinder.Request>> groups = new ArrayList<List<QuestionFinder.Request>>();
        List<QuestionFinder.Request> group = new ArrayList<QuestionFinder.Request>();
        group.add(qfRequest);

        if(!groups.contains(group)){
            groups.add(group);
        }




        ListStopper<String,Question,List<WeightedTable.Winner<String,Question>>> interviewTimeLimitation =
                new ListStopper<String,Question, List<WeightedTable.Winner<String,Question>>>(){

                    @Override
                    public boolean qualifies(final String category, final Question question, final List<WeightedTable.Winner<String,Question>> questionsSoFar) {
                        return true;
                    }

                    @Override
                    public int cap() {
                        return limit * 2;
                    }

                };


        List<Question> results =  QuestionFinder.findQuestions(
                interview,

                groups,

                interviewTimeLimitation,

                false,

                "Live_Similar");

        renderJSON(new GsonBuilder()
                .serializeNulls()
                .registerTypeAdapter(Question.class, new QuestionSerializer(null, null))
                .create()
                .toJson(results));
    }

    /**
     * Gets all the questions for the interview in JSON format as defined in QuestionSerializer
     * @param id key of the interview to load questions for
     */
    @RestrictedResource(name = {"models.ActiveInterview"}, staticFallback = true)
    public static void getLiveInterviewJson(@Required final Long id){
        ActiveInterview liveInterview = ActiveInterview.findById(id);
        notFoundIfNull(liveInterview);

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(InterviewQuestion.class,
                        new InterviewQuestionSerializer(Security.connectedUser().company, liveInterview))
                .serializeNulls()
                .create();
        renderJSON(gson.toJson(liveInterview.interviewQuestions));
    }

    /**
     * Initializes the LiveInterview flow
     * @param id key of the ActiveInterview to load and render into the view
     */
    @RestrictedResource(name = {"models.ActiveInterview"}, staticFallback = true)
    public static void live(@Required final Long id){
        ActiveInterview interview = ActiveInterview.findById(id);
        render(interview);
    }

    /**
     * Adds a new quick question to the given interview.  Renders the id of the newly created question as the response
     * @param id key of the interview to add the question to
     * @param text text of the question
     */
    @RestrictedResource(name = {"models.ActiveInterview"}, staticFallback = true)
    public static void quickAddQuestion(
            @Required final Long id,
            @Required final String text){
        Question question = new Question(text, Security.connectedUser());
        question.status = QuestionStatus.QUICK;
        question.save();

        Interview interview = Interview.findById(id);
        interview.addQuestionToInterview(question);
        interview.save();

        Map<String, String> response = buildSuccessResponseMap();
        response.put("questionId", question.id.toString());
        renderJSON(response);
    }

    /**
     * Remove the question with the given Id from the interview, adjust sort order values accordingly
     * @param questionId primary key of the Question to be removed from the interview
     */
    public static void removeQuestion(
            @Required final Long questionId,
            @Required final Long interviewId){

        Question question = Question.findById(questionId);
        Interview interview = Interview.findById(interviewId);
        User user = Security.connectedUser();
        if(question == null || !question.hasAccess(user) || interview == null || !interview.hasAccess(user)){
            unauthorized();
        }

        interview.removeQuestionFromInterview(question);
        interview.save();
        renderJSON(buildSuccessResponseMap());
    }

    public static void updateQuestion(@Required final Long questionId,
                                      @Required final Long interviewId,
                                      @Required final QuestionRating rating,
                                      @Required final String comment) {

        Question question = Question.findById(questionId);
        Interview interview = Interview.findById(interviewId);
        User user = Security.connectedUser();
        if(question == null || !question.hasAccess(user) || interview == null || !interview.hasAccess(user)){
            unauthorized();
        }

        for (InterviewQuestion interviewQuestion : interview.interviewQuestions) {
            if (interviewQuestion.question.equals(question)) {
                if (comment != null) {
                    interviewQuestion.comment = comment;
                }
                if (rating != null) {
                    interviewQuestion.rating = rating;
                }
                if (comment != null || rating != null) {
                    interviewQuestion.save();
                }
                renderJSON(buildSuccessResponseMap());
                break;
            }
        }

        renderJSON("{}");
    }

    /**
     * Updates the notes on an ActiveInterview
     * @param activeInterviewId primary key of the ActiveInterview for which to update the notes
     * @param interviewNotes String to store in the notes field
     */
    @RestrictedResource(name = {"models.ActiveInterview"}, staticFallback = true)
    public static void updateNotes(Long activeInterviewId, String interviewNotes){
        ActiveInterview interview = ActiveInterview.findById(activeInterviewId);
        interview.notes = interviewNotes;
        interview.save();
        renderJSON(buildSuccessResponseMap());
    }

}
