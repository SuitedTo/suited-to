package controllers.prep.base;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import common.utils.prep.SecurityUtil;
import controllers.BasicAuthController;
import models.prep.PrepUser;
import play.Logger;
import play.Play;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Scope;

public abstract class PrepBaseController extends Controller {

    private static final String REALM = "Basic realm=\"prepado.com\"";

    /** Called before every request to ensure that HTTPS is used. */
    @Before
    public static void redirectToHttps() {
        //if it's not secure, but Heroku has already done the SSL processing then it might actually be secure after all
        if (!request.secure && request.headers.get("x-forwarded-proto") != null) {
            request.secure = request.headers.get("x-forwarded-proto").values.contains("https");
        }

        //redirect if it's not secure
        if (!request.secure && Play.configuration.getProperty("force.ssl").equals("true")) {
            String url = "https://" + request.host + request.url;
            Logger.info("Redirecting to secure: " + url);
            flash.keep("url");
            redirect(url);
        }
    }

    @Before
    public static void prepareResponse() {
        PrepUser user = SecurityUtil.connectedUser();
        Long user_id = null;
        String roles = null;
        boolean is_admin = false;

        if (user != null) {
            user_id = user.id;

            Gson gson = new GsonBuilder().create();
            roles = gson.toJson(user.getRoleNames());
        }

        renderArgs.put("authenticated_userid", user_id == null ? "null" : user_id);
        renderArgs.put("roles", roles == null ? "[]" : roles);
        renderArgs.put("csrf_token", Scope.Session.current().getAuthenticityToken());
    }

    @Before
    public static void doBasicAuthCheck() throws Exception {
        BasicAuthController.doBasicAuthCheck(REALM);
    }
}
