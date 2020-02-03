package controllers.prep.rest;

import static play.libs.F.Option.None;
import static play.libs.F.Option.Some;
import controllers.prep.access.Access;
import controllers.prep.access.PrepRestrictedResource;
import controllers.prep.auth.PublicAction;
import controllers.prep.delegates.ErrorHandler;
import data.binding.types.prep.JsonBinder;
import data.binding.types.prep.PageBinder;
import data.binding.types.prep.SearchBinder;
import data.binding.types.prep.SortBinder;
import data.binding.types.prep.PathBinder;
import dto.prep.PrepInterviewCategoryListBuildRequestDTO;
import dto.prep.PrepInterviewCategoryListDTO;
import dto.prep.PrepInterviewDTO;
import dto.prep.PrepInterviewReviewDTO;
import dto.prep.PrepQuestionDTO;
import enums.prep.PrepInterviewReviewStatus;
import errors.prep.PrepError;
import errors.prep.PrepErrorResult;
import errors.prep.PrepErrorType;
import models.Question;
import models.prep.*;
import models.prep.ewrap.Filters;
import models.prep.ewrap.Page;
import models.prep.ewrap.QueryResult;
import models.prep.ewrap.QueryResultBuilder;
import models.prep.ewrap.QueryResultBuilderProvider;
import models.prep.ewrap.Search;
import models.prep.ewrap.Sort;
import models.prep.queries.filters.interviews.Accessible;
import notifiers.prep.Mails;
import play.data.binding.As;
import play.data.validation.Required;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.security.SecureRandom;

import com.avaje.ebean.Expr;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import common.utils.prep.SecurityUtil;
import play.db.jpa.JPA;
import play.libs.F;
import play.libs.F.Option;
import play.mvc.With;

import data.binding.types.prep.FilterBinder;

@With(Access.class)
public class Interviews extends PrepController {
	
	/**
	 * Build the interview while the client waits.
	 * 
	 * @param body
	 */
	public static void create(@As(binder = JsonBinder.class) PrepInterviewCategoryListBuildRequestDTO body){
		
		PrepUser user = SecurityUtil.connectedUser();
		
		//Can't use @RestrictAccess(FreeIfNoOwnedInterviews) on the method because (unfortunately) after each
		//await the befores are called again so after the freebie is created in a background job the check will
		//fail
		if(!user.hasPaid()){
			if(PrepInterview.find.where().and(Expr.eq("owner.id", user.id),Expr.eq("valid", true)).findRowCount() > 0){
				throw new PrepErrorResult(new PrepError(PrepErrorType.ACCESS_ERROR));
			}
		}
		
		PrepInterviewCategoryList categoryList =
				await(new InterviewCategoryListBuilds.BuildJob(null, user, body).now());
		
		PrepInterviewCategoryListDTO categoryListDTO =
				PrepInterviewCategoryListDTO.fromPrepInterviewCategoryList(categoryList);
		
		PrepInterview interview = await(new InterviewBuilds.BuildJob(null, user, categoryListDTO).now());
		if (interview != null) {
			interview.refresh();
			renderRefinedJSON(interview.toJsonObject());
		}
		ErrorHandler.emitCustomErrorResult(PrepErrorType.INVALID_INTERVIEW, "INVALID_INTERVIEW");
	}

    public static void list(
    		@As(binder=PathBinder.class) Option<String> path,
    		@As(binder=FilterBinder.class) Option<Filters> filters,
    		@As(binder=SearchBinder.class) Option<Search> search,
    		@As(binder=SortBinder.class) Option<Sort> sort,
    		@As(binder=PageBinder.class) Option<Page> page) {
    	
    	QueryResultBuilder<PrepInterview> builder =
    			QueryResultBuilderProvider.getQueryResultBuilder(PrepInterview.class);

    	boolean pagedRequest = (page != null) && page.isDefined();
    	PrepUser user = SecurityUtil.connectedUser();
    	if(!user.hasPaid()){
    		Page limited = new Page();
    		limited.index = 0;
    		limited.size = 1;
    		page = Some(limited);
    	}

    	
    	QueryResult<PrepInterview> result = 
    			builder.applyFilter((F.Option) Some(new Accessible<PrepInterview>()), (F.Option) None())
    	    	.applyFilters(filters)
    	    	.applySearch(search)
    	    	.applySort(sort)
    	    	.setPage(page)
    	    	.getResult();
    	
    	if(!user.hasPaid() && !pagedRequest){
    		//didn't ask for a paged result so convert the paged result to a plain
    		//query result
    		result = new QueryResult<PrepInterview>(result.getResultList());
    	}
    	renderRefinedJSON(result.asJson(path));
    }

    @PublicAction
	@PrepRestrictedResource(resourceClassName = "models.prep.PrepInterview")
	public static void get(Long id) {
		if (id == null) {
			emptyObject();
		}
		PrepInterview prepInterview = PrepInterview.find.byId(id);
		if (prepInterview != null) {
			renderRefinedJSON(prepInterview.toJsonObject());
		}
		emptyObject();
	}

    @PrepRestrictedResource(resourceClassName = "models.prep.PrepInterview")
    public static void listReviews(Long id) {
        List<PrepInterviewReview> reviews = PrepInterviewReview.find.where().eq("prepInterview.id", new Long(id)).findList();

        JsonArray result = new JsonArray();
        for(PrepInterviewReview review : reviews){
            PrepInterviewReviewDTO dto = PrepInterviewReviewDTO.fromPrepInterviewReview(review);
            result.add(dto.toJsonTree());
        }

        renderRefinedJSON(result);
    }

    // TODO: ACCESS control
    public static void createReview(@Required @As(binder = JsonBinder.class) PrepInterviewReviewDTO body, Long id) {

        if(PrepInterviewReview.find.where().and(
                Expr.eq("prepInterview.id", body.prepInterview.id),
                Expr.eq("reviewerEmail", body.reviewerEmail))
                .findList().size() > 0){
            ErrorHandler.emitCustomErrorResult(PrepErrorType.DATA_VALIDATION_ERROR, "You have already requested a review from " + body.reviewerEmail + ".");
        }

        PrepInterviewReview review = new PrepInterviewReview();

        review.prepInterview = PrepInterview.findById(body.prepInterview.id);
        review.status = PrepInterviewReviewStatus.PENDING;
        review.reviewerEmail = body.reviewerEmail;

        SecureRandom random = new SecureRandom();

        byte[] bytes = new byte[4];
        random.nextBytes(bytes);

        long value = 0;
        for (int i = 0; i < 4; i++) {
            value += ((long) bytes[i] & 0xffL) << (8 * i);
        }

        review.reviewKey = Integer.toString((int) value);

        review.save();

        Mails.reviewRequest(review, body.message);
    }
	
    @PublicAction
	public static void getReview(Long id, String reviewKey) {
		if (id == null) {
			emptyObject();
		}
		PrepInterview interview = PrepInterview.find.byId(id);
		if (interview != null) {
			PrepInterviewReview review = PrepInterviewReview.find.where().
					and(Expr.eq("prepInterview.id", interview.id),Expr.eq("reviewKey", reviewKey)).findUnique();
			if(review == null){
				emptyObject();
			}
			renderRefinedJSON(review.toJsonObject());
		}
		emptyObject();
	}
    
    @PublicAction
    @PrepRestrictedResource(resourceClassName = "models.prep.PrepInterviewReview")
	public static void updateReview(@Required @As(binder = JsonBinder.class) PrepInterviewReviewDTO body) {
		if (body.id == null) {
			notFound();
		}
		PrepInterviewReview review = PrepInterviewReview.find.byId(body.id);
		if (review != null) {
			
			if((PrepInterviewReviewStatus.COMPLETED != review.status) &&
					((PrepInterviewReviewStatus.COMPLETED == body.status))){
				Mails.feedbackReceived(review);
			}
			review.status = body.status; //this is all we're updating right now...
			review.save();
			renderRefinedJSON(review.toJsonObject());
		}
		notFound();
	}

	@PrepRestrictedResource(resourceClassName = "models.prep.PrepInterview")
	public static void update(@Required @As(binder = JsonBinder.class) PrepInterviewDTO body) {
		
		PrepInterview prepInterview = PrepInterview.find.byId(body.id);
		prepInterview.name = body.name;
        prepInterview.currentQuestion = body.currentQuestion;

        /*
        * This is a strong hack zone. We need to support retaking an interview which means leaving the same set of questions
        * in the same order but clearing out any text or video answers. Because EBean won't make updates for missing fields
        * we can't just pass in the questions with null answer or videoId so we have to do some manual work.
        *
        * The InterviewDTO exposes a "reset" property which the client can set to true. If reset is true then we will
        * need to "clone" all of the questions minus any answer fields and repopulate the Interview with those new
        * unanswered questions.
        *
        * One further complication is that deleting FFMpegJobRequest which has multiple relationships to FFMpegFile
        * cannot be handled automatically by Ebean. The delete method on FFMpegJobRequest is overridden to handle the
        * deletion of its associated FFMpegFile instances but we need to call FFMpegJobRequest.delete() explicitly in
        * order for that overridden delete() method to be invoked.
        * */
        if(body.reset){
            List<PrepQuestion> newQuestions = new ArrayList<PrepQuestion>();
            for (PrepQuestion question : prepInterview.questions) {
                //clone the question
                PrepQuestion newQuestion = new PrepQuestion();
                newQuestion.categoryName = question.categoryName;
                newQuestion.questionId = question.questionId;
                newQuestion.staticAnswers = question.staticAnswers;
                newQuestion.text = question.text;
                newQuestion.tips = question.tips;
                newQuestions.add(newQuestion);

                //now delete the old question
                question.delete();
            }

            prepInterview.questions = newQuestions;
            prepInterview.currentQuestion = 1;
        }

		prepInterview.save();

		renderRefinedJSON(body.toJsonTree());
	}

    /**
     * Send the reviewer another email reminding them
     * to review an interviews
     *
     * @param id
     */
    public static void resendEmail(Long id) {
        PrepInterviewReview review = PrepInterviewReview.findById(id);
        if(id != null) {
            Mails.reviewRequest(review, null);
        }
    }
    public static void deleteReview(String reviewKey) {
        if(reviewKey != null) {
            PrepInterviewReview review = PrepInterviewReview.find.where().eq("reviewKey", reviewKey).findUnique();
            if(review != null && review.status.name() == "PENDING") {
                review.delete();
            }
        }
    }
}
