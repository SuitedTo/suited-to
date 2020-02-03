package controllers.prep.rest;

import enums.prep.PrepInterviewReviewStatus;
import models.prep.PrepInterviewReview;
import models.prep.PrepQuestion;
import models.prep.PrepQuestionReview;
import play.data.binding.As;
import play.data.validation.Required;
import controllers.prep.access.PrepRestrictedResource;
import controllers.prep.auth.PublicAction;
import data.binding.types.prep.JsonBinder;
import dto.prep.PrepQuestionReviewDTO;

public class QuestionReviews extends PrepController {
	
	@PublicAction
	public static void create(@Required @As(binder = JsonBinder.class) PrepQuestionReviewDTO body) {

        PrepQuestionReview review = new PrepQuestionReview();
        review.prepInterviewReview = PrepInterviewReview.find.byId(body.prepInterviewReviewId);
        if(review.prepInterviewReview == null){
        	notFound();
        }
        if(!review.hasAccess(null)){
        	forbidden();
        }
        
        PrepQuestion question = PrepQuestion.find.byId(body.question.id);
        if(question == null){
        	forbidden();
        }
        review.prepQuestion = question;
        review.text = body.text;
        review.save();

        if(review.prepInterviewReview.status == PrepInterviewReviewStatus.PENDING) {
            review.prepInterviewReview.status = PrepInterviewReviewStatus.IN_PROGRESS;
            review.prepInterviewReview.save();
        }
    }
	
	@PublicAction
	@PrepRestrictedResource(resourceClassName = "models.prep.PrepQuestionReview")
	public static void update(@Required @As(binder = JsonBinder.class) PrepQuestionReviewDTO body) {

        PrepQuestionReview review = PrepQuestionReview.find.byId(body.id);
        review.text = body.text;
        review.save();
    }

	@PublicAction
	@PrepRestrictedResource(resourceClassName = "models.prep.PrepQuestionReview")
	public static void get(Long id) {
		if (id == null) {
			emptyObject();
		}
		PrepQuestionReview review = PrepQuestionReview.find.byId(id);
		if (review != null) {
			renderRefinedJSON(review.toJsonObject());
		}
		emptyObject();
	}
}
