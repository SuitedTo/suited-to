package controllers.prep.auth;

import play.mvc.Before;
import play.mvc.Controller;

import common.utils.prep.SecurityUtil;

import errors.prep.PrepHttpError;
import errors.prep.PrepHttpErrorResult;

/**
 * Created with IntelliJ IDEA for sparc-interview
 * User: phutchinson
 * Date: 3/16/13
 * Time: 9:41 AM
 * Custom server-side authorization checks for the Prep application.  Applied by using @With(Authorization.class) at the
 * controller Class level.  Note that this annotation is inherited by child classed
 */
public class Authorization extends Controller {

    /**
     * Check authorization prior to continuing to execute the requested action
     */
    @Before
    public static void checkAuthorization(){
        /*annotating a class with @With(Authorization.class) is sufficient to require that a user is present. The
        @PublicAction annotation at the action level can bypass this check*/
        if(SecurityUtil.connectedUser() == null){
            PublicAction publicAction = getActionAnnotation(PublicAction.class);
            if(publicAction == null){
                throw new PrepHttpErrorResult(PrepHttpError.AUTHORIZATION_ERROR);
            }
        }
    }
}
