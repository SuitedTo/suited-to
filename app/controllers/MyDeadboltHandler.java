package controllers;

import controllers.deadbolt.DeadboltHandler;
import controllers.deadbolt.ExternalizedRestrictionsAccessor;
import controllers.deadbolt.RestrictedResourcesHandler;
import models.deadbolt.RoleHolder;
import play.Logger;
import play.mvc.Controller;

public class MyDeadboltHandler extends Controller implements DeadboltHandler {

    @Override
    public void beforeRoleCheck() {
        if (!Security.isConnected()) {
            try {
                if (!session.contains("username")){
                    flash.put("url", "GET".equals(request.method) ? request.url : "/");
                    Security.login();
                }
            } catch (Exception e){
                Logger.error(e, "Exception caught in beforeRoleCheck method");
            }
        }
    }


    @Override
    public RoleHolder getRoleHolder() {
        return Security.connectedUser();
    }

    @Override
    public void onAccessFailure(String controllerClassName) {
        forbidden();
    }

    @Override
    public ExternalizedRestrictionsAccessor getExternalizedRestrictionsAccessor() {
        throw new UnsupportedOperationException("Not Implemented");
    }

    @Override
    public RestrictedResourcesHandler getRestrictedResourcesHandler() {
        return new MyRestrictedResourcesHandler();
    }
}
