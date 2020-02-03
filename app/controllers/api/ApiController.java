package controllers.api;

import controllers.ControllerBase;
import models.*;
import net.oauth.OAuthMessage;
import play.db.jpa.GenericModel;
import play.mvc.Before;
import utils.StringUtils;
import utils.oauth.MyOAuthProvider;
import utils.oauth.util.OAuthUtil;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public abstract class ApiController extends ControllerBase {

    protected static DateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");

    @Before
    public static void determineAppropriateEntity(String entityKey) {
        //some API calls won't have an entityKey in that case just skip this logic.
        if(StringUtils.isEmpty(entityKey)){
            return;
        }

        if ("me".equals(entityKey)) {
            try {
                OAuthMessage requestMessage = OAuthUtil.getMessage(request, null);
                OAuthAccessor accessor = MyOAuthProvider.getAccessor(requestMessage);
                if(accessor != null){
                    routeArgs.put("entity", accessor.user);
                }
            } catch (Exception e) {
                MyOAuthProvider.handleException(e, request, response, true);
            }
            return;
        }


        Long idValue = null;
        try {
            idValue = Long.parseLong(entityKey);
        } catch (NumberFormatException e) {
            renderError("Invalid format for entityKey. Expected number value but got " + entityKey);
        }

        GenericModel result = null;
        if (idValue != null) {
            //this logic assumes that ids are unique across entities across the system
            result = Company.findById(idValue);

            if (result == null) {
                result = User.findById(idValue);
            }

            if (result == null) {
                result = Candidate.findById(idValue);
            }

            if (result == null) {
                result = Interview.findById(idValue);
            }

            if (result == null) {
                result = Question.find("byId", idValue).first();
            }

            if (result == null){
                result = CandidateFile.findById(idValue);
            }
        }


        if (result == null) {
            renderError("Could not determine entity from entityKey value " + entityKey);
        }

        routeArgs.put("entity", result);

    }

    @Before
    public static void checkAccess() {
        try {
            OAuthMessage requestMessage = OAuthUtil.getMessage(request, null);

            OAuthAccessor accessor = MyOAuthProvider.getAccessor(requestMessage);
            MyOAuthProvider.VALIDATOR.validateMessage(requestMessage, accessor);
            routeArgs.put("user", accessor.user);
        } catch (Exception e) {
            MyOAuthProvider.handleException(e, request, response, true);
        }
    }

    @Before(priority = 5) //set to a higher number so that default (0) priority methods are executed first
    public static void handleAccessCheck(){
        Restrictable entity = (Restrictable) routeArgs.get("entity");
        User accessorUser = (User) routeArgs.get("user");
        if(entity != null && !entity.hasAccess(accessorUser)){
            renderError("User associated with accessToken is not allowed to access requested entity");
        }
    }

    /**
     * After all the @Before handlers fire there should be an Object present in the routeArgs.  This is just a convenience
     * method to retrieve that object
     * @throws IllegalStateException if no entity is present
     */
    protected static ModelBase getSpecifiedEntity(){
        ModelBase entity = (ModelBase)routeArgs.get("entity");
        if(entity == null){
            throw new IllegalStateException("API method invoked with no entity specified");
        }
        return entity;
    }

    /**
     * The application user that corresponds to the access token used to make the API call.  Will not be populated until
     * the @Before handlers fire and set the user in the routeArgs
     * @return User
     */
    protected static User getUserInvokingService(){
         return (User) routeArgs.get("user");
    }

    /**
     * Renders a properly formatted error message and causes the action to halt.
     * @param message
     */
    protected static void renderError(String message){
        Map<String, String> error = new HashMap<String, String>();
        error.put("error", message);
        renderJSON(error);
    }

    /**
     * Builds the default Map that can be rendered as JSON data for API calls
     * @return Map of Strings
     */
    protected static Map<String, String> buildSuccessResponseMap(){
        Map<String, String> response = ControllerBase.buildSuccessResponseMap();
        return response;
    }

}

