package controllers;

import com.stripe.model.Coupon;
import com.stripe.model.Customer;
import com.stripe.model.Discount;
import com.stripe.model.Subscription;
import data.validation.Password;
import enums.AccountType;
import enums.RoleValue;
import enums.UserStatus;
import exceptions.LoginException;
import exceptions.PaymentException;
import exceptions.TrialExpiredException;
import models.Company;
import models.User;
import notifiers.Mails;
import play.Logger;
import play.data.validation.Required;
import play.libs.*;
import play.mvc.Http;
import play.mvc.Scope;
import play.mvc.Util;
import utils.StringUtils;
import utils.StripeUtil;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Controller for security re
 */
public class Security extends ControllerBase {


    public static OAuth2 GOOGLE = new OAuth2(
            "https://accounts.google.com/o/oauth2/auth",
            "https://accounts.google.com/o/oauth2/token",
            "703651956654-69kbm7dfmollgam4dlls1dsektebu5ln.apps.googleusercontent.com",
            "MT88lyrFFpvO3mFE-ju6wean"
    );

    private static String userInfoUrl = "https://www.googleapis.com/oauth2/v1/userinfo";


    public static void oAuth() throws Throwable {
        if (OAuth2.isCodeResponse()) {
            //User u = connected();
            String accessToken = retrieveAccessToken(authURL());
            if (accessToken == null) {
                displayAuthenticationError();
            }

            //first try and find a SuitedTo user matching the accessToken
            User user = User.find("byGoogleOpenIdUrl", accessToken).first();

            /*if we couldn't find a user that's already hooked up to this
            accessToken try and find a user that matches the email address*/
            if (user == null) {
                String email = null;
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("access_token", accessToken);

                WS.HttpResponse response = WS.url(userInfoUrl).params(params).get();
                if(response.success()) {
                    email = response.getJson().getAsJsonObject().get("email").getAsString();
                }

                user = User.find("byEmail", email).first();

                if (user != null) {
                    user.googleOpenIdUrl = accessToken;
                    user.googleOpenIdEmail = email;
                }
            }

            if (user != null) {
                String username = user.email;
                Boolean allowed = checkAuthentication(username);
                if(validation.hasErrors() || !allowed) {
                    displayAuthenticationError();
                }

                // Mark user as connected
                session.put("username", username);

                // Redirect to the original URL (or /)
                redirectToOriginalURL();
            } else {
                flash.put("customError", "Sorry, that email address does not appear to be linked to a SuitedTo user.");
                displayAuthenticationError();
            }
        }

        Map<String, String> oAuthParams = new HashMap<String, String>();
        oAuthParams.put("response_type", "code");
        oAuthParams.put("scope", "email");

        GOOGLE.retrieveVerificationCode(authURL(), oAuthParams);
    }

    private static String retrieveAccessToken(String callbackURL) {
        String accessCode = Scope.Params.current().get("code");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("client_id", GOOGLE.clientid);
        params.put("client_secret", GOOGLE.secret);
        params.put("redirect_uri", callbackURL);
        params.put("code", accessCode);
        params.put("grant_type", "authorization_code");

        WS.HttpResponse response = WS.url(GOOGLE.accessTokenURL).params(params).post();
        if(response.success()) {
           return response.getJson().getAsJsonObject().get("access_token").getAsString();
        }

        return null;
    }

    static String authURL() {
        return play.mvc.Router.getFullUrl("Security.oauth");
    }

    public static void login() {
        if (isConnected()) {
            redirectToOriginalURL();
        }

        Http.Cookie remember = request.cookies.get("rememberme");
        if(remember != null && remember.value.indexOf("-") > 0) {
            String sign = remember.value.substring(0, remember.value.indexOf("-"));
            String username = remember.value.substring(remember.value.indexOf("-") + 1);
            if(Crypto.sign(username).equals(sign)) {
                session.put("username", username);
                redirectToOriginalURL();
            }
        }
        flash.keep("url");
        render();
    }

    public static void authenticate(String username, String password, boolean remember, String googleAuthorizationCode) throws Throwable {
        System.out.println("google auth code is: " + googleAuthorizationCode);
        if(googleAuthorizationCode != null){

        }

        Boolean allowed = checkAuthentication(username, password);

        if(validation.hasErrors() || !allowed) {
            displayAuthenticationError();
        }

        // Mark user as connected
        session.put("username", username);

        // Remember if needed
        if(remember) {
            response.setCookie("rememberme", Crypto.sign(username) + "-" + username, "30d");
        }

        // Redirect to the original URL (or /)
        redirectToOriginalURL();
    }

    private static void displayAuthenticationError(){
        flash.keep("url");
//        flash.error("secure.error");
        params.flash();
        render("@login");
    }

    public static void logout() throws Throwable{

        session.clear();
        response.removeCookie("rememberme");
        response.removeCookie("DataTables_questions_list");
        response.removeCookie("questions.tab");
        response.removeCookie("questions.datasource");
        onDisconnected();
        flash.success("secure.logout");
        login();
    }


    static void redirectToOriginalURL() {
        onAuthenticated();
        String url = flash.get("url");
        if(url == null) {
            url = "/";
        }
        redirect(url);
    }

    /**
     * This method returns the current connected username
     * @return
     */
    static String connected() {
        return (session == null)?null:session.get("username");
    }

    /**
     * Indicate if a user is currently connected
     * @return  true if the user is connected
     */
    static boolean isConnected() {
        return (session == null)?false:session.contains("username");
    }

    @Util
    public static boolean checkAuthentication(String username){
        return checkAuthentication(username, null, false);
    }

    @Util
    public static boolean checkAuthentication(String username, String password) {
        return checkAuthentication(username, password, true);
    }

    private static boolean checkAuthentication(String username, String password, boolean passwordRequired){
        User user = null;
        try {
            if(passwordRequired) {
                user = User.connect(username, password);
            } else {
                user = User.connect(username);
            }

            /*check that Payment is present and up to date if required.  This is done here in the controller instead of
            * inside the User.connect method because we want to use the Promise API for a potentially longer-running
            * task of making an API call to Stripe*/
            Company company = user.company;
            if(company != null && company.accountType.requiresPayment()){
                Customer customer = await(StripeUtil.getCustomerAsPromise(company));
                if(customer != null){

                    if(!StripeUtil.is100PercentDiscount(customer) &&  company.lastFourCardDigits == null && company.isTrialExpired()){
                        throw new TrialExpiredException();
                    }

                    if(!StripeUtil.isSubscriptionInGoodStanding(customer)){
                        throw new PaymentException();
                    }
                }
            }

        } catch (LoginException e) {
            final String message = e.getMessage();
            Logger.warn("Failed Login Attempt.  Error is: " + message);
            flash.put("customError", message);

            return false;

        } catch (PaymentException e) { //specific handling of a PaymentException
            user = user.merge();
            Logger.warn("Payment Error trying to log in for user: " + user.email);

            //if a payment error has been detected for a company admin, redirect to the company management page
            if (user.hasRole(RoleValue.COMPANY_ADMIN)) {
                /*set a temporary username into the session.  The user isn't actually authenticated and won't be able
                to access any other sections of the application but we need something to check against to make sure
                he's accessing the company he's allowed to access*/
                session.put("temporaryUserName", user.email);
                flash.put("customError", "fixPayment");
                flash.put("companyId", user.company.id);
            } else {
                flash.put("customError", "Please contact your company administrator, your account needs a little attention.");
            }

            return false;

        } catch(TrialExpiredException e) {
            user = user.merge();
            final boolean trialExpiredAndNoCardOnFile = user.company.isTrialExpired() && user.company.lastFourCardDigits == null;
            Logger.warn("SuitedTo's free trial has expired for user: " + user.email);
            //if a payment error has been detected for a company admin, redirect to the company management page
            if(user.hasRole(RoleValue.COMPANY_ADMIN) && trialExpiredAndNoCardOnFile) {
                session.put("temporaryUserName", user.email);
                flash.put("customError", "endOfFreeTrialAdmin");
                flash.put("companyId", user.company.id);
            }  else {
                flash.put("customError", "Please contact your company administrator, your account needs a little attention.");
            }

            return false;
        }

        if (user != null){ //todo: looks like the user should always be non null here
            if(user.hashPassword(password).equals(user.tempPassword) && user.passwordExpired()){
                flash.error("password.expired");
                flash.put("username", username);
                flash.put("password", password);
                render("@changePassword");
            }
            session.put("prettyusername", user.email);
            return true;
        }

        return false;
    }


    public static User connectedUser(){
        String connectedUserName = Security.connected();
        
        User user = null;
        if(connectedUserName != null){
        	String idStr = session.get("id");
        	if(idStr != null){
        		user = User.findById(Long.valueOf(idStr));
        	}else{
        		user = User.findByUsername(connectedUserName);
        		session.put("id", user.id);
        	}
        }

        return user;
    }
    
    public static void validateUser(String userName, String context) throws Throwable{
    	if (connectedUser() == null){
    		if ((userName == null) || (userName.length() == 0)){
    			flash.error("secure.error.user.missing");
    			Security.login();
    		}else{
    			User user = User.findByUsername(userName);
    			if (user == null){
    				flash.error("secure.error.user.invalid");
    				Security.login();
    			} 
                        else if (user.status.equals(UserStatus.PENDING)) {
                            flash.error("secure.error.cannot.reset.unconfirmed.user");
                            Security.login();
                        }
                        else{
    				//A valid user name has been passed in
    				flash.put("username", userName);
    				flash.put("context", context);

                    render();

    			}
    		}
    		login();
    	}else{
    		logout();
    	}
    }
    
    public static void validate () throws Throwable{

        String result = "success";
        render("@validateUser", result);
    }
    
    public static void changePassword(String userName, String password){
    	flash.put("username", userName);
    	flash.put("password", password);
    	flash.success("secure.reset.notification");
    	render("@changePassword");
    }
    
    public static void setNewPassword(
    		@Required String userName,
    		@Password String password,
    		@Password (alias="new.password") String newPassword,
    		@Required String confirmNewPassword) throws Throwable{

    	flash.put("username", userName);
    	if (userName != null){
    		User user = User.connect(userName, password);
    		if (user != null){
    			
    			validation.equals(newPassword, confirmNewPassword).message("password.confirmation.doNotMatch");
    			
    			if(validation.hasErrors()){
    	            render("@changePassword");
    	        }
    			
    			user.setPassword(newPassword);
    			
    			flash.success("secure.passwordChanged");
    			
    			user.save();
    			
    			session.put("username", userName);
    			session.put("prettyusername", user.email);
    			
    			Application.home();
    		}else{
    			flash.error("secure.error");
    		}
    	}
    	render("@changePassword");
    }
    
    public static void resetPassword(String userName, String token) throws Throwable{

    	if (userName != null){
    		User user = User.findByUsername(userName);
    		if ((user != null)){
    			String pwd = UUID.randomUUID().toString().substring(0, 8);
    			user.setTemporaryPassword(pwd);
    			user.save();

    			Mails.temporaryPassword(user, pwd);

                String result = "success";
                render("@validateUser", result);

    		}
    	}

    }
    
    public static void onDisconnected(){
        Community.index();
    }
    
    static void onAuthenticated() {
        User connectedUser = connectedUser();
        session.put("prettyusername", connectedUser.email);
        if(connectedUser.hasRole(RoleValue.APP_ADMIN)){
            response.setCookie("statusAccess", Crypto.sign("@status"), "2d");
        }

        connectedUser.lastLogin= new Date();
        connectedUser.save();

    }
}
