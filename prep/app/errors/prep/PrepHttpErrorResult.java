package errors.prep;


import com.google.gson.Gson;
import play.exceptions.UnexpectedException;
import play.libs.MimeTypes;
import play.mvc.Http.Request;
import play.mvc.Http.Response;
import play.mvc.results.Result;

import java.util.*;


public class PrepHttpErrorResult extends Result {

    private final Map<String, List<Map<String, String>>> errors;
    private final int statusCode;

    public PrepHttpErrorResult(PrepHttpError error) {
        if (error == null) {
            throw new IllegalArgumentException("PrepHttpErrorResult must have a PrepHttpError");
        }
        this.errors = new HashMap<String, List<Map<String, String>>>();
        Map<String, String> httpErrorMessage = new HashMap<String, String>();
        httpErrorMessage.put("message", error.getMessage());

        List<Map<String, String>> errors = new ArrayList<Map<String, String>>();
        errors.add(httpErrorMessage);

        this.errors.put("error", errors);
        this.statusCode = error.getStatusCode();
    }

    @Override
    public void apply(Request request, Response response) {
        response.status = this.statusCode;

        response.contentType = MimeTypes.getContentType("xx.json");

        String errorHtml = new Gson().toJson(errors);

        try {
            response.out.write(errorHtml.getBytes(getEncoding()));
        } catch (Exception e) {
            throw new UnexpectedException(e);
        }
    }

}
