package controllers.prep.rest;

import controllers.prep.access.PrepRestrictedResource;
import models.User;
import models.prep.PrepUser;
import notifiers.prep.Mails;

/**
 * Created with IntelliJ IDEA for sparc-interview
 * User: phutchinson
 * Date: 10/23/13
 * Time: 2:05 PM
 * Allows creation of emails from client
 */
public class Emails extends PrepController {

    public enum EmailType{
        MOBILE_TRIAL_COMPLETE,
        WEBAPP_TRIAL_COMPLETE
    }

    @PrepRestrictedResource(resourceClassName = "models.prep.PrepInterview")
    public static void create(Long id, EmailType type){
        PrepUser user = PrepUser.findById(id);
        notFoundIfNull(user);

        if(EmailType.MOBILE_TRIAL_COMPLETE.equals(type)){
            Mails.mobileTrialComplete(user);
        } else if(EmailType.WEBAPP_TRIAL_COMPLETE.equals(type)){
            Mails.webAppTrialComplete(user);
        } else {
            throw new IllegalStateException("Invalid email type requested type is: " + type);
        }

    }
}
