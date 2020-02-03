package controllers.prep.rest;

import common.utils.prep.SecurityUtil;
import controllers.prep.auth.PublicAction;
import play.data.binding.As;
import play.data.validation.Required;
import controllers.prep.delegates.UsersDelegate;
import data.binding.types.prep.JsonBinder;
import dto.prep.PrepCredentialsDTO;
import dto.prep.PrepUserDTO;
import play.mvc.Scope;

/**
 * RESTful Service for Sessions
 */
public class Sessions extends PrepController{

    /**
     * Create a new Session for the given credentials.  No new entities are actually created here.
     * @param body
     */
    @PublicAction
    public static void create(@As(binder = JsonBinder.class) PrepCredentialsDTO body){
		if(body == null){
            body = new PrepCredentialsDTO();
        }


        //the userDTO should never actually be null after this call.  If there was a problem logging in a
        //PrepErrorResult should be thrown by the call to login()
		PrepUserDTO userDTO = await(UsersDelegate.login(body));

        SecurityUtil.createAuthenticatedSession(userDTO);

        renderRefinedJSON(userDTO.toJsonTree());

	}

    /**
     * Clears the authentication credentials from the user session
     */
    public static void delete(){
        SecurityUtil.clearSession();
        emptyObject();
    }
}
