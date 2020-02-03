package utils;

import cache.KeyBuilder;
import com.stripe.Stripe;
import com.stripe.exception.*;
import com.stripe.model.*;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import controllers.Security;
import enums.AccountType;
import enums.CompanyAccountStatus;
import enums.RoleValue;
import jobs.MultiNodeRunnable;
import models.Company;
import models.User;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.Logger;
import play.Play;
import play.cache.Cache;
import play.jobs.Every;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import play.libs.F.Promise;

import java.util.*;
import java.util.concurrent.Callable;


/********************************************************************
 * Utility Methods for Interacting with the Stripe payment system   *
 ********************************************************************/
public class StripeUtil {

	private static XStream xstream = new XStream(new StaxDriver());
	
	private StripeUtil(){}

    /*
    Statically define the Stripe API keys and version information.  This version will override the default that is set
    on the account in the Stripe.com account page.
    */
    static {
        Stripe.apiKey = Play.configuration.get("stripe.secret_key").toString();
        Stripe.apiVersion = "2013-08-13";
    }

    /**
     * Looks up an existing Card record from the Stripe system for the company.  If the Company account type is FREE
     * the lookup is not performed since the card is not currently being charged.
     * @param company Company to check for an existing Card
     * @return Card record for the company
     */
    public static Card getExistingActiveCard(Company company){

        Card card = null;
        /*Get the previously entered card data unless the current account type is FREE. There may actually be a case
        * where someone switches from a paid to a free account where we actually do have card information in the
        * payment system but it'll likely freak them out to see their old card reappearing so don't display that and
        * make them reenter*/
        if(company != null && !AccountType.FREE.equals(company.accountType)){
            try {
                Customer customer = Customer.retrieve(company.paymentSystemKey);
                if(customer != null && customer.getDefaultCard() != null){
                    card = customer.getCards().retrieve(customer.getDefaultCard());
                }
            } catch (StripeException e) {
                Logger.error("Unable to retrieve token data from Stripe API. " + e.getMessage(), e);
            }
        }

        return card;
    }


    /**
     * Looks up a Card record matching the Stripe token.  This is useful for re-displaying a redacted version of the
     * card data after server-side validation errors.
     * @param stripeToken String version of a Stripe Token - if present should match a Token record in the Stripe
     *                    system from which we can gather the card information.
     * @return Card if stripeToken is not null
     */
    public static Card getCardFromToken(String stripeToken){
        if(StringUtils.isEmpty(stripeToken)){
            return null;
        }

        Card card = null;
        try {
            Token token =  Token.retrieve(stripeToken);
            if(token != null) {
                card = token.getCard();

            }
        } catch (StripeException e) {
            Logger.error("Unable to retrieve token data from Stripe API." + e.getMessage(), e);
        }

        return card;
    }

    /**
     * Gets the correct card data to be used when redisplaying the manage page after a server-side validation failure.
     * First checks for an updated card, then if none is present tries to get the card data off the Customer record.
     * It is possible that this will return null if a card has not been established for the Company and not entered on
     * the page.
     * @param stripeToken String version of a Stripe token
     * @param company Company to check for an existing Card
     * @return Card or null if no appropriate card information found
     */
    public static Card getCardDataForPageRedisplay(String stripeToken, Company company){
        Card card = getCardFromToken(stripeToken);
        if(card == null){
            card = getExistingActiveCard(company);
        }

        return card;
    }

    /**
     * Updates the Customer record in the Stripe payment system to correspond with changes to the Company record in the
     * SuitedTo system.
     * @param company Company to be updated
     * @param stripeToken Matches a card record in the Stripe system.  Its presence indicates updated credit card
     *                    information. If this argument is null the card will not be updated on the customer record.
     */
    public static boolean updatePaymentSystemData(Company company, String stripeToken, String couponCode){
        if(company.paymentSystemKey == null){
        	try{
        		createNewPaymentSystemCustomer(company, stripeToken, couponCode);
        	}catch(Exception e){
				Logger.error("unable to update customer account type in Stripe", e.getMessage());
        		return false;
        	}
        } else {
            try {
                //update basic customer information
                Customer stripeCustomer = Customer.retrieve(company.paymentSystemKey);
                Map<String, Object> apiParams = new HashMap<String, Object>();
                apiParams.put("description", "SuitedTo - " + company.name);
                User defaultUser = company.getDefaultAdminUser();
                if(defaultUser != null){
                    apiParams.put("email", defaultUser.email);
                }

                if (StringUtils.isNotEmpty(stripeToken)) {
                    apiParams.put("card", stripeToken);
                    storeLastFourDigits(company, stripeToken);
                }

                //if the current user is an application admin update the couponCode, otherwise just leave it as-is
                final User connectedUser = Security.connectedUser();
                if (connectedUser != null && connectedUser.hasRole(RoleValue.APP_ADMIN)) {
                    if(!StringUtils.isBlank(couponCode)){
                        apiParams.put("coupon", couponCode);
                    }
                }

                stripeCustomer.update(apiParams);


                //update subscription information. see https://stripe.com/docs/api/java#update_subscription
                Map<String, Object> subscriptionParams = new HashMap<String, Object>();
                subscriptionParams.put("plan", Play.configuration.getProperty("stripe.plan_id." + company.accountType.name()));

                boolean providingPaymentAfterExpiredTrial = company.trialExpiration != null && StringUtils.isNotEmpty(stripeToken);

                if(company.trialExpiration != null && company.trialExpiration.after(new Date())){
                    long timestamp = company.trialExpiration.getTime() / 1000; //convert to seconds
                    subscriptionParams.put("trial_end", timestamp);
                } else if(providingPaymentAfterExpiredTrial){     //company's trial has expired and they've provided a new card
                    subscriptionParams.put("trial_end", "now");
                    subscriptionParams.put("prorate", "false");
                }
                stripeCustomer.updateSubscription(subscriptionParams);

                //if we've just applied the payment after an expired trial then adjust the trial expiration date to one month in the future
                if(providingPaymentAfterExpiredTrial){
                    subscriptionParams.clear();
                    subscriptionParams.put("plan", Play.configuration.getProperty("stripe.plan_id." + company.accountType.name()));
                    subscriptionParams.put("trial_end", new DateTime().plusMonths(1).toDate().getTime() / 1000);
                    subscriptionParams.put("prorate", "false");
                    stripeCustomer.updateSubscription(subscriptionParams);
                }

            } catch (StripeException e) {
                Logger.error(e.getMessage());
                Logger.error("unable to update customer information in Stripe", e);
                return false;
            }
        }
        return true;
    }

    /**
     * Creates a new Customer based on provided information
     *
     * @param company       the company data. the object may be modified by this method,
     *                      but it is not responsible for persisting changes
     * @param stripeToken   credit card data
     * @param couponCode    coupon code
     */
    public static void createNewPaymentSystemCustomer(Company company, String stripeToken, String couponCode)  {
        final User connectedUser = Security.connectedUser();
        AccountType accountType = company.accountType;
        Map<String, Object> customerData = new HashMap<String, Object>();
        if(StringUtils.isNotEmpty(stripeToken)) {
            customerData.put("card", stripeToken);
            storeLastFourDigits(company, stripeToken);
        }

        //can't use the default admin user because company hasn't been saved yet, but if the logged in user is a compoany
        //admin use his email address as the company contact email
        if(connectedUser != null && connectedUser.hasRole(RoleValue.COMPANY_ADMIN)){
            customerData.put("email", connectedUser.email);
        }

        customerData.put("plan", Play.configuration.get("stripe.plan_id." + accountType.name()));
        if (connectedUser != null && connectedUser.hasRole(RoleValue.APP_ADMIN)) {
            if(!StringUtils.isEmpty(couponCode)){
                customerData.put("coupon", couponCode);
            }
        }
        if(StringUtils.isNotEmpty(company.contactEmail)){
            customerData.put("email", company.contactEmail);
        }
        customerData.put("description", "SuitedTo - " + company.name);

        if(company.trialExpiration != null){
            long timestamp = company.trialExpiration.getTime() / 1000; //convert to seconds
            customerData.put("trial_end", timestamp);
        }

        try {
            Customer stripeCustomer = Customer.create(customerData);
            company.paymentSystemKey = stripeCustomer.getId();
        } catch (StripeException e) {
            Logger.error("Unable to create customer in Stripe API", e);
            throw new RuntimeException(e);
        }
    }
    
    public static List<Coupon> fastGetAvailableCoupons(){
    	Object cached = Cache.get(KeyBuilder.buildGlobalKey(StripeUtil.class.getName() + "coupons"));
    	if(cached != null){
    		List<CouponData> coupons = (List)xstream.fromXML((String)cached);
    		List<Coupon> rtn = new ArrayList<Coupon>();
    		if(coupons.size() > 0){
    			
    			for(CouponData cd : coupons){
    				Coupon c = new Coupon();
    				c.setDuration(cd.getDuration());
    				c.setId(cd.getId());
    				c.setLivemode(cd.getLivemode());
    				c.setPercentOff(cd.getPercentOff());
    				rtn.add(c);
    			}
    		}
    		return rtn;
    	}
    	return getAvailableCoupons();
    }

    public static List<Coupon> getAvailableCoupons(){
        Stripe.apiKey = Play.configuration.get("stripe.secret_key").toString();
        try {
            CouponCollection allCoupons = Coupon.all(new HashMap<String, Object>());
            return allCoupons.getData();
        } catch (StripeException e) {
            Logger.error("Unable to retrieve available coupon codes", e.getMessage());
        }
        return null;
    }

    public static String getExistingCouponCode(Company company){
        if(company == null){
            return null;
        }
        try {
            Customer stripeCustomer = Customer.retrieve(company.paymentSystemKey);
            Discount discount = stripeCustomer.getDiscount();
            if (discount != null){
                Coupon coupon = discount.getCoupon();
                if(coupon != null){
                    return coupon.getId();
                }
            }
        } catch (StripeException e) {
            Logger.error("Unable to retrieve Stripe customer", e.getMessage());
        }
        return null;
    }

    /**
     * Get the Stipe Customer associated with the given company.  Will log an error message and return null if no
     * Customer is associated with the company
     * @param company
     * @return
     */
    public static Customer getCustomer(Company company) {
        if(company == null){
            return null;
        }

        try {
            return Customer.retrieve(company.paymentSystemKey);
        } catch (StripeException e) {
            Logger.error("Unable to retrieve Stripe customer", e.getMessage());
        }

        return null;
    }

    public static boolean is100PercentDiscount(Customer stripeCustomer){
        //check for 100% off coupon before adding error for trial expired
        boolean is100PercentOff = false;
        Discount discount = stripeCustomer.getDiscount();
        if (discount != null){
            Coupon coupon = discount.getCoupon();
            is100PercentOff = coupon != null && new Integer(100).equals(coupon.getPercentOff());
        }

        return is100PercentOff;
    }

    public static boolean isSubscriptionInGoodStanding(Customer stripeCustomer){
        Subscription subscription = stripeCustomer.getSubscription();
        String subscriptionStatus = subscription != null ? subscription.getStatus() : null;
        return subscriptionStatus != null && !subscriptionStatus.equals("past_due") &&
                !subscriptionStatus.equals("unpaid") && !subscriptionStatus.equals("cancelled");
    }

    public static Card getDefaultCard(Customer stripeCustomer) {
        if(stripeCustomer.getDefaultCard() != null){
            try {
                return stripeCustomer.getCards().retrieve(stripeCustomer.getDefaultCard());
            } catch (StripeException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        return null;
    }
    
    /*****************************************************************************
     * Promised Utility Methods for Interacting with the Stripe payment system   *
     *****************************************************************************/
    
    public static Promise<Card> getExistingActiveCardAsPromise(final Company company){
    	return new Job<Card>(){
    		public Card doJobWithResult(){
    			return getExistingActiveCard(company);
    		}
    	}.now();
    }
    
    public static Promise<Card> getCardFromTokenAsPromise(final String stripeToken){
    	return new Job<Card>(){
    		public Card doJobWithResult(){
    			return getCardFromToken(stripeToken);
    		}
    	}.now();
    }
    
    public static Promise<Card> getCardDataForPageRedisplayAsPromise(final String stripeToken, final Company company){
    	return new Job<Card>(){
    		public Card doJobWithResult(){
    			return getCardDataForPageRedisplay(stripeToken, company);
    		}
    	}.now();
    }
    
    public static Promise<Boolean> updatePaymentSystemDataAsPromise(final Company company, final String stripeToken, final String couponCode){
    	return JobUtil.asJobInContext(new Callable<Boolean>(){
    		public Boolean call(){
    			return updatePaymentSystemData(company, stripeToken, couponCode);
    		}
    	}).now();
    }
    
    public static Promise createNewPaymentSystemCustomerAsPromise(final Company company, final String stripeToken, final String couponCode){
    	return new Job(){
    		public void doJob(){
    			createNewPaymentSystemCustomer(company, stripeToken, couponCode);
    		}
    	}.now();
    }
    
    public static Promise<List<Coupon>> fastGetAvailableCouponsAsPromise(){
    	return new Job<List<Coupon>>(){
    		public List<Coupon> doJobWithResult(){
    			return fastGetAvailableCoupons();
    		}
    	}.now();    	
    }
    
    public static Promise<List<Coupon>> getAvailableCouponsAsPromise(){
    	return new Job<List<Coupon>>(){
    		public List<Coupon> doJobWithResult(){
    			return getAvailableCoupons();
    		}
    	}.now();    	
    }
    
    public static Promise<String> getExistingCouponCodeAsPromise(final Company company){
    	return new Job<String>(){
    		public String doJobWithResult(){
    			return getExistingCouponCode(company);
    		}
    	}.now();
    }

    public static Promise<Customer> getCustomerAsPromise(final Company company){
        return new Job<Customer>(){
            public Customer doJobWithResult(){
                return getCustomer(company);
            }
        }.now();
    }

    public static Promise<Card> getDefaultCardAsPromise(final Customer stripeCustomer){
        return new Job<Card>(){
            public Card doJobWithResult(){
                return getDefaultCard(stripeCustomer);
            }
        }.now();
    }
    
    @OnApplicationStart(async=true)
    @Every("15min")
    public class UpdateCachedCoupons extends Job{
    	public void doJob(){
    		new MultiNodeRunnable("UpdateCachedCoupons",
	    			new Runnable(){
	    				public void run(){
	    						try {
	    							List<Coupon> coupons = getAvailableCoupons();
	    							if((coupons != null) && (coupons.size() > 0)){
	    								List<CouponData> couponDataList = new ArrayList<CouponData>();
	    								for(Coupon c : coupons){
	    									couponDataList.add(new CouponData(c.getPercentOff(),
	    											c.getDuration(),
	    											c.getId(),
	    											c.getLivemode()));
	    								}
	    								Cache.add(KeyBuilder.buildGlobalKey(StripeUtil.class.getName() + "coupons"),
	    										xstream.toXML(couponDataList));
	    							}
	    							
	    						} catch (Exception e) {
	    							Logger.error(e, "Failed to update cached coupons: %s", e.getMessage());
	    						}
	    	}}).run();
    	}
    }
    
    public final static class CouponData{
    	private final int percentOff;
    	private final String duration;
    	private final String id;
    	private final boolean livemode;
    	
		public CouponData(Integer percentOff, String duration, String id,
				boolean livemode) {
			this.percentOff = percentOff;
			this.duration = duration;
			this.id = id;
			this.livemode = livemode;
		}
		public Integer getPercentOff() {
			return percentOff;
		}
		public String getDuration() {
			return duration;
		}
		public String getId() {
			return id;
		}
		public boolean getLivemode() {
			return livemode;
		}
    }

   /*
   * Stores the last 4 digits of a company credit card. This is useful for
   * determining if the company is on a free trial with SuitedTo or a current
   * paying customer
   * @param: company
   * @param: stripeToken: the key to grab the card information for a particular company from stripe
   * */
    private static void storeLastFourDigits(Company company, String stripeToken) {
        Card creditCard = getCardFromToken(stripeToken);
        company.lastFourCardDigits = creditCard.getLast4();
    }
}
