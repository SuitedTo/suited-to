package controllers.prep.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import models.prep.PrepCategory;
import models.prep.PrepInterview;
import models.prep.PrepInterviewBuild;
import models.prep.PrepQuestion;
import models.prep.PrepUser;
import play.Logger;
import play.Play;
import play.data.binding.As;
import play.data.validation.Required;
import play.db.jpa.JPAPlugin;
import play.jobs.Job;
import play.mvc.With;

import common.utils.prep.SecurityUtil;

import controllers.prep.access.Access;
import controllers.prep.access.PrepRestrictedResource;
import controllers.prep.delegates.ErrorHandler;
import controllers.prep.delegates.InterviewBuilderDelegate;
import data.binding.types.prep.JsonBinder;
import dto.prep.PrepInterviewBuildDTO;
import dto.prep.PrepInterviewCategoryDTO;
import dto.prep.PrepInterviewCategoryListDTO;
import dto.suitedto.SuitedToCategoryDTO;
import dto.suitedto.SuitedToInterviewCategoryDTO;
import dto.suitedto.SuitedToInterviewRequestDTO;
import dto.suitedto.SuitedToQuestionDTO;
import errors.prep.PrepError;
import errors.prep.PrepErrorResult;
import errors.prep.PrepErrorType;

@With(Access.class)
public class InterviewBuilds extends PrepController{
	
	/**
	 * Kick off a build and return it. The client is responsible for polling
	 * the build to figure out when it is finished. When it includes in interviewId
	 * it is finished.
	 * 
	 * 
	 * *** A PrepInterview Build will be created and saved as a result of this call ***
	 * 
	 * 
	 * @param searchTerm
	 */
    //Instead of using RestrictAccess with FreeIfNoOwnedInterviews I created the same functionality inside of
    //the BuildJob job. I was not able to use the await(job) with the Restrict without it being called multiple times
    //and failing whenever the job actually finishes.
	public static void create(@Required @As(binder = JsonBinder.class) PrepInterviewCategoryListDTO body) {
		
		PrepInterviewBuild newBuild = new PrepInterviewBuild();
		newBuild.save();
		
		PrepUser user = SecurityUtil.connectedUser();

        JPAPlugin.closeTx(false);


        Future<PrepInterview> job = new BuildJob(newBuild.id, user, body).now();
        PrepInterview interview = await(job);

        if(interview != null){
            renderRefinedJSON(PrepInterviewBuildDTO.fromPrepInterviewBuild(newBuild).toJsonTree());
        } else {
            ErrorHandler.emitCustomErrorResult(PrepErrorType.INVALID_INTERVIEW, "INVALID_INTERVIEW");
        }


    }

    @PrepRestrictedResource(resourceClassName = "models.prep.PrepInterviewBuild")
	public static void get(Long id) {
		
		PrepInterviewBuild build = PrepInterviewBuild.find.byId(id);
		
		if(build == null){
			notFound();
		}
		
		renderRefinedJSON(PrepInterviewBuildDTO.fromPrepInterviewBuild(build).toJsonTree());
	}
	
    private static int getInterviewDuration(PrepUser user){
		if(!user.hasPaid()){
			return Integer.parseInt(Play.configuration.getProperty("prep.limitedPracticeInteviewDuration","20"));
		}
		return Integer.parseInt(Play.configuration.getProperty("prep.practiceInteviewDuration","60"));
	}

	
	public static class BuildJob extends Job<PrepInterview>{
		private final Long id;
		private final PrepUser user;
		private final PrepInterviewCategoryListDTO request;
		
		public BuildJob(Long id, PrepUser user, PrepInterviewCategoryListDTO request){
			this.id = id;
			this.user = user;
			this.request = request;
		}
		public PrepInterview doJobWithResult(){
            int nameCount = 0;

			List<SuitedToInterviewCategoryDTO> suitedToInterviewCategories =
					new ArrayList<SuitedToInterviewCategoryDTO>();
			if(request.categories != null){
				for(PrepInterviewCategoryDTO prepCategoryDTO : request.categories){
					SuitedToInterviewCategoryDTO sic = new SuitedToInterviewCategoryDTO();
					
					PrepCategory pc = PrepCategory.find.byId(prepCategoryDTO.category.id);
					if((pc == null) || (pc.categoryId == null)){
						continue;
					}
					SuitedToCategoryDTO sc = new SuitedToCategoryDTO();
					sc.id = Long.parseLong(pc.categoryId);
					sic.category = sc;
					
					sic.contribution = prepCategoryDTO.contribution;
					
					List<enums.prep.Difficulty> difficulties = new ArrayList<enums.prep.Difficulty>();
					difficulties.add(prepCategoryDTO.difficulty);
					sic.difficulties = difficulties;
					
					suitedToInterviewCategories.add(sic);
				}
			}
			
			List<SuitedToQuestionDTO> blackList = new ArrayList<SuitedToQuestionDTO>();
			
			List<PrepInterview> interviews = PrepInterview.find.where().eq("owner.id", user.id).findList();
            if(!user.hasPaid()){
                for(PrepInterview interview : interviews){
                    if(interview.valid){
                        throw new PrepErrorResult(new PrepError(PrepErrorType.ACCESS_ERROR));
                    }
                }
            }
			for(PrepInterview interview : interviews){
                nameCount = interviewNumber(interview.name, request.name, nameCount);

				List<PrepQuestion> prepQuestions  = interview.questions;
				if(prepQuestions != null){
					for(PrepQuestion pq : prepQuestions){
						if(pq.questionId != null){
							SuitedToQuestionDTO sq = new SuitedToQuestionDTO();
							sq.id = Long.parseLong(pq.questionId);
							blackList.add(sq);
						}
					}
				}
			}
			
			SuitedToInterviewRequestDTO suitedToInterviewRequest = new SuitedToInterviewRequestDTO();
			suitedToInterviewRequest.categories = suitedToInterviewCategories;
			suitedToInterviewRequest.blackList = blackList;
			
			List<SuitedToQuestionDTO> questions =
					InterviewBuilderDelegate.buildInterview(getInterviewDuration(user), user.hasPaid(), suitedToInterviewRequest);

            if(questions.size() > 0){
                PrepInterview interview = new PrepInterview();
                if(nameCount > 1){
                    interview.name = request.name + " (" + nameCount + ")";
                } else {
                    interview.name = request.name;
                }
                interview.owner = user;
                interview.currentQuestion = 0;
                interview.valid = questions.size() > 0;
                interview.save();
                for(SuitedToQuestionDTO sq : questions){
                    PrepQuestion newQuestion = new PrepQuestion();
                    newQuestion.questionId = "" + sq.id;
                    newQuestion.text = sq.text;
                    newQuestion.interview = interview;

                    // default staticAnswers and tips to null
                    newQuestion.staticAnswers = null;
                    newQuestion.tips = null;

                    // set staticAnswers and tips accordingly
                    if(sq.prepAnswers != null && !sq.prepAnswers.equals("")) {
                        newQuestion.staticAnswers = sq.prepAnswers;
                    } else {
                        if(sq.answers != null && !sq.answers.equals("")) {
                            newQuestion.staticAnswers = sq.answers;
                        }
                        if(sq.tips != null && !sq.tips.equals("")) {
                            newQuestion.tips = sq.tips;
                        }
                    }

                    newQuestion.save();
                }

                if(id != null){
                	PrepInterviewBuild build = PrepInterviewBuild.find.byId(id);
                	if(build != null){
                		build.interview = interview;
                		build.save();
                	} else {
                		Logger.error("InterviewBuild was not found");
                		return null;
                	}
                }
            	return interview;
            }
            return null;
		}

        public int interviewNumber(String interviewName, String requestName, int highestNumber){
            int currentNumber = 0;
            if(interviewName.contains("(") && interviewName.contains(")")){

                if(interviewName.substring(0,interviewName.lastIndexOf('(')).trim().compareToIgnoreCase(requestName.trim()) == 0){
                    currentNumber = Integer.valueOf(interviewName.substring(interviewName.lastIndexOf('(')+1, interviewName.lastIndexOf(')')));
                    if(currentNumber >= highestNumber){
                        return currentNumber + 1;
                    }
                } else {
                    if(interviewName.compareToIgnoreCase(requestName) == 0 && highestNumber < 2){
                    	return 2;
                    }
                }
            } else {

                if(interviewName.compareToIgnoreCase(requestName) == 0 && highestNumber < 2){
                    return 2;
                }
            }
            return highestNumber;
        }
	}
}
