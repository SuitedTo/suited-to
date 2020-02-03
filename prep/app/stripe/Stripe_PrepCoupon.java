package stripe;

import java.util.HashMap;
import java.util.Map;

import models.prep.PrepCoupon;
import play.Play;
import play.jobs.Job;

import com.stripe.Stripe;
import com.stripe.exception.InvalidRequestException;
import com.stripe.exception.StripeException;
import com.stripe.model.Coupon;

/**
 * Binds Prepado coupon to Stripe coupon
 * 
 * Allows us to have our own coupon properties/logic while backed by a stripe coupon
 *
 */
public class Stripe_PrepCoupon {

	
	/**
	 * The wrapped prep coupon should be treated as read-only within
	 * this class.
	 * 
	 * PrepCoupon is the only model/entity that should be imported
	 * 
	 * To be avoided:
	 *     1) prepCoupon.xxx = yyy;
	 *     2) prepCoupon.save();
	 *     3) import models.prep.<class that isn't PrepCoupon>
	 *     
	 */
	private PrepCoupon prepCoupon;
	
	
	private Coupon stripeCoupon;
	private String stripeId;
	
	private static final String stripeApiKey = Play.configuration.getProperty("prep.stripe.private.api-key");
	
	public Stripe_PrepCoupon(PrepCoupon prepCoupon) {
		this.prepCoupon = prepCoupon;
		this.stripeId = prepCoupon.name;
	}
	
	private void establishStripeCoupon() throws StripeException{
		if(stripeCoupon == null){
			try{
				stripeCoupon = Coupon.retrieve(stripeId, stripeApiKey);
			}catch(InvalidRequestException e){
				Stripe.apiKey = Play.configuration.getProperty("prep.stripe.private.api-key");
				Map<String, Object> couponParams = new HashMap<String, Object>();
				couponParams.put("percent_off", prepCoupon.discount);
				couponParams.put("max_redemptions", prepCoupon.maxUses);
				couponParams.put("duration", "repeating");
				couponParams.put("duration_in_months", prepCoupon.payPeriods);
				couponParams.put("id", prepCoupon.name);
				stripeCoupon = Coupon.create(couponParams);
			}
		}
		
		
	}
	
	public StripeResult<Integer> getPercentOff(){
		final StripeResult<Integer> result = new StripeResult<Integer>();

		new Job(){
			public void doJob(){
				try {
					establishStripeCoupon();
					result.setResult(stripeCoupon.getPercentOff());
				} catch (StripeException e) {
					result.setException(e);
				}
			}
		}.now();
		return result;
	}
}
