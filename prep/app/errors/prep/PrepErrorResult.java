package errors.prep;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import play.Play;
import play.exceptions.UnexpectedException;
import play.libs.MimeTypes;
import play.mvc.Http;
import play.mvc.Http.Request;
import play.mvc.Http.Response;
import play.mvc.Scope;
import play.mvc.results.Result;
import play.templates.TemplateLoader;

/**
 * Mechanism for passing server-side errors back to the client as JSON data
 */
public class PrepErrorResult extends Result {

    /**
     * PrepErrors represented by this PrepErrorResult, the Map structure here is really just facilitating mapping to the
     * correct JSON format.
     */
    private final Map<String, PrepError[]> errors;

    /**
     * Creates a new PrepErrorResult with one to many PrepErrors
     * @param errors PrepError instances to be represented by this PrepErrorResult, varargs style
     * @throws IllegalArgumentException if no errors are specified
     */
    public PrepErrorResult(PrepError... errors) {
        if(errors == null || errors.length == 0){
            throw new IllegalArgumentException("PrepErrorResult must have at least one PrepError");
        }
        this.errors = new HashMap<String, PrepError[]>();
        this.errors.put("error", errors);
    }

    /**
     * Sends JSON representation of the error messages to the HTTP response
     * @param request Request
     * @param response Response
     */
    @Override
    public void apply(Request request, Response response) {
        response.status = Http.StatusCode.OK;

        response.contentType = MimeTypes.getContentType("xx.json");

        String errorHtml = new Gson().toJson(errors);

        try {
            response.out.write(errorHtml.getBytes(getEncoding()));
        } catch (Exception e) {
            throw new UnexpectedException(e);
        }
    }

}
