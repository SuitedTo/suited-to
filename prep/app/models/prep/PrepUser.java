package models.prep;

import static javax.persistence.CascadeType.REMOVE;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.stripe.model.Invoice;
import com.stripe.model.Subscription;
import models.PrepRestrictable;
import modules.ebean.Finder;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.mindrot.jbcrypt.BCrypt;

import play.Logger;
import play.Play;
import play.cache.Cache;
import play.data.validation.Email;
import play.jobs.Job;
import play.libs.Crypto;
import play.libs.F.Promise;
import play.libs.Mail;
import play.libs.OAuth.ServiceInfo;
import play.libs.WS;
import play.libs.WS.HttpResponse;
import play.libs.WS.WSRequest;
import play.mvc.Http;
import stripe.Stripe_PrepUser;
import stripe.StripeResult;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Expr;
import com.avaje.ebean.Update;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializer;
import com.stripe.Stripe;
import com.stripe.exception.APIConnectionException;
import com.stripe.exception.APIException;
import com.stripe.exception.AuthenticationException;
import com.stripe.exception.CardException;
import com.stripe.exception.InvalidRequestException;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;

import controllers.prep.delegates.ErrorHandler;
import enums.prep.SubscriptionType;
import errors.prep.PrepErrorType;
import exceptions.LoginException;
import exceptions.prep.CacheMiss;
import exceptions.prep.CouponNotAvailableException;

@Entity
@Table(name = "PREP_User")
public class PrepUser extends EbeanModelBase implements PrepRestrictable {

    /*****************************************************
     * Static Fields and Enums                           *
     *****************************************************/

    public enum AuthProvider {
        FACEBOOK, GOOGLE, LINKEDIN;
    }

    public enum RoleValue {
        STANDARD, ADMIN
    }


    /*****************************************************
     * Basic Identity and Auth Fields                    *
     *****************************************************/

    //@Unique TODO: Figure something out here...broken with the move to ebean
    @Email
    public String email;

    public String password;

    public String tempPassword;

    public Date tempExpire;

    public String stripeId;

    /**
     * Used to "salt" the user's password, such that even if two users have the same password string the hashed value
     * that is stored will still be unique.
     */
    public String salt = BCrypt.gensalt();

  //@Unique TODO: Figure something out here...broken with the move to ebean
    @Column(name = "external_auth_provider_id")  
    public String externalAuthProviderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "external_auth_provider")  
    public AuthProvider externalAuthProvider;

    /**
     * Timestamp of when the user last logged in to the application
     */
    @Temporal(TemporalType.TIMESTAMP)
    public Date lastLogin;

    /***********************************************************************************
     * Relationships to Child Entities                                                 *
     ***********************************************************************************/

    @OneToMany(mappedBy = "owner", cascade = REMOVE)
    private List<PrepInterview> interviews;

    @OneToMany(mappedBy = "prepUser", cascade = REMOVE)
    private List<PrepUserRole> prepUserRole = new ArrayList<PrepUserRole>();

    @OneToMany(mappedBy = "prepUser", cascade = REMOVE)
    private List<PrepCouponChargeHistory> prepCouponChargeHistory = new ArrayList<PrepCouponChargeHistory>(); //TODO: remove
    
    @OneToMany(mappedBy="user", cascade = REMOVE)
	private List<PrepClientSession> sessions;
    
    
    /*****************************************************
     * Transient Fields                   *
     *****************************************************/
    
    private transient Stripe_PrepUser stripeCustomer;


    /*****************************************************
     * Demographics and Display Fields                   *
     *****************************************************/

    /**
     * The User's first name, this will be null in many cases. As it's typically just pulled in from a 3rd party
     * authenication provider and not set through the front end.
     */
    @Column(name = "first_name")  
    public String firstName;

    /**
     * URL to a profile picture for this user, may point to an external website or prep application S3 url, should always
     * represent a publicly accessible image url if not null.
     */
    public String profilePictureUrl;
    
    
    public static Finder<Long,PrepUser> find = new Finder<Long,PrepUser>(
            Long.class, PrepUser.class
    );

    /*****************************************************
     * Authorization and Authentication Stuff            *
     *****************************************************/

    public static PrepUser connect(String externalId, AuthProvider provider, String accessToken, String accessTokenSecret, String emailAddressForExternalProviderAccount){
    	
    	if(externalId == null){
    		throw new LoginException("could not log in without an external id");
    	}
    	if(provider == null){
    		throw new LoginException("could not log in without a specified provider");
    	}
    	if(accessToken == null){
    		throw new LoginException("could not log in without an access token");
    	}
    	
        PrepUser user = PrepUser.find.where().and(Expr.eq("externalAuthProviderId",externalId), Expr.eq("externalAuthProvider", provider)).findUnique();

        if(user == null){
        	if(emailAddressForExternalProviderAccount != null){
        		PrepUser conflictingUser = PrepUser.find.where().eq("email", emailAddressForExternalProviderAccount).findUnique();
        		if (conflictingUser != null){
        			if(conflictingUser.externalAuthProvider != null){
        				throw new ConflictingUserException(provider, conflictingUser.externalAuthProvider);
        			} else {
        				throw new ConflictingUserException(provider, null);
        			}
        		}
        	}
        	throw new LoginException(PrepErrorType.NO_SUCH_USER_ERROR, "The given email address is not associated with a user.");
        }
        
        switch(provider){
        	case FACEBOOK:
        		checkFacebookAccess(externalId, accessToken);
        		break;
        	case LINKEDIN:
        		checkLinkedInAccess(externalId, accessToken, accessTokenSecret);
        		break;
        	case GOOGLE:
        		checkGoogleAccess(externalId, accessToken);
        		break;
        	default:
        		throw new LoginException("could not log in with unsupported provider: " + provider.name());
        }

        return user;
    }
    
    private static void checkFacebookAccess(String externalId, String accessToken){
    	
    	WSRequest request = WS.url("https://graph.facebook.com/me")
    			.setParameter("fields", "id")
    			.setParameter("access_token",accessToken);
        HttpResponse response = request.get();
        JsonObject responseJson = response.getJson().getAsJsonObject();
        if ( (response.getStatus() != 200) || !responseJson.has("id") ) {
            throw new LoginException("could not log in using an invalid Facebook session");
        }
    }
    
    private static void checkLinkedInAccess(String externalId, String accessToken, String accessTokenSecret){
    	if(StringUtils.isEmpty(accessTokenSecret)){
    		checkLinkedInAccessOauth2(externalId, accessToken);
    	} else {
    		//Supporting Ouath1 right now because linkedin doesn't seem to give
    		//us a way to exchange a bearer token for an oauth2 access token so our
    		//browser clients authenticate with oauth1 at this time.
    		checkLinkedInAccessOauth1(externalId, accessToken, accessTokenSecret);
    	}
    }
    
    private static void checkLinkedInAccessOauth2(String externalId, String accessToken){
    	WSRequest request = WS.url("https://api.linkedin.com/v1/people/~:(lastName)")
    			.setParameter("oauth2_access_token",accessToken)
    			.setHeader("x-li-format", "json");
    	HttpResponse response = request.get();
    	JsonObject responseJson = response.getJson().getAsJsonObject();
        if ( (response.getStatus() != 200) || !responseJson.has("lastName") ) {
        	//System.out.println(responseJson.toString());
            throw new LoginException("could not log in using an invalid LinkedIn session");
        }
    }
    
    private static void checkLinkedInAccessOauth1(String externalId, String accessToken, String accessTokenSecret){
    	
    	WSRequest request = WS.url("https://api.linkedin.com/v1/people/~:(lastName)");
    	request.setHeader("Content-Type", "application/json");
		request.setHeader("x-li-format", "json");
		
        ServiceInfo svcInfo = new ServiceInfo(
        		"https://api.linkedin.com/uas/oauth/requestToken",
        		"https://api.linkedin.com/uas/oauth/accessToken",
        		"https://api.linkedin.com/uas/oauth/authenticate",
        		play.Play.configuration.getProperty("prep.linkedin.api-key"),
				play.Play.configuration.getProperty("prep.linkedin.api-secret"));
        
        request = request.oauth(svcInfo, accessToken, accessTokenSecret);
		
        HttpResponse response = request.get();
        JsonObject responseJson = response.getJson().getAsJsonObject();
        if ( (response.getStatus() != 200) || !responseJson.has("lastName") ) {
        	//System.out.println(responseJson.toString());
            throw new LoginException("could not log in using an invalid LinkedIn session");
        }
    }
    
    private static void checkGoogleAccess(String externalId, String accessToken){
    	
    	WSRequest request = WS.url("https://www.googleapis.com/oauth2/v2/userinfo")
    			.setParameter("access_token",accessToken);
        HttpResponse response = request.get();
        JsonObject responseJson = response.getJson().getAsJsonObject();
        if ( (response.getStatus() != 200) || !responseJson.has("id") ) {
            throw new LoginException("could not log in using an invalid Google session");
        }
    }

    /**                                                                                                                                                 ˜
     * Checks if a user matching the given credentials should be allowed to access the application.
     * @param email Email address for log in
     * @param password The password
     * @return The user matching the given credentials or null if no match is found or the user's status does not allow
     * login
     */
    public static PrepUser connect(String email, String password) {
        LoginException standardException =
                new LoginException("We didn't recognize the username or password you entered. Please try again.");

        //one or both credentials missing
        if (StringUtils.isEmpty(email) || StringUtils.isEmpty(password)){
            throw new LoginException("Please enter an email address and password.");
        }

        //No matching user found
        PrepUser user = PrepUser.find.where().eq("email", email).findUnique();
        if(user == null){
            throw new LoginException(PrepErrorType.NO_SUCH_USER_ERROR, "The given email address is not associated with a user.");
        }

        //check password
        boolean validPassword = false;
        if (user.password != null && user.password.equals(user.hashPassword(password))) {
            validPassword = true;
        }

        //todo: forgotten password stuff
        //allow the user to login with a temporary, system generated password if one exists and is not expired
        if (user.tempPassword != null && user.tempPassword.equals(user.hashPassword(password)) && !user.passwordExpired()) {
            //throw exception
            throw new LoginException("CHANGE_PASSWORD");
        }

        if(!validPassword){
            throw standardException;
        }

        return user;
    }

    /**
     * Reset password for a user
     */
    public static String resetPassword(PrepUser user) throws EmailException{
        String pwd = UUID.randomUUID().toString().substring(0,8);
        sendPasswordResetEmail(user.email, pwd);
        return pwd;
    }

    private static void sendPasswordResetEmail(String emailAddress, String pwd) throws EmailException {
        HtmlEmail email = new HtmlEmail();
        email.setSubject("Your Prepado account update");
        email.addTo(emailAddress);
        email.setFrom("noreply@prepado.com", "Prepado Admin");
        String link = Http.Request.current().getBase() + "/forgotPassword";

        StringBuilder builder = new StringBuilder();
        builder.append("<html>");
        builder.append("Your password has been replaced with the following temporary one: ");
        builder.append(pwd);
        builder.append("<br/><br/>");
        builder.append("Please log in to Prepado within the next 24 hours to create a permanent password.");
        builder.append("<br/>");
        builder.append("If you did not request a password reset ignore this message, your password has not been changed.");
        builder.append("<br/>");
        builder.append("<a href=\"" + link + "\">" + link + "</a>");
        builder.append("</html>");
        email.setMsg(builder.toString());

        Mail.send(email);
    }

    @Override
    public boolean hasAccess(PrepUser user) {
        return user.equals(this);
    }

    public void setStripeId(String stripeId) {
        if (this.stripeId == null || !this.stripeId.equals(stripeId)) {
            this.stripeId = stripeId;
            bindToStripe();
        }
    }

    /**
     * Runs some additional checks on the User to ensure that his account status is in good standing and he should be
     * allowed to log in.  In cases where the user should not be able to log in, throws a reason-specific exception
     * @param user The PrepUser to run checks on
     */
    private static void throwExceptionsIfNecessary(PrepUser user){
        //have they paid

        //are they allowed to log in?

        //throw an exception if the user exists but they are not allowed to log in for some reason
    }

    public String hashPassword(String password) {
        return Crypto.passwordHash(BCrypt.hashpw(password, salt),
                Crypto.HashType.SHA512);
    }

    /**
     * Sets the user's password to a hash of the plain text password argument and clears the password expiration (Dropped as of 131 of the SQL evolutions).
     *
     * @param password Plain text password value.
     */
    public void setPassword(String password) {
        this.password = hashPassword(password);
        //setting a new actual password clears out any temporary password data
        this.tempPassword = null;
        this.tempExpire = null;
        //this._save();
    }

    /**
     * Sets a temp password that will expire 24 hours after created
     */
    public void setTemporaryPassword(final String password) {
        this.tempPassword = hashPassword(password);
        Date expirationDate = new Date();
        expirationDate = DateUtils.addDays(expirationDate, 1);
        this.tempExpire = expirationDate;
        //this._save();
    }


    /*****************************************************
     * Utility Methods                                   *
     *****************************************************/
    
    public boolean hasRole(RoleValue role){
    	return 
    			PrepUserRole.find.where().and(
    					Expr.eq("prep_user_id", this.id),
    					Expr.eq("role", role))
    					.findRowCount()
    			> 0;
    }

    public List<String> getRoleNames() {
        List<String> result = new ArrayList<String>();
        List<PrepUserRole> userRoles = PrepUserRole.find.where().eq("prep_user_id", this.id).findList();
        for (PrepUserRole userRole : userRoles) {
            result.add(userRole.role.name());
        }
        return result;
    }

    public Boolean passwordExpired(){
        return tempExpire != null && new Date().before(tempExpire);
    }

    /**
     *
     * @return amount Payment amount without coupons applied
     */
    public Integer getRawNextPaymentAmount() {
        return Integer.parseInt(Play.configuration.getProperty("prep.stripe.base-charge"));
    }

    /*****************************************************
     * Custom Exceptions                                 *
     *****************************************************/

    public static class ConflictingUserException extends RuntimeException {
        public AuthProvider requestedAuthProvider;
        public AuthProvider conflictingAuthProvider;

        public ConflictingUserException(AuthProvider requestedAuthProvider, AuthProvider conflictingAuthProvider) {
            this.requestedAuthProvider = requestedAuthProvider;
            this.conflictingAuthProvider = conflictingAuthProvider;
        }
    }

    /**
     *
     * @param email
     * @return null if not found
     */
    public static PrepUser findByEmail(String email){
        //will return Null if not found.
        return PrepUser.find.where().eq("email", email).findUnique();
    }
    
	@Override
	public JsonSerializer serializer() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public CachedPrepUser cached(){
		return new CachedPrepUser();
	}

	public class CachedPrepUser extends CachedEntity{

		public CachedPrepUser() {
			super(PrepUser.class, id);
		}

		public CachedProperty<Boolean> hasPaid(){

			return new CachedProperty<Boolean>(){

				@Override
				public Boolean getValue() throws CacheMiss {
					return _getAsBoolean("hasPaid");
				}

				@Override
				public void setValue(Boolean value) {
					_setValue("hasPaid",value);
				}

				@Override
				public void clear() {
					_unsetValue("hasPaid");
				}

			};
		}

	}

	/*****************************************************
	 * Stripe Customer delegate methods                     *
     *****************************************************/
	private void bindToStripe(){
		if(stripeCustomer == null){
			stripeCustomer = new Stripe_PrepUser(this, stripeId);
		}
	}
	public String getLastFour() {
		return ((StripeResult<String>)getLastFourAsPromise()).get();
	}

	public void changeSubscriptionType(SubscriptionType type) {
		
		((StripeResult)changeSubscriptionTypeAsPromise(type)).get();
	}

	public void setCreditCard(String stripeToken) {
		
		((StripeResult)setCreditCardAsPromise(stripeToken)).get();
	}
	
	public Integer getNextChargeAmount(){
		
		return ((StripeResult<Integer>)getNextChargeAmountAsPromise()).get();
	}

	public Date getChargeDate() {
		
		return ((StripeResult<Date>)getChargeDateAsPromise()).get();
	}

	public SubscriptionType getSubscriptionType() {
		
		return ((StripeResult<SubscriptionType>)getSubscriptionTypeAsPromise()).get();
	}

	public Date getSubscriptionExpiration() {
		
		return ((StripeResult<Date>)getSubscriptionExpirationAsPromise()).get();
	}

	public String getCardType() {
		
		return ((StripeResult<String>)getCardTypeAsPromise()).get();
	}

	public Boolean hasPaid() {
		
		return ((StripeResult<Boolean>)hasPaidAsPromise()).get();
	}

	public Boolean hasPaid(Subscription subscription) {
		
		return ((StripeResult<Boolean>) hasPaidAsPromise(subscription)).get();
	}

	public String createSubscription(String stripeToken, String coupon) {
		
			return ((StripeResult<String>)createSubscriptionAsPromise(stripeToken, coupon)).get();
	}
	
	/*
	 * 
	 * As promises...
	 * 
	 */
	public Promise<String> getLastFourAsPromise() {
		bindToStripe();
		return stripeCustomer.getLastFour();
	}

	public Promise changeSubscriptionTypeAsPromise(SubscriptionType type) {
		cached().hasPaid().clear();
		bindToStripe();
		return stripeCustomer.changeSubscriptionType(type);
	}

	public Promise setCreditCardAsPromise(String stripeToken) {
		bindToStripe();
		return stripeCustomer.setCreditCard(stripeToken);
	}

	public Promise<Date> getChargeDateAsPromise() {
		bindToStripe();
		return stripeCustomer.getChargeDate();
	}
	
	public Promise<Integer> getNextChargeAmountAsPromise(){
		bindToStripe();
		return stripeCustomer.getNextChargeAmount();
	}

	public Promise<SubscriptionType> getSubscriptionTypeAsPromise() {
		bindToStripe();
		return stripeCustomer.getSubscriptionType();
	}

	public Promise<Date> getSubscriptionExpirationAsPromise() {
		bindToStripe();
		return stripeCustomer.getSubscriptionExpiration();
	}

	public Promise<String> getCardTypeAsPromise() {
		bindToStripe();
		return stripeCustomer.getCardType();
	}

	public Promise<Boolean> hasPaidAsPromise() {
		bindToStripe();
		return stripeCustomer.hasPaid();
	}

	public Promise<Boolean> hasPaidAsPromise(Subscription subscription) {
		bindToStripe();
		return stripeCustomer.hasPaid(subscription);
	}

	public Promise<String> createSubscriptionAsPromise(String stripeToken, String coupon) {
		cached().hasPaid().clear();
		bindToStripe();
		return stripeCustomer.createSubscription(stripeToken, coupon);
	}

	public Promise<String> applyCouponAsPromise(String coupon) {
		cached().hasPaid().clear();
		bindToStripe();
		return stripeCustomer.applyCoupon(coupon);
	}
	
	
	
}
