package controllers.prep.rest;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import play.Play;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Scope;
import play.mvc.With;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import common.utils.JsonUtil;
import common.utils.prep.SecurityUtil;

import controllers.prep.auth.Authorization;
import errors.prep.PrepError;
import errors.prep.PrepErrorResult;
import errors.prep.PrepErrorType;
import errors.prep.PrepHttpError;
import errors.prep.PrepHttpErrorResult;

@With(Authorization.class)
public abstract class PrepController extends Controller {
	
	protected static final SimpleDateFormat standardDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
	
    /** Called before every request to ensure that HTTPS is used. */
    @Before
    public static void redirectToHttps() {
        //if it's not secure, but Heroku has already done the SSL processing then it might actually be secure after all
        if (!request.secure && request.headers.get("x-forwarded-proto") != null) {
            request.secure = request.headers.get("x-forwarded-proto").values.contains("https");
        }

        //redirect if it's not secure
        if (!request.secure && Play.configuration.getProperty("force.ssl").equals("true")) {
            throw new PrepHttpErrorResult(PrepHttpError.SSL_ERROR);
        }
    }
	
    protected static void renderRefinedJSON(Object object){
        Map <String, Object> result = new HashMap<String, Object>();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setDateFormat(standardDateFormat);
        
        result.put("data", objectMapper.valueToTree(object));

        renderJSON(JsonUtil.toJson(result).toString());
    }

	private static JsonElement drillDown(String[] path, JsonElement root){
		if((path == null) || (path.length == 0) || (path[0].length() == 0)){
			return root;
		}

		if(root.isJsonArray()){
			long id;
			try{
				id = Long.parseLong(path[0]);
			}catch(NumberFormatException nfe){
				return new JsonParser().parse("{}");
			}

			JsonArray a = root.getAsJsonArray();
			Iterator<JsonElement> it = a.iterator();
			while(it.hasNext()){
				JsonElement next = it.next();
				JsonElement idElement = next.getAsJsonObject().get("id");
				if(idElement != null){
					if(idElement.getAsLong() == id){
						return drillDown(Arrays.copyOfRange(path, 1, path.length), next);
					}
				}
			}
			return new JsonParser().parse("{}");
		}

		JsonElement e = root.getAsJsonObject().get(path[0]);
		if(e.isJsonArray()){
			if(path.length > 1){
				return drillDown(Arrays.copyOfRange(path, 1, path.length), e);
			} else {
				return e;
			}
		} else {
			return e;
		}
	}

	private static JsonElement refine(String[] fields, JsonObject object){
		if((fields == null) || fields.length == 0){
			return object;
		}

		JsonObject o = new JsonParser().parse("{}").getAsJsonObject();
		for(String field : fields){
			JsonElement e = object.get(field);
			if(e != null){
				o.add(field, e);
			}
		}
		return o;
	}

    /**
     *
     * @param path
     * @param fields
     * @param dataElement JSON representation of a DTO object
     * @return
     */
	private static String extractJSON(String path, String fields, JsonElement dataElement){
		if(dataElement == null){
			return "{}";
		}

		if(path != null){
			String[] pathParts = path.split("/");
            dataElement = drillDown(pathParts, dataElement);
		}

		if(!dataElement.isJsonArray() && (fields != null)){
            dataElement = refine(fields.split(","), dataElement.getAsJsonObject());
		}

		JsonObject result = new JsonObject();
		result.add("data", dataElement);

		return result.toString();

	}

    protected static void renderRefinedJSON(JsonElement data) {
        renderJSON(extractJSON(params.get("path"), params.get("fields"), data));

    }

	protected static void emptyObject(){
		renderJSON("{\"data\":{}}");
	}

	protected static void emptyArray(){
		renderJSON("{\"data\":[]}");
	}



	protected static void authenticationError(){
		throw new PrepErrorResult(new PrepError(PrepErrorType.AUTHENTICATION_ERROR));
	}
}
