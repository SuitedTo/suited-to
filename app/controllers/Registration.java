package controllers;

import data.validation.PasswordCheck;
import enums.RoleValue;
import enums.UserStatus;
import models.User;
import notifiers.Mails;
import play.Logger;
import play.Play;
import play.data.validation.Required;
import play.i18n.Messages;
import play.libs.OpenID;
import play.mvc.Router;
import utils.StringUtils;

import java.util.Arrays;
import java.util.TimeZone;

public class Registration extends ControllerBase {

    /*************************************************
     * Static Fields                                 *
     *************************************************/

    public static final String OPEN_ID_EMAIL = "email";


    /*************************************************
     * Actions                                       *
     *************************************************/

    public static void invitationFail(String reason) {
        render(reason);
    }

	public static void acceptInvitation(String invitationKey){
        User user = User.find("byInvitationKey", invitationKey).first();

        OpenID.UserInfo verifiedUser = OpenID.getVerifiedID();
        if(verifiedUser != null){
            user.googleOpenIdEmail = verifiedUser.extensions.get("email");
            user.googleOpenIdUrl = verifiedUser.id;
        }

		if(user == null || !UserStatus.PENDING.equals(user.status)){
			Application.home();
		}
                else if (Security.isConnected() && 
                        !Security.connectedUser().id.equals(user.id)) {
                    
                    invitationFail(Messages.get("invitation.already.logged.in",
                            Router.getFullUrl("Security.logout")));
                }
		render(user, invitationKey);
	}

	public static void acceptInvitationSave(@Required String invitationKey, String password, String confirmPassword,
            User user, String displayName){

		/* there may be a way to improve security here, but basically what we're trying to prevent is someone being able
		 * to accept an invitation just by guessing a 24 character registration key.  By checking that the user Id
		 * matches the registration key we're at least preventing the ability to just randomly guess a registration key
		 * to use.*/
		if(!user.invitationKey.equals(invitationKey)){
			validation.required("invitationKey.doesNotMatch");
		}

                //TODO : This is a quick hack to fix a crash under a time crunch
                //       We should figure out why letting user.displayName be
                //       temporarily set causes an SQL key violation
                if (!StringUtils.isEmpty(displayName)) {
                    
                    if (User.count("byDisplayNameIlike", 
                            displayName.trim().toLowerCase()) == 0) {
                        user.displayName = displayName;
                    }
                    else {
                        validation.isTrue(false).message("username.notUnique");
                    }
                }
                
        //when using open id for user credentials the email and password are not required
        if(StringUtils.isEmpty(user.googleOpenIdUrl)){
            validation.required(user.email);
            validation.required(password);
            validation.range(password.length(), PasswordCheck.MIN, PasswordCheck.MAX)
                    .message(Messages.get("validation.password", "", "Password", PasswordCheck.MIN, PasswordCheck.MAX));
            validation.equals(password, confirmPassword).message("password.confirmation.doNotMatch");
            user.password = password;
        }

        user.status = UserStatus.ACTIVE;
		user.questionStatusEmails = true;


        //set the timeZone
        String timeZone = user.timeZone;
        if(timeZone != null){
            if(Arrays.asList(TimeZone.getAvailableIDs()).contains(timeZone)){
                user.timeZone = timeZone;
            } else{
                Logger.error("User %s has been created with unknown time zone %s. The default time zone will be used instead.",
                        user.email, timeZone);
                user.timeZone = Play.configuration.getProperty("default.timezone");
            }
        }
        validation.valid(user);
		if(validation.hasErrors()){
			renderTemplate("@acceptInvitation", user, invitationKey, password,
					confirmPassword, displayName);
		}

		user.save();

		if(user.hasRole(RoleValue.COMPANY_ADMIN)) {
			Mails.adminWelcome(user);
		}
		else {
			Mails.userWelcome(user);
		}

		// Mark user as connected
		session.put("username", user.email);
		session.put("prettyusername", user.email);

		Application.home();
	}

    /**
     * Initiates the OpenID flow with Google, passes in the given callback as the return url.
     * @param callback Url to return to once the OpenID flow is complete
     */
    public static void useOpenIdForRegistration(String callback){
        /*handle any mismatch in protocol between the callback and request.getBase() since OpenID spec requires the realm
        and return_to values to match both domain and protocol.  This *should* already match but we have seen instances
        where requet.getBase() is returning an "http://" value even though https is being used. */
        boolean environmentIsSecure = Play.configuration.getProperty("force.ssl").equals("true");
        String realm = request.getBase();
        if (realm.startsWith("http://") && environmentIsSecure){
            realm = realm.replaceFirst("http://", "https://");
        }
        if (callback.startsWith("http://") && environmentIsSecure){
            callback = callback.replaceFirst("http://", "https://");
        }

        OpenID.id("https://www.google.com/accounts/o8/id")
                .required(OPEN_ID_EMAIL, "http://axschema.org/contact/email")
                .forRealm(realm)
                .returnTo(callback)
                .verify();
    }


    /**
     * renders the registration page
     */
	public static void selfRegistration(){
        if (Security.isConnected()) {
            flash.error("registration.error.loggedInSignup");
            flash.keep();
            Application.home();
        }

        OpenID.UserInfo verifiedUser = OpenID.getVerifiedID();
        User user = new User();
        if(verifiedUser != null){
            user.email = verifiedUser.extensions.get(OPEN_ID_EMAIL);

            user.googleOpenIdEmail = user.email;
            user.googleOpenIdUrl = verifiedUser.id;
        }

        render(user);
	}

    /**
     * Saves a new user
     * @param user
     * @param password
     * @param confirmPassword
     */
	public static void selfRegistrationSave(User user, String password, String confirmPassword){

        user.registerListeners();
        user.metrics.registerListeners();

        //when using open id for user credentials the email and password are not required
        if(StringUtils.isEmpty(user.googleOpenIdUrl)){
            validation.required(user.email);
            validation.required(password);

            //a little niceness to allow single character passwords in dev mode
            double passwordMin = Play.mode.equals(Play.Mode.DEV) ? 1 : PasswordCheck.MIN;

            validation.range(password.length(), passwordMin, PasswordCheck.MAX)
                    .message(Messages.get("validation.password", "", "Password", PasswordCheck.MIN, PasswordCheck.MAX));
            validation.equals(password, confirmPassword).message("password.confirmation.doNotMatch");
            //hidden inputs are getting bound as empty strings as opposed to the null value that we want so overwrite here
            user.googleOpenIdUrl = null;
            user.googleOpenIdEmail = null;
            user.password = password;
        }


        //set some default stuff
        user.status = UserStatus.ACTIVE;
		user.questionStatusEmails = true;

        //set the timeZone
        String timeZone = user.timeZone;
        if(timeZone != null){
			if(Arrays.asList(TimeZone.getAvailableIDs()).contains(timeZone)){
				user.timeZone = timeZone;
			} else{
				Logger.error("User %s has been created with unknown time zone %s. The default time zone will be used instead.",
						user.email, timeZone);
				user.timeZone = Play.configuration.getProperty("default.timezone");
			}
		}

        validation.valid(user);
		if(validation.hasErrors()){
			renderTemplate("@selfRegistration", user);
		}

		user.save();


		// Mark user as connected
		session.put("username", user.email);
		session.put("prettyusername", user.email);

		Application.chooseUsageType();
	}
}