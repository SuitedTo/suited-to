package controllers;

import cache.HeapCache;
import controllers.deadbolt.RestrictedResourcesHandler;
import enums.AccountLimitedAction;
import enums.AccountType;
import enums.RoleValue;
import models.AccountHolder;
import models.GateKeeper;
import models.Restrictable;
import models.User;
import models.deadbolt.AccessResult;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.mvc.Http;
import play.mvc.Scope;
import utils.RestrictionCheck;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyRestrictedResourcesHandler implements RestrictedResourcesHandler {

    private static RestrictionCheck APP_ADMIN_ONLY = new AppAdminOnly();
    private static RestrictionCheck COMPANY_AND_APP_ADMIN = new CompanyAndAppAdmin();
    private static RestrictionCheck NON_LONER_ONLY = new NonLonerOnly();
    
    private Map<String, RestrictionCheck> myRestrictions = 
            new HashMap<String, RestrictionCheck>();
    
    public MyRestrictedResourcesHandler() {
        myRestrictions.put("resource.create_user", 
                new AccountLimitedCheck(AccountLimitedAction.CREATE_USER));
        myRestrictions.put("resource.create_administrator", 
                new AccountLimitedCheck(
                    AccountLimitedAction.CREATE_ADMINISTRATOR));
        myRestrictions.put("resource.create_candidate", 
                new AccountLimitedCheck(AccountLimitedAction.CREATE_CANDIDATE));
        myRestrictions.put("resource.create_interview", 
                new AccountLimitedCheck(AccountLimitedAction.CREATE_INTERVIEW));
        myRestrictions.put("resource.create_private_questions", 
                new AccountLimitedCheck(
                    AccountLimitedAction.CREATE_PRIVATE_QUESTION));
        
        myRestrictions.put("Questions.downloadSpreadSheet", APP_ADMIN_ONLY);
        
        myRestrictions.put("Interviews.list", NON_LONER_ONLY);
        myRestrictions.put("Candidates.list", NON_LONER_ONLY);
        myRestrictions.put("Jobs.list", COMPANY_AND_APP_ADMIN);
        
        myRestrictions.put("nonLonerOnly", NON_LONER_ONLY);
    }
    
    @Override
    public AccessResult checkAccess(List<String> resourceNames, Map<String, String> resourceParameters) {
        AccessResult result = AccessResult.ALLOWED;
        //a blank restrictedResource tag assumes the use of the Gatekeeper functionality to check the current Controller and Action
        if(resourceNames == null || resourceNames.isEmpty() || StringUtils.isEmpty(resourceNames.get(0))){
            return checkGatekeeperForFeatureAccess(Http.Request.current().action);
        }
        for (String resourceName : resourceNames) {
            if (myRestrictions.containsKey(resourceName)) {
                result = myRestrictions.get(resourceName).check(resourceName);
            }

            /*if "myRestrictions" check says that the resource is denied don't even bother making additional checks, but
            don't assume that the resource is allowed just because the myRestictions says it is.*/
            if(AccessResult.ALLOWED.equals(result)){
                if (resourceName.startsWith("models.")) {
                    result = checkAccessForRestrictableModel(resourceName);
                } else {
                    result = checkGatekeeperForFeatureAccess(resourceName);
                }
            }
        }

        return result;
    }


    private static AccessResult checkGatekeeperForFeatureAccess(String key){
        final User user = Security.connectedUser();
        return (user != null && user.hasRole(RoleValue.APP_ADMIN))||
               GateKeeper.hasAccess(key) ? AccessResult.ALLOWED : AccessResult.DENIED;
    }


    /**
     *
     * @param className
     * @return
     */
    private AccessResult checkAccessForRestrictableModel(String className) {
        /* get the id and lookup the object byId, its possible that the id param won't be present.  A typical case is on
         * the "show" actions where the same method is used for displaying the form for creating a new entity and editing
         * an existing one.  If the id is not present assume that access is allowed
         */
        Long idValue = extractIdValueFromParams();
        //if no id param is present on the request then default to allowed
        if (idValue == null) {
            return AccessResult.ALLOWED;
        }

        try {
            Class modelClass = HeapCache.getClassForName(className);
            Method finder = modelClass.getMethod("findById", Object.class);
            Restrictable restrictable = (Restrictable)finder.invoke(null, new Object[]{idValue});
            return restrictable != null && restrictable.hasAccess(Security.connectedUser()) ? AccessResult.ALLOWED : AccessResult.DENIED;

        } catch (ClassCastException e){
            Logger.error("checkAccessForRestrictableModel className: " + className + " does not map to a class implementing the Restricable interface");
        } catch (Exception e) {
            Logger.error(e, "Exception trying to check access");
        }

        return AccessResult.DENIED;

    }

    /**
     * This supports a couple of different conventions:
     * 1. there is a parameter named "id"
     * 2. there is a parameter named "/modelType/.id" example: question.id 
     * the second variation supports directly binding to JPA entities
     * @return Long value of the id parameter or null if none found
     */
    private Long extractIdValueFromParams(){
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
    
    private class AccountLimitedCheck implements RestrictionCheck {
        private final AccountLimitedAction myAction;
        
        public AccountLimitedCheck(AccountLimitedAction action) {
            myAction = action;
        }

        public AccessResult check(String key) {
            AccessResult result;
                
            if (myAction.canPerform((AccountHolder)Security.connectedUser())) {
                result = AccessResult.ALLOWED;
            }
            else {
                result = AccessResult.DENIED;
            }
            
            return result;
        }
    }
    
    private static class AppAdminOnly implements RestrictionCheck {

        public AccessResult check(String key) {
            AccessResult result;
            
            if (Security.connectedUser().hasRole(RoleValue.APP_ADMIN)) {
                result = AccessResult.ALLOWED;
            }
            else {
                result = AccessResult.DENIED;
            }
            
            return result;
        }
        
    }

    private static class CompanyAndAppAdmin implements RestrictionCheck {

        public AccessResult check(String key) {
            AccessResult result;

            User user = Security.connectedUser();

            if (user.hasRole(RoleValue.APP_ADMIN) || user.hasRole(RoleValue.COMPANY_ADMIN)) {
                result = AccessResult.ALLOWED;
            }
            else {
                result = AccessResult.DENIED;
            }

            return result;
        }

    }
    
    private static class NonLonerOnly implements RestrictionCheck {

        public AccessResult check(String key) {
            AccessResult result;
            
            if (Security.connectedUser().getAccountType().equals(
                    AccountType.INDIVIDUAL)) {
                result = AccessResult.DENIED;
            }
            else {
                result = AccessResult.ALLOWED;
            }
            
            return result;
        }
    }
}
