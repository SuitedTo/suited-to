package controllers;

import play.mvc.With;
import controllers.deadbolt.Deadbolt;
import controllers.deadbolt.Restrict;
import enums.RoleValue;
import models.InvocationException;

@With(Deadbolt.class)
public class InvocationExceptions extends ControllerBase{

	@Restrict(RoleValue.APP_ADMIN_STRING)
	public static void view(String identifier){
		InvocationException ie = InvocationException.findFirst("byIdentifier", identifier);
		if(ie == null){
			notFound();
		}
		renderArgs.put("exception", ie);
		render();
	}
	
	@Restrict(RoleValue.APP_ADMIN_STRING)
	public static void list(){
		render();
	}
}
