package controllers.prep.rest;

import models.prep.PrepUser;
import play.data.binding.As;
import play.data.validation.Required;
import controllers.prep.access.PrepRestrictedResource;
import controllers.prep.delegates.ErrorHandler;
import data.binding.types.prep.JsonBinder;
import dto.prep.PrepSubscriptionDTO;
import errors.prep.PrepErrorType;

public class Subscriptions extends PrepController {
	
	@PrepRestrictedResource(resourceClassName = "models.prep.PrepUser")
    public static void create(Long id, @Required @As(binder = JsonBinder.class) PrepSubscriptionDTO body) {
		PrepUser user = PrepUser.findById(id);
		if(user == null){
			notFound();
		}
		try{
			user.stripeId = await(user.createSubscriptionAsPromise(body.stripeToken, body.coupon));
			user.save();
			ok();
		} catch (Exception e){
			ErrorHandler.emitCustomErrorResult(PrepErrorType.STRIPE_ERROR, "prep.error.stripe.processingError");
		}
    }
	
	@PrepRestrictedResource(resourceClassName = "models.prep.PrepUser")
    public static void update(Long id, @Required @As(binder = JsonBinder.class) PrepSubscriptionDTO body) {
		PrepUser user = PrepUser.findById(id);
		if(user == null){
			notFound();
		}
		try{
			await(user.changeSubscriptionTypeAsPromise(body.type));
		ok();
		} catch(Exception e){
			ErrorHandler.emitCustomErrorResult(PrepErrorType.STRIPE_ERROR, "Unable to update subscription at this time");
		}
    }

}
