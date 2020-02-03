package integration.social;


/***********************************************************
 * Please don't import anything from the models package    */
import org.apache.http.HttpStatus;
import integration.social.facebook.Facebook;
import integration.social.linkedin.LinkedIn;
import integration.social.twitter.Twitter;
import play.Logger;
import play.jobs.Job;
import play.libs.F.Promise;
import play.libs.WS.HttpResponse;
import play.libs.WS.WSRequest;
import securesocial.provider.ProviderType;
import securesocial.provider.SocialUser;
/***********************************************************/


public abstract class Provider{
	
	public abstract void share(SocialUser socialUser, String title, String comment, String url, String imageUrl);
	
	
	protected static final void expressFailure(Failure failure){
		SocialNetwork.INSTANCE(failure.providerType).fail(failure);
	}
	
	public enum FailureType{
		UNKNOWN,
		NOT_AUTHORIZED
	}
	
	public static class Failure{
		private final SocialUser socialUser;
		private final FailureType failureType;
		private final ProviderType providerType;
		public Failure(SocialUser socialUser, FailureType failureType, ProviderType providerType) {
			super();
			this.socialUser = socialUser;
			this.failureType = failureType;
			this.providerType = providerType;
		}
		public SocialUser getSocialUser() {
			return socialUser;
		}
		public FailureType getFailureType() {
			return failureType;
		}
		public ProviderType getProviderType() {
			return providerType;
		}
		
		public String getMessage(){
			switch(failureType){
			case NOT_AUTHORIZED:
				return "Your connection to " + providerType.name() +
					" has been lost. It seems that SuitedTo is no longer authorized.";
			default:
				return "Your connection to " + providerType.name() +
						" has been lost.";
		}
		}
	}
	
	protected static Promise<HttpResponse> postAsync(final WSRequest request,
			final SocialUser socialUser,
			final ProviderType providerType){
		return new Job<HttpResponse>(){
			public HttpResponse doJobWithResult(){
				HttpResponse response = request.post();
				if(!response.success()){
					if(response.getStatus() == HttpStatus.SC_UNAUTHORIZED){
						expressFailure(new Failure(socialUser, FailureType.NOT_AUTHORIZED, providerType));
					} else {
						expressFailure(new Failure(socialUser, FailureType.UNKNOWN, providerType));
					}
				}
				return response;
			}
		}.now();
	}

	protected static HttpResponse post(final WSRequest request,
			final SocialUser socialUser,
			final ProviderType providerType){
		HttpResponse response = request.post();
		if(!response.success()){
			if(response.getStatus() == HttpStatus.SC_UNAUTHORIZED){
				expressFailure(new Failure(socialUser, FailureType.NOT_AUTHORIZED, providerType));
			} else {
				expressFailure(new Failure(socialUser, FailureType.UNKNOWN, providerType));
			}
		}
		return response;
	}

	static class Registry{

		private static LinkedIn linkedInProvider = new LinkedIn();
		private static Twitter twitterProvider = new Twitter();
		private static Facebook facebookProvider = new Facebook();
	
		static Provider get(ProviderType providerType){
			if(providerType == null){
				return null;
			}
			switch(providerType){
				case linkedin: return linkedInProvider;
				case twitter: return twitterProvider;
				case facebook: return facebookProvider;
			}
			Logger.error("No provider available for type: %s" + providerType.name());
			return null;
		}
	}
}