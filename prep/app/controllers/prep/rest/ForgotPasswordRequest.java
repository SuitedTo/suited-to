package controllers.prep.rest;

import controllers.prep.base.PrepBaseController;
import controllers.prep.delegates.ErrorHandler;
import dto.prep.PrepForgotPasswordRequestDTO;
import errors.prep.PrepErrorType;
import models.prep.PrepUser;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.mail.EmailException;
import data.binding.types.prep.JsonBinder;
import play.data.binding.As;
import play.data.validation.Required;

public class ForgotPasswordRequest extends PrepBaseController {
    public static void home() {
        render();
    }


    public static void update(@Required @As(binder = JsonBinder.class)PrepForgotPasswordRequestDTO body){
        PrepUser user = PrepUser.findByEmail(body.email);
        if(user == null){
            ErrorHandler.emitCustomErrorResult(PrepErrorType.DATA_VALIDATION_ERROR, "No account found with that email address. Maybe you signed up with a 3rd party?");
        } else {
            if (user.externalAuthProviderId == null){
                String pwd = "";
                try{
                    pwd = PrepUser.resetPassword(user);
                }
                catch (EmailException e){
                    ErrorHandler.emitCustomErrorResult(PrepErrorType.DATA_VALIDATION_ERROR, "Could not send the email.");
                }
                if(StringUtils.isNotEmpty(pwd)){
                    user.setTemporaryPassword(pwd);
                    user._save();
                }
            } else {
                ErrorHandler.emitCustomErrorResult(PrepErrorType.DATA_VALIDATION_ERROR, "You appeared to sign up with " + user.externalAuthProvider + ". Please log in with them now.");
            }
        }
    }
}
