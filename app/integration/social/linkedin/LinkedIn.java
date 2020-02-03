package integration.social.linkedin;

import org.apache.http.HttpStatus;

import integration.social.Provider;
import integration.social.ProviderStatus;
import integration.social.Provider.Failure;
import integration.social.Provider.FailureType;
import models.SocialIdentity;
import play.jobs.Job;
import play.libs.WS;
import play.libs.F.Promise;
import play.libs.WS.HttpResponse;
import play.libs.WS.WSRequest;
import securesocial.provider.ProviderType;
import securesocial.provider.SocialUser;

import com.google.gson.JsonObject;

public class LinkedIn extends Provider{

	private static final String SHARE = "https://api.linkedin.com/v1/people/~/shares";

	public void share(final SocialUser socialUser, String title, String comment, String url, String imageUrl){

		JsonObject payload = new JsonObject();
		JsonObject contentObject = new JsonObject();
		if(title != null){
			contentObject.addProperty("title", title);
		}
		if(imageUrl != null){
			if(url != null){
				contentObject.addProperty("submitted-url",url);
			}
			contentObject.addProperty("submitted-image-url", imageUrl);
		}

		if(comment != null){
			payload.addProperty("comment", comment);
		}
		payload.add("content", contentObject);

		JsonObject visibilityObject = new JsonObject();
		visibilityObject.addProperty("code", "anyone");

		payload.add("visibility", visibilityObject);

		WSRequest request = WS.url(SHARE);
		request.setHeader("Content-Type", "application/json");
		request.setHeader("x-li-format", "json");

		request.body(payload);

		request = request.oauth(socialUser.serviceInfo,socialUser.token, socialUser.secret);

		postAsync(request, socialUser, ProviderType.linkedin);
		
	}
	
	
}
