package controllers;


import enums.RoleValue;
import play.Logger;
import play.data.validation.Required;
import play.mvc.Util;
import integration.taleo.TaleoCredentials;
import integration.taleo.TaleoService;
import integration.taleo.generated.WebServicesException;
import models.User;

public class Taleo extends ControllerBase{
    
    public static void checkAccess(){
    	if(getCredentialsForConnectedUser() != null){
    		ok();
    	}
    	forbidden();
    }
    
    @Util
    public static TaleoCredentials getCredentialsForConnectedUser(){
    	User user = Security.connectedUser();
    	if(user.hasRole(RoleValue.APP_ADMIN) || ((user.company != null) && user.company.taleoIntegration)){
    		String tcStr = session.get("taleoCredentials");
    		if(tcStr != null){
    			
    			try {
    				TaleoCredentials tc = TaleoCredentials.fromXML(tcStr);
    				TaleoService.INSTANCE(tc, false);
    				return tc;
    			} catch (Exception e) {
    				Logger.error("Unable to get Taleo credentials: %s" , e.getMessage());
    			}
    		}
    	}
    	session.remove("taleoCredentials");
    	return null;
    }
    
    public static void login(@Required String companyCode,
            @Required String userName,
            @Required String password){
                try {
                    TaleoService.INSTANCE(new TaleoCredentials(companyCode, userName, password), true);
                    session.put("taleoCredentials", new TaleoCredentials(companyCode, userName, password).toXML());
                    ok();
                } catch (Exception e) {
                	session.remove("taleoCredentials");
                    forbidden(e.getMessage());
                }
    }
}
