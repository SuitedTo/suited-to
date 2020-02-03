package logic.interviews;

import java.util.*;
import java.util.concurrent.Callable;

import models.filter.question.IsFlaggedAsInappropriate;
import play.Logger;
import play.libs.F.Promise;
import utils.JobUtil;

import controllers.Interviews;
import enums.AccessGroup;
import enums.Difficulty;
import logic.interviews.InterviewRequest.InterviewCategory;
import logic.questions.finder.ListQualifier;
import logic.questions.finder.ListStopper;
import logic.questions.finder.QuestionFinder;
import logic.questions.finder.QuestionFinder.Request;
import logic.questions.finder.WeightedTable.Winner;
import models.*;
import models.cache.CachedEntity;
import models.cache.InterviewCache;
import models.cache.InterviewCache.InProgressInterviewContext;
import models.filter.question.ByCategory;
import models.filter.question.ByDifficulty;
import models.filter.question.ByExcludeFromPrep;
import models.filter.question.QuestionFilter;
import utils.LoggingUtil;

/**
 * Utility class for building interviews.
 * @author joel
 *
 */
public final class InterviewBuilder {

	private InterviewBuilder(){}

	/**
	 * Uses information contained in the given request to build up the
	 * in progress interview that is cached for the connected user.
	 * 
	 * @param request Interview request
	 */
	public static Promise<List<Question>> buildInterview(final InterviewRequest request){
        Logger.info("InterviewBuilder.buildInterview called with InterviewRequest \n" + request.toJson());
        return JobUtil.asJobInContext(new Callable<List<Question>>(){

			@Override
			public List<Question> call() {

                Candidate c = request.getTargetCandidate();
				if(c != null){
					request.setTargetCandidate((Candidate) c.merge());
				}

                Date start = new Date();
				List<Question> results = doPhase1(request);
                Date end = new Date();
                LoggingUtil.logElapsedTime("InterviewBuilder.phase1", start, end);


                request.setAssumedQuestions(results);

                Date phase2Start = new Date();
				results.addAll(doPhase2(request));
                Date phase2End = new Date();
                LoggingUtil.logElapsedTime("InterviewBuilder.phase2", phase2Start, phase2End);

                LoggingUtil.logElapsedTime("InterviewBuilder.total", start, phase2End);

                return results;
			}

		}).now();
	}

	/**
	 * This first phase collects the best questions from each category. The sum of
	 * all question durations in a given category will not be allowed to exceed the
	 * amount of time given to that category. This phase accepts the possibility that
	 * there could be a small time deficit in each category knowing that phase 2 will
	 * fill any void that is left behind.
	 * 
	 * @param request
	 */
	private static List<Question> doPhase1(final InterviewRequest request){


		List<InterviewCategory> icList = request.getInterviewCategory();

		final int interviewDuration = request.getDuration();
		int remainingDuration = interviewDuration;

		if(request.getAssumedQuestions() != null){
			outer: for(Question q : request.getAssumedQuestions()){
				if(q.category != null){
					for(InterviewCategory ic : icList){
						if(ic.getCategory().id == q.category.id){
							continue outer;
						}
					}
				}
				remainingDuration -= q.getDuration();
			}
		}

		final int finalRemainingDuration = remainingDuration;
		Map<Integer, List<Request>> durationToGroup = new Hashtable<Integer, List<Request>>();		
		Collections.<InterviewCategory>sort(icList);
		List<List<Request>> groups = new ArrayList<List<Request>>();
		for(int i = 0; i < icList.size(); ++i){
			InterviewCategory ic = icList.get(i);

			ListQualifier<String,Question, List<Winner<String,Question>>> categoryQualifier = null;

			float percent = ic.getPercentage();

			final int categoryDuration = (int)Math.round(new Float(finalRemainingDuration)*(percent/100f));

			List<Request> group = durationToGroup.get(categoryDuration);
			if(group == null){
				group = new ArrayList<Request>();
				durationToGroup.put(categoryDuration, group);
			}

			categoryQualifier = new ListQualifier<String,Question, List<Winner<String,Question>>>(){

				@Override
				public boolean qualifies(final String categoryName, final Question question, final List<Winner<String,Question>> winningQuestionsSoFar) {

					/*
					 * If a candidate is associated with this interview then don't allow any
					 * questions in that are in any of that candidate's other interviews.
					 */
					if(request.getTargetCandidate() != null){
						if(request.getTargetCandidate().getQuestionsToAvoid().contains(question)){
							return false;
						}
					}

					int timeSpentSoFar = 0;
					for(Winner<String,Question> winner : winningQuestionsSoFar){

						if((winner.getContext() != null) && winner.getContext().equals(categoryName)){
							timeSpentSoFar += winner.getValue().getDuration();
							if(question.duplicates(winner.getValue())){
								return false;
							}
						}

					}
					return (question.getDuration() + timeSpentSoFar) <= categoryDuration;
				}
			};

			List<QuestionFilter> filters = new ArrayList<QuestionFilter>();

			//exclude any questions that are marked flaggedAsInappropriate
			IsFlaggedAsInappropriate flaggedFilter = new IsFlaggedAsInappropriate();
			flaggedFilter.exclude(Boolean.TRUE.toString());
			filters.add(flaggedFilter);
			
			if(request.getAccessGroup() == AccessGroup.PREPADO){
				ByExcludeFromPrep excludedFilter = new ByExcludeFromPrep();
				excludedFilter.exclude(Boolean.TRUE.toString());
				filters.add(excludedFilter);
			}

			Category category = ic.getCategory();
			if(category != null){
				//exclude any questions that are already part of the in progress
				//interview
				models.filter.question.ById byId = new models.filter.question.ById();
				List<Question> questions = request.getAssumedQuestions();
				if(questions != null){
					for(Question question : questions){
						if((question.category != null) && (question.category.id == category.id)){
							byId.exclude(String.valueOf(question.id));
						}
					}
				}

				if(request.getBlackList() != null){
					for(Question question : request.getBlackList()){
						byId.exclude(String.valueOf(question.id));
					}
				}

				if(byId.getExcludes().size() > 0){
					filters.add(byId);
				}
			}

			List<Difficulty> difficulties = ic.getDifficulties();
			ByDifficulty dFilter = new ByDifficulty();
			for(Difficulty difficulty : difficulties){
				dFilter.include(difficulty.name());
			}

			filters.add(dFilter);

			QuestionFinder.Request qfRequest = new QuestionFinder.Request(category,
					filters,
					categoryQualifier);

			group.add(qfRequest);

			if(!groups.contains(group)){
				groups.add(group);
			}
		}




		ListStopper<String,Question,List<Winner<String,Question>>> interviewTimeLimitation =
				new ListStopper<String,Question, List<Winner<String,Question>>>(){

			@Override
			public boolean qualifies(final String category, final Question question, final List<Winner<String,Question>> questionsSoFar) {
				int totalDuration = 0;
				for(Winner<String,Question> w : questionsSoFar){
					totalDuration += w.getValue().getDuration();
				}

				return (question.getDuration() + totalDuration) <= interviewDuration;
			}

			@Override
			public int cap() {
				return interviewDuration;
			}

		};


		return QuestionFinder.findQuestions(
				request.getAssumedQuestions(),

				groups,

				interviewTimeLimitation,

				false,

				"Phase1");
	}

	/**
	 * This second phase is designed to ensure that the interview is full. Exceeding
	 * the desired duration is preferred here over a time deficit so, if there
	 * are enough questions available, then the resulting interview duration will be
	 * at least equal to the desired duration and can be up to (Timing.LONG.duration -1)
	 * longer than the desired duration.
	 * 
	 * 
	 * @param request
	 */
	private static List<Question> doPhase2(final InterviewRequest request){

		final int interviewDuration = request.getDuration();

		if(request.getActualDuration() >= interviewDuration){
			return new ArrayList<Question>();
		}

		ListQualifier<String,Question, List<Winner<String,Question>>> categoryQualifier =
				new ListQualifier<String,Question, List<Winner<String,Question>>>(){

			@Override
			public boolean qualifies(final String categoryName, final Question question, final List<Winner<String,Question>> winningQuestionsSoFar) {

				if(request.getTargetCandidate() != null){
					if(request.getTargetCandidate().getQuestionsToAvoid().contains(question)){
						return false;
					}
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

		List<InterviewCategory> icList = request.getInterviewCategory();

		int remainingDuration = interviewDuration;
		outer: for(Question q : request.getAssumedQuestions()){
			if(q.category != null){
				for(InterviewCategory ic : icList){
					if(ic.getCategory().id == q.category.id){
						continue outer;
					}
				}
			}
			remainingDuration -= q.getDuration();
		}

		final int finalRemainingDuration = remainingDuration;
		Map<Integer, List<Request>> durationToGroup = new Hashtable<Integer, List<Request>>();
		Collections.<InterviewCategory>sort(icList);
		List<List<Request>> groups = new ArrayList<List<Request>>();
		for(int i = 0; i < icList.size(); ++i){


			InterviewCategory ic = icList.get(i);

			float percent = ic.getPercentage();

			final int categoryDuration = (int)Math.round(new Float(finalRemainingDuration)*(percent/100f));

			List<Request> group = durationToGroup.get(categoryDuration);
			if(group == null){
				group = new ArrayList<Request>();
				durationToGroup.put(categoryDuration, group);
			}

			List<QuestionFilter> filters = new ArrayList<QuestionFilter>();

			//exclude any questions that are marked flaggedAsInappropriate
			IsFlaggedAsInappropriate flaggedFilter = new IsFlaggedAsInappropriate();
			flaggedFilter.exclude(Boolean.TRUE.toString());
			filters.add(flaggedFilter);
			
			if(request.getAccessGroup() == AccessGroup.PREPADO){
				ByExcludeFromPrep excludedFilter = new ByExcludeFromPrep();
				excludedFilter.exclude(Boolean.TRUE.toString());
				filters.add(excludedFilter);
			}

			Category category = ic.getCategory();
			if(category != null){

				//exclude any questions that are already part of the in progress
				//interview
				models.filter.question.ById byId = new models.filter.question.ById();
				List<Question> questions = request.getAssumedQuestions();
				if(questions != null){
					for(Question question : questions){
						if((question.category != null) && (question.category.id == category.id)){
							byId.exclude(String.valueOf(question.id));
						}
					}
				}

				if(request.getBlackList() != null){
					for(Question question : request.getBlackList()){
						byId.exclude(String.valueOf(question.id));
					}
				}

				if(byId.getExcludes().size() > 0){
					filters.add(byId);
				}
			}

			List<Difficulty> difficulties = ic.getDifficulties();
			ByDifficulty dFilter = new ByDifficulty();
			for(Difficulty difficulty : difficulties){
				dFilter.include(difficulty.name());
			}

			filters.add(dFilter);

			QuestionFinder.Request qfRequest = new QuestionFinder.Request(category,
					filters,
					categoryQualifier);

			group.add(qfRequest);

			if(!groups.contains(group)){
				groups.add(group);
			}
		}




		ListStopper<String,Question,List<Winner<String,Question>>> interviewTimeLimitation =
				new ListStopper<String,Question, List<Winner<String,Question>>>(){

			@Override
			public boolean qualifies(final String category, final Question question, final List<Winner<String,Question>> questionsSoFar) {
				int totalDuration = 0;
				for(Winner<String,Question> w : questionsSoFar){
					totalDuration += w.getValue().getDuration();
				}

				return totalDuration < interviewDuration;
			}

			@Override
			public int cap() {
				return interviewDuration*2;
			}

		};


		return QuestionFinder.findQuestions(
				request.getAssumedQuestions(),

				groups,

				interviewTimeLimitation,

				true,

				"Phase2");
	}
}
