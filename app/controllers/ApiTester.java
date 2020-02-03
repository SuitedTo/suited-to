package controllers;

import controllers.deadbolt.Deadbolt;
import controllers.deadbolt.Restrict;
import enums.RoleValue;
import play.mvc.Controller;
import play.mvc.With;


@With(Deadbolt.class)
@Restrict(RoleValue.APP_ADMIN_STRING)
public class ApiTester extends Controller {

    public static void index(){
        render();
    }

    public static void requestToken(String url, String token, String secret){

    }
}
