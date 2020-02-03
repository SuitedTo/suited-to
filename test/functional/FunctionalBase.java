package functional;

import models.User;
import org.junit.Before;
import play.mvc.Http.Request;
import play.mvc.Http.Response;
import play.test.Fixtures;
import play.test.FunctionalTest;

/**
 * Base class for functional tests.
 *
 * @author joel
 */
public abstract class FunctionalBase extends FunctionalTest {

    /**
     * Connected user
     */
    protected User user;

    /**
     * Load test data and log in.
     */
    @Before
    public void before() {
        Fixtures.deleteDatabase();
        Fixtures.loadModels("data.yml");
        user = User.findByUsername("bob@gmail.com");
        login(user, "secret");
    }

    /**
     * Send the login request. Cookies Map will store session id
     * after logging in so the rest of our requests will be authenticated.
     */
    protected void login(User user, String password) {
        Request request = newRequest();
        request.method = "POST";
        request.path = "/login";
        request.params.put("username", user.email);
        request.params.put("password", password);
        request.params.put("remember", "true");
        Response response = newResponse();
        makeRequest(request, response);
    }

    /**
     * Send the logout request.
     */
    protected void logout() {

        Request request = newRequest();
        request.method = "GET";
        request.path = "/logout";
        Response response = newResponse();
        makeRequest(request, response);
    }
    
}
