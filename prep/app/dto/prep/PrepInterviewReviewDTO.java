package dto.prep;

import java.util.ArrayList;
import java.util.List;

import enums.prep.PrepInterviewReviewStatus;
import models.prep.PrepInterviewReview;
import models.prep.PrepQuestionReview;

public class PrepInterviewReviewDTO extends PrepDTO{
	
	public Long id;

	public PrepInterviewReviewStatus status;
	
	public List<PrepQuestionReviewDTO> questions = new ArrayList<PrepQuestionReviewDTO>();

    public String reviewerEmail;

    public String message;

    public PrepInterviewDTO prepInterview;

    public String reviewKey;

    public String interviewerName;

    public static PrepInterviewReviewDTO fromPrepInterviewReview(
			PrepInterviewReview pi) {
		PrepInterviewReviewDTO dto = new PrepInterviewReviewDTO();
		if(pi != null){
			dto.id = pi.id;
			dto.status = pi.status;
            dto.reviewerEmail = pi.reviewerEmail;
            dto.reviewKey = pi.reviewKey;
			if(pi.questions != null){
				for(PrepQuestionReview q : pi.questions){
					dto.questions.add(PrepQuestionReviewDTO.fromPrepQuestionReview(q));
				}
			}

            dto.interviewerName = pi.prepInterview.owner.firstName != null ? pi.prepInterview.owner.firstName : pi.prepInterview.owner.email;
        }
		return dto;
	}

}
