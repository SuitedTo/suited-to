package controllers;

import play.Play;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Util;
import utils.StringUtils;

/**
 * Created with IntelliJ IDEA for sparc-interview
 * User: phutchinson
 * Date: 7/23/13
 * Time: 9:04 AM
 */
public class BasicAuthController extends Controller {

    /**
     * Checks if Basic Authentication is required for the environment and presents the basic auth popup if necessary.
     * Useful for public-facing dev or test environments
     * @throws Exception
     */
    @Util
    public static void doBasicAuthCheck(String realm) throws Exception {

        String systemPassword = Play.configuration.getProperty("basicAuth.password");

        //the basic auth configuration has not been set just return and continue with application
        if(StringUtils.isEmpty(systemPassword) || systemPassword.startsWith("${")){
            return;
        }

        Http.Header authHeader = request.headers.get("authorization");
        if(authHeader == null){
            response.setHeader("WWW-authenticate", realm);
            unauthorized();
        }

        String enteredAuth = authHeader.value().substring(6); //should be in the format "Basic SOMEENCRYPTEDVALUE"
        byte[] decodedAuth = new sun.misc.BASE64Decoder().decodeBuffer(enteredAuth);

        String[] decodedUserNameAndPassword = new String(decodedAuth, "UTF-8").split(":");

        if (decodedUserNameAndPassword == null || decodedUserNameAndPassword.length != 2) {
            unauthorized();
        }

        String enteredPassword = decodedUserNameAndPassword[1];

        if(!enteredPassword.equals(systemPassword)){
            unauthorized();
        }

    }
}
