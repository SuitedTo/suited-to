package controllers.prep.delegates;

import java.util.ArrayList;
import java.util.List;

import dto.suitedto.SuitedToInterviewCategoryDTO;
import dto.suitedto.SuitedToInterviewRequestDTO;
import dto.suitedto.SuitedToQuestionDTO;
import play.Logger;

import logic.interviews.InterviewBuilder;
import logic.interviews.InterviewRequest;
import logic.interviews.InterviewRequest.InterviewCategory;
import models.Category;
import models.Question;

import enums.AccessGroup;
import enums.prep.Contribution;

public class InterviewBuilderDelegate {

    // max number of questions allowed in practice interviews
    public static final Integer NUMBER_OF_QUESTIONS = 12;
    public static final Integer UNPAID_NUMBER_OF_QUESTIONS = 5;
	
	public static List<SuitedToQuestionDTO> buildInterview(int interviewDuration, boolean hasPaid, SuitedToInterviewRequestDTO request){
		
		List<InterviewCategory> categories = new ArrayList<InterviewCategory>();
		if(request.categories != null){
			int totalContribution = 0;
			for(SuitedToInterviewCategoryDTO dto : request.categories){
				if(dto.contribution.equals(Contribution.SMALL)){
					totalContribution += 1;
				} else if(dto.contribution.equals(Contribution.MEDIUM)){
					totalContribution += 2;
				} else {
					totalContribution += 3;
				}
			}
			
			for(SuitedToInterviewCategoryDTO dto : request.categories){
				Category category = Category.findById(dto.category.id);
				
				List<enums.Difficulty> difficulties = new ArrayList<enums.Difficulty>();
				for(enums.prep.Difficulty pd : dto.difficulties){
					if(pd.equals(enums.prep.Difficulty.EASY)){
						difficulties.add(enums.Difficulty.EASY);
					} else if(pd.equals(enums.prep.Difficulty.MEDIUM)){
						difficulties.add(enums.Difficulty.MEDIUM);
					} else {
						difficulties.add(enums.Difficulty.HARD);
					}
				}
				
				int contribution = 0;
				if(dto.contribution.equals(Contribution.SMALL)){
					contribution = 1;
				} else if(dto.contribution.equals(Contribution.MEDIUM)){
					contribution = 2;
				} else {
					contribution = 3;
				}
				
				categories.add(new InterviewCategory(
						category,
						(((float)contribution)/((float)totalContribution)) * 100f,
						difficulties));
				
			}
		}
		
		List<Question> blackList = new ArrayList<Question>();
		if(request.blackList != null){
			for(SuitedToQuestionDTO dto : request.blackList){
				Question q = Question.findById(dto.id);
				if(q != null){
					blackList.add(q);
				}
			}
		}
		
		InterviewRequest internalRequest = new InterviewRequest(
				AccessGroup.PREPADO,
				null,
				null,
				blackList,
				interviewDuration,
				categories
				);
		
		List<SuitedToQuestionDTO> results = new ArrayList<SuitedToQuestionDTO>();
		try {
			List<Question> questions = InterviewBuilder.buildInterview(internalRequest).get();
			if(questions != null){
				for(Question question : questions){
					results.add(SuitedToQuestionDTO.fromSuitedToQuestion(question));
				}
			}
			
		} catch (Exception e) {
			Logger.error("Failed to build practice interview: %s", e.getMessage());
			e.printStackTrace();
		}

        // reduce the number of results based on the specified number of questions allowed for a practice interview
        if(!hasPaid && results.size() >= UNPAID_NUMBER_OF_QUESTIONS) {
            results = results.subList(0,UNPAID_NUMBER_OF_QUESTIONS);
        } else if(hasPaid && results.size() >= NUMBER_OF_QUESTIONS) {
            results = results.subList(0,NUMBER_OF_QUESTIONS);
        }

		return results;
	}
}
