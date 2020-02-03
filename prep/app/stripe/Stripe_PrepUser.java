package stripe;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import models.prep.PrepUser;
import play.Logger;
import play.Play;
import play.jobs.Job;

import com.stripe.exception.InvalidRequestException;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Invoice;
import com.stripe.model.Subscription;

import enums.prep.SubscriptionType;
import exceptions.prep.CacheMiss;

/**
 * Binds Prepado user to Stripe Customer
 *
 */
public class Stripe_PrepUser{


	/**
	 * The wrapped prep user should be treated as read-only within
	 * this class.
	 * 
	 * PrepUser is the only model/entity that should be imported
	 * 
	 * To be avoided:
	 *     1) user.xxx = yyy;
	 *     2) user.save();
	 *     3) import models.prep.<class that isn't PrepUser>
	 *     
	 */
	private final PrepUser user;


	private String stripeId;
	private Customer customer;
	
	private static final String stripeApiKey = Play.configuration.getProperty("prep.stripe.private.api-key");

	public Stripe_PrepUser(PrepUser user, String stripeId){
		this.user = user;
		this.stripeId = stripeId;
	}

	private void establishCustomer() throws StripeException{

		if(customer == null){
			customer = Customer.retrieve(stripeId, stripeApiKey);
		}
	}

	/**
	 * Any method that updates something in Stripe should force
	 * the next call to refresh customer
	 */
	private void invalidate(){
		customer = null;
	}

	public StripeResult<String> getLastFour(){

		final StripeResult<String> result = new StripeResult<String>();
		
		new Job(){
			public void doJob(){
				try {
					establishCustomer();
					result.setResult(customer.getCards().retrieve(customer.getDefaultCard(),stripeApiKey).getLast4());
				} catch (StripeException e) {
					result.setException(e);
				}
			}
		}.now();
		return result;
	}

	/**
	 * Changes the subscription type for existing subscriptions
	 * 
	 * @param stripeToken
	 */
	public StripeResult changeSubscriptionType(final SubscriptionType type) {
		final StripeResult result = new StripeResult();
		new Job(){
			public void doJob(){
				try {
					establishCustomer();
					Map<String, Object> options = new HashMap<String, Object>();
					if(SubscriptionType.BASIC.equals(type)){
						options.put("at_period_end", true);
						customer.cancelSubscription(options,stripeApiKey);
					}else if(SubscriptionType.RECURRING.equals(type)){
						options.put("plan", "reoccurring");
						customer.updateSubscription(options,stripeApiKey);
					}
					invalidate();
					result.setResult(null);
				} catch (StripeException e) {
					result.setException(e);
				}
			}
		}.now();
		return result;
	}

	/**
	 * Set the credit card
	 * @param stripeToken
	 */
	public StripeResult setCreditCard(final String stripeToken) {
		final StripeResult result = new StripeResult();
		new Job(){
			public void doJob(){
				try {
					Map<String, Object> customerInfo = new HashMap<String, Object>();
					customerInfo.put("card", stripeToken);
					establishCustomer();
					customer.update(customerInfo,stripeApiKey);
					invalidate();
					result.setResult(null);
				} catch (StripeException e) {
					result.setException(e);
				}
			}
		}.now();
		return result;
	}
	
	/**
	 * Apply coupon to existing customer
	 * 
	 * @param name Name of coupon
	 */
	public StripeResult applyCoupon(final String name) {
		final StripeResult result = new StripeResult();
		new Job(){
			public void doJob(){
				try {
					Map<String, Object> customerInfo = new HashMap<String, Object>();
					customerInfo.put("coupon", name);
					establishCustomer();
					customer.update(customerInfo,stripeApiKey);
					invalidate();
					result.setResult(null);
				} catch (StripeException e) {
					result.setException(e);
				}
			}
		}.now();
		return result;
	}

	public StripeResult<Date> getChargeDate() {
		final StripeResult<Date> result = new StripeResult<Date>();
		new Job(){
			public void doJob(){
				Map<String, Object> invoiceParams = new HashMap<String, Object>();
				invoiceParams.put("customer", stripeId);
				try {
					Invoice invoice = Invoice.upcoming(invoiceParams,stripeApiKey);
					result.setResult(new Date(invoice.getDate()*1000));  // stripe returns date in seconds, have to convert to milliseconds
				} catch (InvalidRequestException e) {
					result.setResult(null);
				} catch (StripeException e) {
					result.setException(e);
				}
			}
		}.now();
		return result;
	}
	
	public StripeResult<Integer> getNextChargeAmount(){
		final StripeResult<Integer> result = new StripeResult<Integer>();
		new Job(){
			public void doJob(){
				Map<String, Object> invoiceParams = new HashMap<String, Object>();
				invoiceParams.put("customer", stripeId);
				try {
					Invoice invoice = Invoice.upcoming(invoiceParams,stripeApiKey);
					result.setResult(invoice.getAmountDue());
				} catch (InvalidRequestException e) {
					result.setResult(null);
				} catch (StripeException e) {
					result.setException(e);
				}
			}
		}.now();
		return result;
	}

	public StripeResult<SubscriptionType> getSubscriptionType(){
		final StripeResult<SubscriptionType> result = new StripeResult<SubscriptionType>();
		new Job(){
			public void doJob(){
				try {
					establishCustomer();
					Subscription s = customer.getSubscription();
					if((s != null) && hasPaid(s).get()){
						result.setResult(s.getCancelAtPeriodEnd()?SubscriptionType.BASIC:SubscriptionType.RECURRING);
					}
					
					result.setResult(SubscriptionType.NONE);

				} catch (StripeException e) {
					result.setException(e);
				}
			}
		}.now();

		return result;

	}

	public StripeResult<Date> getSubscriptionExpiration(){
		final StripeResult<Date> result = new StripeResult<Date>();
		new Job(){
			public void doJob(){
				try {
					establishCustomer();
					Subscription s = customer.getSubscription();
					if((s != null) && hasPaid(s).get() && s.getCancelAtPeriodEnd()){
						result.setResult(new Date(s.getCurrentPeriodEnd() * 1000));
					}
					result.setResult(null);

				} catch (StripeException e) {
					result.setException(e);
				}
			}
		}.now();
		return result;

	}

	public StripeResult<String> getCardType(){
		
		final StripeResult<String> result = new StripeResult<String>();

		new Job(){
			public void doJob(){
				try {
					establishCustomer();
					result.setResult(customer.getCards().retrieve(customer.getDefaultCard(),stripeApiKey).getType());

				} catch (StripeException e) {
					result.setException(e);
				}
			}
		}.now();
		return result;

	}

	public StripeResult<Boolean> hasPaid() {
		return hasPaid(null);

	}

	/**
	 * Determine whether this user is up to date on payment and has full access to the application.
	 * 
	 * @return True if the user has full access. False if payment is required.
	 */
	public StripeResult<Boolean> hasPaid(Subscription subscription) {

		final StripeResult<Boolean> result = new StripeResult<Boolean>();

		final Subscription[] s = new Subscription[1];
		s[0] = subscription;
		new Job(){
			public void doJob(){

				if(stripeId != null) {
					try{
						result.setResult(user.cached().hasPaid().getValue());
					}catch(CacheMiss cm){}
					boolean hasPaid = false;
					try {
						if(s[0] == null){
							Customer customer = Customer.retrieve(stripeId,stripeApiKey);
							s[0] = customer.getSubscription();
						}
						String subscriptionStatus = s[0] != null ? s[0].getStatus() : null;
						hasPaid = subscriptionStatus != null && !subscriptionStatus.equals("past_due") &&
								!subscriptionStatus.equals("unpaid") && !subscriptionStatus.equals("cancelled");
						user.cached().hasPaid().setValue(hasPaid);
						result.setResult(hasPaid);
					} catch (StripeException e) {
						result.setException(e);
					}
					result.setResult(false);
				} else {
					result.setResult(false);
				}
			}
		}.now();
		return result;
	}

	/**
	 * Subscribe the user to the reoccurring payment plan
	 * @param stripeToken represents the credit card
	 * @param coupon optional coupon name
	 * @return stripe id
	 */
	public StripeResult<String> createSubscription(final String stripeToken, final String coupon) {

		final StripeResult<String> result = new StripeResult<String>();
		
		new Job(){
			public void doJob(){
				try {
					if (stripeToken != null) {
						Map<String, Object> customerInfo = new HashMap<String, Object>();
						customerInfo.put("card", stripeToken);
						customerInfo.put("plan", "reoccurring");
						customerInfo.put("email", user.email);
						if(coupon != null){
							customerInfo.put("coupon", coupon);
						}

						customer = Customer.create(customerInfo,stripeApiKey);
						stripeId = customer.getId();

						
						if(coupon != null){
							//I think this is a Stripe bug - have to update with coupon even
							//though we created with coupon. Shouldn't be necessary but discounts
							//aren't applied to recurring charges if this isn't done.
							customerInfo = new HashMap<String, Object>();
							customerInfo.put("coupon", coupon);
							customer.update(customerInfo,stripeApiKey);
						}

					}
					
					result.setResult(stripeId);
				} catch (StripeException e) {
					Logger.error(e.getMessage());
					result.setException(e);
				}
			}
		}.now();
		
		return result;
	}
}