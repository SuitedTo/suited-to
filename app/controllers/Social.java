package controllers;

import integration.social.ProviderStatus;
import integration.social.SocialNetwork;
import integration.social.SocialNetwork.SocialNetworkException;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import models.SocialIdentity;
import models.User;
import play.Logger;
import play.jobs.Job;
import securesocial.provider.IdentityProvider;
import securesocial.provider.ProviderRegistry;
import securesocial.provider.ProviderType;
import db.jpa.S3Blob;


public class Social extends ControllerBase{


	/**
	 * Just for testing
	 * 
	 * @param providerType
	 */
	public static void shareProfilePic(final ProviderType providerType){
		final User user = Security.connectedUser();
		if(user.picture != null){
			share(providerType,
					user.displayName,
					"Here's my SuitedTo profile picture!\nTesting-" + System.currentTimeMillis(),
					"https://dev.suitedto.com/",
					user.picture.getPublicUrl().toString());
		}
	}
	
	/**
	 * Asynchronously share to a social network.
	 * 
	 * @param providerType The provider. If this parameter is null then share to all providers.
	 * @param title The title. Optional - may not be supported by all providers.
	 * @param comment The comment. Mandatory - supported by all providers.
	 * @param imgHref The link associated with this share. Optional - supported by all providers.
	 * @param imgUrl The URL of an image. This must be one of our public images (static or s3).
	 * Optional - supported by all providers.
	 */
	public static void share(final ProviderType providerType, final String title, final String comment,
			final String imgHref, final String imgUrl){
		final User user = Security.connectedUser();
		
		if(providerType == null){
			SocialNetwork.shareALL(user, title, comment, imgHref, imgUrl);
			return;
		}
		final SocialNetwork network = SocialNetwork.INSTANCE(providerType);

		if ( network == null ) {
			Logger.error("No such network: %s", providerType.name());
			return;
		}
		
		final boolean validURL[] = {false};
		try {
			URL url = new URL(imgUrl);
			if(S3Blob.exists(url) || new File(imgUrl).exists()){
				validURL[0] = true;
			}
		} catch (MalformedURLException e) {
		}
		
		new Job(){
			public void doJob(){
				try{
					network.share(user,
							title,
							comment,
							imgHref,
							validURL[0]?imgUrl:null);
				}catch(SocialNetworkException e){
					
				}

			}
		}.now();
	}
	
	public static void getSocialIdentity(ProviderType providerType){
		User user = Security.connectedUser();
		if(user != null){
			SocialIdentity si = SocialIdentity.findFirst("byUserAndProvider", user, providerType);
			if(si != null){
				renderJSON("{\"avatarUrl\":\""+ si.avatarUrl + "\"}");
			}
		}
		renderJSON("{}");
	}

	/**
	 * Connect to the given provider.
	 * 
	 * @param providerType   The provider type as selected by the user in the login page
	 * @see ProviderType
	 * @see IdentityProvider
	 */
	public static void connect(ProviderType providerType, String redirectUrl) {
		SocialNetwork network = SocialNetwork.INSTANCE(providerType);
		notFoundIfNull(network);
		try {
			network.connect();
		} catch ( SocialNetworkException e ) {
			flash.error(e.getMessage());
		}
		if(redirectUrl == null){
			Application.home();
		}else{
			redirect(redirectUrl);
		}
	}

	/**
	 *
	 * Disconnect from the given provider.
	 *
	 * @param providerType   The provider type as selected by the user in the login page
	 * @see ProviderType
	 * @see IdentityProvider
	 */
	public static void disconnect(ProviderType providerType, String redirectUrl) {
		SocialNetwork network = SocialNetwork.INSTANCE(providerType);

		notFoundIfNull(network);

		network.disconnect();

		if(redirectUrl == null){
			Application.home();
		}else{
			redirect(redirectUrl);
		}
	}

	/**
	 * Get a list of provider status objects with the connected user as context
	 * 
	 * @return A list of provider status objects
	 */
	public static List<ProviderStatus> getStatusAll(List<ProviderType> providerTypes){
		List<ProviderStatus> result = new ArrayList<ProviderStatus>();
		User user = Security.connectedUser();
		Collection<IdentityProvider> iProviders = ProviderRegistry.all();
		for(IdentityProvider ip : iProviders){			
			if((providerTypes == null) || (providerTypes.contains(ip.type))){
				SocialNetwork nw = SocialNetwork.INSTANCE(ip.type);
				if(nw != null){
					result.add(nw.getStatus(user));
				}else{
					result.add(ProviderStatus.UNSUPPORTED());
				}
			}
		}
		return result;
	}

	public static ProviderStatus getStatus(ProviderType providerType){
		if(providerType == null){
			return null;
		}
		User user = Security.connectedUser();
		IdentityProvider ip = ProviderRegistry.get(providerType);
		SocialNetwork nw = SocialNetwork.INSTANCE(ip.type);
		if(nw != null){
			return nw.getStatus(user);
		}else{
			return ProviderStatus.UNSUPPORTED();
		}
	}
}
