package dto.suitedto;

import models.Question;

import com.google.gson.Gson;

public class SuitedToQuestionDTO extends SuitedToDTO {
	
	public Long id;

	public String text;
	
	public String answers;

    public String prepAnswers;
	
	public String tips;
	
	public String categoryName;
	
	public static SuitedToQuestionDTO fromSuitedToQuestion(Question question){
		if(question == null){
			return null;
		}
		SuitedToQuestionDTO dto = new SuitedToQuestionDTO();
		dto.id = question.id;
		dto.text = question.text;
		dto.tips = question.tips;
		dto.categoryName = question.category.name;
		dto.answers = question.answers;
        dto.prepAnswers = question.prepAnswers;
		return dto;
	}
	
	public static SuitedToQuestionDTO fromJson(String json){
		try {
	        Gson gson = new Gson();
	        SuitedToQuestionDTO sq = gson.fromJson(json, SuitedToQuestionDTO.class);
	        return sq;
	    }
	    catch(Exception ex) {
	    	ex.printStackTrace();
	        return null;
	    }
	}
}
