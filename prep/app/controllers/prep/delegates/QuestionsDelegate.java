package controllers.prep.delegates;

import javax.persistence.NoResultException;

import dto.suitedto.SuitedToQuestionDTO;
import models.Question;

public class QuestionsDelegate {
	
	public static SuitedToQuestionDTO get(Long id){
		
		if(id == null){
			return null;
		}
		
		try{
			Question q = (Question)Question.findById(id);
			return SuitedToQuestionDTO.fromSuitedToQuestion(q);
		}catch(NoResultException nre){
			return null;
		}
	}
}
