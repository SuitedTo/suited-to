package controllers;

import controllers.deadbolt.Deadbolt;
import controllers.deadbolt.Restrict;
import controllers.deadbolt.RoleHolderPresent;
import enums.RoleValue;
import play.mvc.With;

@With(Deadbolt.class)
@RoleHolderPresent
@Restrict(RoleValue.APP_ADMIN_STRING)
public class OAuthConsumers extends CRUD {
}
