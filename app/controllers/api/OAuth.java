package controllers.api;

import exceptions.LoginException;
import models.OAuthAccessor;
import models.OAuthConsumer;
import net.oauth.OAuthMessage;
import net.oauth.OAuthProblemException;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.mvc.Controller;
import utils.oauth.MyOAuthProvider;
import utils.oauth.util.OAuthUtil;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.OutputStream;

public class OAuth extends Controller {

    /**
     * Retrieve a Request token, if username and password are provided we will attempt to authorize the request token
     * in this single step
     */
    public static void requestToken(String username, String password) throws IOException, ServletException {

        try {
            //parse the request into an OAuthMessage which is just a wrapper around the request
            OAuthMessage message = OAuthUtil.getMessage(request, null);

            //determine the consumer from the wrapped message - at this point we haven't validated the signature
            OAuthConsumer consumer = MyOAuthProvider.getConsumer(message);

            /*create a new accessor object which is basically a record of someone/something trying to access the api via
            a particular consumer*/
            OAuthAccessor accessor = new OAuthAccessor(consumer);

            //validate the message is complete and properly signed
            MyOAuthProvider.VALIDATOR.validateMessage(message, accessor);
            // Support the 'Variable Accessor Secret' extension
            // described in http://oauth.pbwiki.com/AccessorSecret
            String secret = message.getParameter("oauth_accessor_secret");
            if (secret != null) {
                Logger.info("Secret: " + secret);
                accessor.tokenSecret = secret;
            } else {
                Logger.info("Empty Secret!");
            }

            //attempt to authorize the request token if username and password are provided
            if(StringUtils.isNotEmpty(username) || StringUtils.isNotEmpty(password)){
                models.User user = null;
                try {
                    user = models.User.connect(username, password);
                } catch (LoginException e) {
                    OAuthProblemException problem = new OAuthProblemException(
                            net.oauth.OAuth.Problems.PERMISSION_DENIED);
                    problem.setParameter(net.oauth.OAuth.Problems.PERMISSION_DENIED, "Could not authenticate username" + username);
                    throw problem;
                }

                MyOAuthProvider.markAsAuthorized(accessor, user);

            }

            //generate request_token and secret
            accessor = MyOAuthProvider.generateRequestToken(accessor);
            Logger.info("Accessor: %s", accessor);

            response.setContentTypeIfNotSet("text/plain");
            OutputStream out = response.out;
            net.oauth.OAuth.formEncode(net.oauth.OAuth.newList("oauth_token",
                    accessor.requestToken, "oauth_token_secret",
                    accessor.tokenSecret), out);
            out.close();
        } catch (Exception e) {
            MyOAuthProvider.handleException(e, request, response, true);
        }

    }



    public static void accessToken(){
        try {
            OAuthMessage requestMessage = OAuthUtil.getMessage(request, null);

            OAuthAccessor accessor = MyOAuthProvider.getAccessor(requestMessage);
            MyOAuthProvider.VALIDATOR.validateMessage(requestMessage, accessor);

            // make sure token is authorized
            Logger.info("Authorized: %s", accessor.authorized);
            if (accessor.authorized == null) {
                throw new OAuthProblemException("permission_denied");
            }

            // generate access token and secret
            accessor = MyOAuthProvider.generateAccessToken(accessor);

            response.setContentTypeIfNotSet("text/plain");
            OutputStream out = response.out;
            net.oauth.OAuth.formEncode(net.oauth.OAuth.newList("oauth_token", accessor.accessToken,
                    "oauth_token_secret", accessor.tokenSecret), out);
            out.close();

        } catch (Exception e) {
            MyOAuthProvider.handleException(e, request, response, true);
        }

    }
}
