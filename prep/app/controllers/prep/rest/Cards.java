package controllers.prep.rest;

import models.prep.PrepUser;
import play.data.binding.As;
import play.data.validation.Required;
import controllers.prep.access.PrepRestrictedResource;
import controllers.prep.delegates.ErrorHandler;
import data.binding.types.prep.JsonBinder;
import dto.prep.PrepCreditCardDTO;
import errors.prep.PrepErrorType;

public class Cards extends PrepController {
	
	@PrepRestrictedResource(resourceClassName = "models.prep.PrepUser")
    public static void update(Long id, @Required @As(binder = JsonBinder.class) PrepCreditCardDTO body) {
		PrepUser user = PrepUser.findById(id);
		if(user == null){
			notFound();
		}
		try{
			await(user.setCreditCardAsPromise(body.stripeToken));
			ok();
		} catch(Exception e){
			ErrorHandler.emitCustomErrorResult(PrepErrorType.STRIPE_ERROR, "prep.error.stripe.processingError");
		}
    }

}
