package integration.social.facebook;

import integration.social.Provider;
import integration.social.Provider.Failure;
import integration.social.Provider.FailureType;

import java.util.Hashtable;
import java.util.Map;

import org.apache.http.HttpStatus;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import play.Logger;
import play.jobs.Job;
import play.libs.WS;
import play.libs.F.Promise;
import play.libs.WS.HttpResponse;
import play.libs.WS.WSRequest;
import securesocial.provider.ProviderType;
import securesocial.provider.SocialUser;

public class Facebook extends Provider{

	public final static String FB_GRAPH_URL = "https://graph.facebook.com/";

	@Override
	public void share(final SocialUser socialUser, String title, String comment,
			String url, String imageUrl) {
		Map<String, String> params = new Hashtable<String, String>();
		params.put("message", comment);
		if(imageUrl != null){
			if(url != null){
				params.put("link", url);
			}
			params.put("picture", imageUrl);
		}
		
		params.put("access_token", socialUser.accessToken);
		WSRequest request = Facebook.buildRequest("me/feed", "POST", params);
		postAsync(request, socialUser, ProviderType.facebook);
	}
	
	protected static Promise<HttpResponse> postAsync(final WSRequest request,
			final SocialUser socialUser,
			final ProviderType providerType){
		return new Job<HttpResponse>(){
			public HttpResponse doJobWithResult(){
				HttpResponse response = request.post();
				if(!response.success()){
					expressFailure(new Failure(socialUser, FailureType.UNKNOWN, providerType));
				}
				JsonElement jelement = new JsonParser().parse(response.getString());
			    JsonObject  jobject = jelement.getAsJsonObject();
			    JsonObject error = jobject.getAsJsonObject("error");
				if(error != null){
					JsonPrimitive jCode = error.getAsJsonPrimitive("code");
					int code = jCode.getAsInt();
					if(code == 190){
						expressFailure(new Failure(socialUser, FailureType.NOT_AUTHORIZED, providerType));
					} else {
						expressFailure(new Failure(socialUser, FailureType.UNKNOWN, providerType));
					}
				}
				return response;
			}
		}.now();
	}

	//stole this from the fbgraph module
	private static WSRequest buildRequest(String path, String method, Map<String, String> params) {
        StringBuilder url = new StringBuilder();
        url.append(FB_GRAPH_URL);
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        url.append(path);
        StringBuilder queryStr = new StringBuilder();
        for (Map.Entry<String, String> param : params.entrySet()) {
            if (queryStr.length() > 0) {
                queryStr.append("&");
            }
            queryStr.append(WS.encode(param.getKey()));
            queryStr.append("=");
            queryStr.append(WS.encode(param.getValue()));
        }
        if (method != null && method.toUpperCase().equals("POST")) {
            Logger.debug("Making a POST request to URL %s with body set to %s", url.toString(), queryStr.toString());
            return WS.url(url.toString()).body(queryStr.toString()).mimeType("multipart/form-data");
        } else {
            url.append("?");
            url.append(queryStr.toString());
            Logger.debug("Making a GET request to URL %s", url.toString());
            return WS.url(url.toString());
        }
    }

}
