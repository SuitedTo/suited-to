package controllers;

import cache.HeapCache;
import enums.StaticPage;
import models.Company;
import play.Logger;
import play.Play;
import play.data.validation.Required;
import play.jobs.Job;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Util;
import play.mvc.results.RenderBinary;
import utils.ResultConverter;
import utils.StringUtils;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class ControllerBase extends Controller {

    public static final DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
    private static final String REALM = "Basic realm=\"suitedTo.com\"";

    @Before
    public static void before(){
        renderArgs.put("connectedUser", Security.connectedUser());
        activateSection();
    }
	
    @Util
    static void activateSection(){
            activateSection(Controller.getControllerClass().getName());
    }

    @Util
    static void activateSection(String sectionName){
        renderArgs.put("currentSection", sectionName);
    }


    @Before
    public static void doBasicAuthCheck() throws Exception {
        BasicAuthController.doBasicAuthCheck(REALM);
    }


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


    /**
     * <p>Returns the company associated with the given company ID, or with the
     * currently logged-in user if <code>companyID</code> is <code>null</code>.
     * </p>
     * 
     * <p>If for any reason the company object cannot be retrieved, a 
     * <code>RuntimeException</code> will be thrown with a message detailing the
     * error.</p>
     * 
     * @param companyID The ID of the company to retrieve, or <code>null</code>
     *            if the company should be retrieved from the current user.
     * 
     * @return The company object.
     * 
     * @throws RuntimeException If the correct company cannot be retrieved for
     *             any reason.
     */
    public static Company getIntendedCompany(Long companyID) {
        Company company = null;
        
        if (companyID == null) {
            company = getUserCompany();
        }
        else {
            company = Company.findById(companyID);
            
            if (company == null) {
                throw new RuntimeException("No such company.");
            }
        }
        
        return company;
    }
    
    /**
     * <p>Returns the company associated with the currently-logged-in user. If
     * no user is logged in, or the user is not associated with a company, a
     * <code>RuntimeException</code> will be thrown with a message describing
     * the error.</p>
     * 
     * @return The company with which the current user is associated.
     */
    public static Company getUserCompany() {
        Company result;
        
        if (Security.isConnected()) {
            final Company company = Security.connectedUser().company;
            if (company == null) {
                throw new RuntimeException(
                        "User does not belong to a company.");
            }
            else {
                result = company;
            }
        }
        else {
            throw new RuntimeException("No user logged in.");
        }
        
        return result;
    }
    
    protected static void renderBinary(final InputStream is, final String name, final String contentType, final boolean inline, final boolean convert) {
    	
    	throw await(new Job<RenderBinary>(){
    		public RenderBinary doJobWithResult(){
    			return ResultConverter.toRenderBinary(is, name, contentType, inline, convert);
    		}
    	}.now());
    }
            
    public static void showStaticPage(@Required StaticPage page){
            if((page == null) || validation.hasErrors()){
                    notFound();
            }
            redirect(page.getURL());
    }

    protected static void validationError(String message) {
        validation.isTrue(false).message(message);
    }
    
    /**
     * Retrieve annotation for the action method.
     * 
     * Note that we're only using this for deadbolt annotations at this time
     * so it won't give you any other sort of annotation.
     * 
     * @param clazz The annotation class
     * @return Annotation object or null if not found
     */
    public static Annotation fastGetActionAnnotation(Class<? extends Annotation> clazz) {
        return HeapCache.getActionAnnotation(Http.Request.current().action, clazz);
    }

    /**
     * Builds the default Map that can be rendered as JSON data for API calls
     * @return Map of Strings
     */
    @Util
    protected static Map<String, String> buildSuccessResponseMap(){
        Map<String, String> response = new HashMap<String, String>();
        response.put("result", "success");
        return response;
    }

    /**
     * Utility to perform authentication check from within custom authorization methods annotated with @Before. This comes
     * in handy when the authorization logic is more complex than what deadbolt can support. Starting our the @Before
     * annotated controller method will first check if a user is authenticated and redirect to the login flow before
     * continuing with the rest of the authorization logic.
     */
    @Util
    protected static void loginIfNecessary(){
        if (!session.contains("username")){
            flash.put("url", "GET".equals(request.method) ? request.url : "/");
            Security.login();
        }
    }
}