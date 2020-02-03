package common.utils.prep;


import dto.prep.PrepUserDTO;
import errors.prep.PrepHttpError;
import errors.prep.PrepHttpErrorResult;
import models.prep.PrepUser;
import play.mvc.Http;
import play.mvc.Scope;
import session.prep.SessionManager;

/**
 * Utility class for handling all things security related within the Prep application.
 */
public class SecurityUtil {

    /**
     * Retrieves the actual PrepUser object that is currently logged in to the system which may be null
     * @return PrepUser
     */
    public static PrepUser connectedUser(){
    	
        return SessionManager.current().getSessionOwner();
    }

    /**
     * Sets all the appropriate values in the current session
     * @param userDTO
     */
    public static void createAuthenticatedSession(PrepUserDTO userDTO){
    	
    	SessionManager.current().createSession(PrepUser.find.byId(userDTO.id));
    }

    /**
     * Clears the current Session including all security credentials which is effectively logging the user out of the
     * application
     */
    public static void clearSession(){
    	
    	SessionManager.current().clearSession();
    }
}
