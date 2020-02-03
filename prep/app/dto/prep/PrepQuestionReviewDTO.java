package dto.prep;

import models.prep.PrepQuestionReview;

public class PrepQuestionReviewDTO extends PrepDTO{
	
	public Long id;
	
	public Long prepInterviewReviewId;
	
	public PrepQuestionDTO question;
	
	public String text;

	public static PrepQuestionReviewDTO fromPrepQuestionReview(
			PrepQuestionReview qr) {
		
		PrepQuestionReviewDTO dto = new PrepQuestionReviewDTO();
		if(qr != null){
			dto.id = qr.id;
			dto.prepInterviewReviewId = qr.prepInterviewReview.id;
			dto.text = qr.text;
			dto.question = PrepQuestionDTO.fromPrepQuestion(qr.prepQuestion);
		}
		return dto;
	}

}
