package controllers.prep.rest;

import common.utils.prep.SecurityUtil;

import models.prep.PrepCoupon;
import models.prep.PrepUser;
import play.Play;
import play.data.binding.As;
import play.data.validation.Required;
import controllers.prep.access.PrepRestrictedResource;
import controllers.prep.delegates.ErrorHandler;
import data.binding.types.prep.JsonBinder;
import dto.prep.PrepCouponDTO;
import dto.prep.PrepDiscountDTO;
import dto.prep.PrepSubscriptionDTO;
import errors.prep.PrepError;
import errors.prep.PrepErrorResult;
import errors.prep.PrepErrorType;

/*
 * A discount is the application of a coupon to a user account
 */
public class Discounts extends PrepController {
	
    public static void create(Long id, @Required @As(binder = JsonBinder.class) PrepCouponDTO body) {
    	
    	PrepCoupon coupon = (PrepCoupon) PrepCoupon.find.where().eq("name", body.name).findUnique();
		if(coupon == null){
			ErrorHandler.emitCustomErrorResult(PrepErrorType.STRIPE_ERROR, "Invalid coupon code.");
		}
		Integer percentOff = null;
		try{
			percentOff = await(coupon.getPercentOffAsPromise());
		} catch(Exception e){
			ErrorHandler.emitCustomErrorResult(PrepErrorType.STRIPE_ERROR, "Invalid coupon code.");
		}
		PrepDiscountDTO discount = new PrepDiscountDTO();
		float baseCharge = Float.parseFloat(Play.configuration.getProperty("prep.stripe.base-charge"));
		discount.resultingPrice = (int)(baseCharge - baseCharge*((float)(percentOff)/100f));
		discount.coupon = body.name;
		if(id != null){
			PrepUser user = PrepUser.findById(id);
			if(user == null){
				notFound();
			}
		
			PrepUser connectedUser = SecurityUtil.connectedUser();
			if(!user.hasAccess(connectedUser)){
				throw new PrepErrorResult(new PrepError(PrepErrorType.ACCESS_ERROR));
			}
		
			try{
				await(user.applyCouponAsPromise(body.name));
			} catch(Exception e){
				ErrorHandler.emitCustomErrorResult(PrepErrorType.STRIPE_ERROR, "Invalid coupon code.");
			}
		}
		
		renderRefinedJSON(discount.toJsonTree());
    }

}

