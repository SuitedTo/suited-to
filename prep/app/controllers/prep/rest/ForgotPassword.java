package controllers.prep.rest;

import controllers.prep.base.PrepBaseController;
import controllers.prep.delegates.ErrorHandler;
import errors.prep.PrepErrorType;
import models.prep.PrepUser;
import play.data.binding.As;
import play.data.validation.Required;
import dto.prep.PrepForgotPasswordDTO;
import data.binding.types.prep.JsonBinder;

public class ForgotPassword extends PrepBaseController{
    public static void home(){
        render();
    }

    public static void update(@Required @As(binder = JsonBinder.class)PrepForgotPasswordDTO body){
        PrepUser user = PrepUser.findByEmail(body.email);
        if(user.tempPassword.equals(user.hashPassword(body.temp))){
            if(body.password.equals(body.confirm)){
                user.setPassword(body.password);
                //user._save();
            } else {
                ErrorHandler.emitCustomErrorResult(PrepErrorType.DATA_VALIDATION_ERROR, "The new passwords do not match.");
            }
        } else {
            ErrorHandler.emitCustomErrorResult(PrepErrorType.DATA_VALIDATION_ERROR, "The temp password is not correct.");
        }
    }
}
