package controllers.prep.access;

import java.util.Map;

import models.PrepRestrictable;

import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Scope;

import com.avaje.ebean.Ebean;
import common.utils.prep.SecurityUtil;

import errors.prep.PrepError;
import errors.prep.PrepErrorResult;
import errors.prep.PrepErrorType;

public class Access extends Controller{

	/**
	 * The RestrictAccess annotation restricts access to a controller action
	 */
	@Before
	public static void checkActionAccess(){

		RestrictAccess far = getActionAnnotation(RestrictAccess.class);
		if(far == null){
			return;
		}
		Class<? extends AccessManager> amClass = far.value();
		if(amClass != null){
			try {
				amClass.newInstance().checkAccess();
			} catch (Exception e) {
				Logger.error("Unable to check access with %s", amClass.getName());
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * The PrepRestrictedResource annotation restricts access to a resource
	 */
	@Before
	public static void checkResourceAccess(){
		PrepRestrictedResource restrictedResource = getActionAnnotation(PrepRestrictedResource.class);
        if(restrictedResource != null){
            Long idValue = extractIdValueFromParams();
            //if no id param is present on the request then default to allowed
            if (idValue == null) {
                throw new IllegalStateException("PrepRestrictedResource method annotation was used but no id to restrict could be determined");

            }
            String className = restrictedResource.resourceClassName();

            try {
                Class modelClass = Class.forName(className);
                PrepRestrictable restrictable = (PrepRestrictable)Ebean.createQuery(modelClass)
                		.where().eq("id", idValue).findUnique();
                if(restrictable == null){
                	notFound();
                }
                	
                if(!restrictable.hasAccess(SecurityUtil.connectedUser())){
                	throw new PrepErrorResult(new PrepError(PrepErrorType.ACCESS_ERROR));
                }

            } catch (ClassCastException e){
                Logger.error("checkAuthorization className: " + className + " does not map to a class implementing the PrepRestricable interface");
            } catch (Exception e) {
                Logger.error(e, "Exception trying to check access for Prep");
            }
        }
	}
	
	private static Long extractIdValueFromParams(){
        final Scope.Params params = Http.Request.current().params;
        String id = params.get("id");
        if(id == null){
            Map<String, String> paramMap = params.allSimple();
            for (String key : paramMap.keySet()) {
                if(key.endsWith(".id")){
                    id = paramMap.get(key);
                    break;
                }
            }
        }
        return StringUtils.isNotEmpty(id) ? Long.valueOf(id) : null;
    }
	
	public static interface AccessManager{
		public void checkAccess();
	}
}
