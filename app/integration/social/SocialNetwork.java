package integration.social;

import integration.social.Provider.Failure;

import java.util.Hashtable;
import java.util.Map;

import models.Alert;
import models.SocialIdentity;
import models.User;
import play.Logger;
import play.i18n.Messages;
import play.mvc.results.Redirect;
import play.utils.FastRuntimeException;
import securesocial.provider.IdentityProvider;
import securesocial.provider.ProviderRegistry;
import securesocial.provider.ProviderType;
import securesocial.provider.SocialUser;
import controllers.Alerts;
import controllers.Security;
import controllers.securesocial.SecureSocial;
import enums.AlertType;

public class SocialNetwork implements ProviderAPI {

	private static final String ERROR = "error";
	private static final String SECURESOCIAL_AUTH_ERROR = "securesocial.authError";
	private static final String ORIGINAL_URL = "originalUrl";
	private static final String GET = "GET";
	private static final String ROOT = "/";


	private IdentityProvider identityProvider;
	private Provider provider;
	private static Map<ProviderType, SocialNetwork> networks =
			new Hashtable<ProviderType, SocialNetwork>();

	private SocialNetwork(IdentityProvider identityProvider, Provider provider){
		this.identityProvider = identityProvider;
		this.provider = provider;
	}

	public static SocialNetwork INSTANCE(ProviderType providerType){
		if(providerType == null){
			return null;
		}
		SocialNetwork network = networks.get(providerType);
		if(network == null){
			IdentityProvider identityProvider = ProviderRegistry.get(providerType);

			if ( identityProvider == null ) {
				Logger.error("Provider type not recognized: %s" + providerType.name());
				return null;
			}

			network = new SocialNetwork(identityProvider, Provider.Registry.get(providerType));

			networks.put(providerType,network);
		}
		return network;
	}
	
	public void connect() throws SocialNetworkException{
		SecureSocial.authenticate(identityProvider.type);
//		getIdentity(Security.connectedUser());
//		try {
//			identityProvider.authenticate();
//		} catch (Redirect redirect){
//			throw redirect;
//		} catch ( Exception e ) {
//			Logger.error(e, "Error authenticating user %s", e.getMessage());
//			throw new SocialNetworkException(Messages.get(SECURESOCIAL_AUTH_ERROR));
//		}
	}
	
	public boolean isConnected(){
		return getIdentity(Security.connectedUser()) != null;
	}

	public void disconnect() {	       
		disconnect(Security.connectedUser(), identityProvider.type);
	}
	
	void fail(Failure failure){
		SocialIdentity si = getIdentity(failure.getSocialUser());
		if(si != null){
			Alerts.raise(new Alert(si.user,
				AlertType.ERROR,
				failure.getMessage() +
				" You can reconnect on your preferences page."));
			disconnect(si.user, identityProvider.type);
		}
		
	}
	
	private static void disconnect(User user, ProviderType providerType) {	    
		SocialIdentity existingIdentity = SocialIdentity.findFirst("byUserAndProvider", user, providerType);
		if(existingIdentity != null){
			existingIdentity.delete();
		}
	}

	
	public static class SocialNetworkException extends FastRuntimeException{
		public SocialNetworkException(String message){
			super(message);
		}
	}
	
	public static class ProviderNotConnectedException extends SocialNetworkException{
		public ProviderNotConnectedException(String message){
			super(message);
		}
	}
	
	public static class ProviderNotSupportedException extends SocialNetworkException{
		public ProviderNotSupportedException(String message){
			super(message);
		}
	}
	
	SocialIdentity getIdentity(SocialUser socialUser){
		return SocialIdentity.findFirst("byExternalId", socialUser.id.id);
	}
	
	SocialIdentity getIdentity(User user) {
		
		return SocialIdentity.findFirst("byUserAndProvider", user, identityProvider.type);
	}

	void check(User user) throws SocialNetworkException {
		
		if (provider == null){
			throw new ProviderNotSupportedException("No API available for " + identityProvider.type.name());
		}
		
		if(getIdentity(user) == null){
			throw new ProviderNotConnectedException("Not connected");
		}
	}


	//---------Provider delegation...

	public static void shareALL(User user, String title, String comment,
			String url, String imageUrl) {
		for(SocialNetwork network : networks.values()){
			network.share(user, title, comment, url, imageUrl);
		}
	}

	@Override
	public void share(User user,
			String title, String comment, String url, String imageUrl) {
		check(user);
		provider.share(getIdentity(user).extractSocialUser(), title, comment, url, imageUrl);
	}

	@Override
	public ProviderStatus getStatus(User user) {
		boolean connected = true, supported = true;
		try{
			check(user);
		} catch (ProviderNotSupportedException ns){
			supported = false;
		}  catch (ProviderNotConnectedException nc){
			connected = false;
		}
		SocialIdentity identity = getIdentity(user);
		return new ProviderStatus(supported, connected);
	}

}
