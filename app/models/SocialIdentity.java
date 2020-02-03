package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import play.libs.OAuth.ServiceInfo;

import securesocial.provider.AuthenticationMethod;
import securesocial.provider.ProviderType;
import securesocial.provider.SocialUser;
import securesocial.provider.UserId;

@Entity
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class SocialIdentity extends ModelBase{
	
	/**
	 * The suitedTo user
	 */
	@ManyToOne
	public User user;

	/**
     * The id the user has in a external service.
     */
    public String externalId;

    /**
     * The provider this user belongs to.
     */
    public ProviderType provider;
    
    /**
     * The user full name.
     */
    public String displayName;

    /**
     * The user's email
     */
    public String email;

    /**
     * A URL pointing to an avatar
     */
    public String avatarUrl;

    /**
     * The time of the last login.  This is set by the SecureSocial controller.
     */
    public Date lastAccess;

	
	public AuthenticationMethod authenticationMethod;
	
	
	
	/***********************************************
	 * 
	 * Information relative to an OAuth 1.0 provider
	 * 
	 */
	public String requestTokenURL;
	public String accessTokenURL;
	public String authorizationURL;
	public String consumerKey;
	public String consumerSecret;
	/*********************************************/
	
	/**
     * The OAuth1 token (available when authMethod is OAUTH1 or OPENID_OAUTH_HYBRID)
     */
    public String token;

    /**
     * The OAuth1 secret (available when authMethod is OAUTH1 or OPENID_OAUTH_HYBRID)
     */
    public String secret;

    /**
     * The OAuth2 access token (available when authMethod is OAUTH2)
     */
    public String accessToken;
    
    public SocialUser extractSocialUser(){
    	SocialUser su = new SocialUser();
    	
    	su.accessToken = accessToken;    	
    	su.authMethod = authenticationMethod;    	
    	su.avatarUrl = avatarUrl; 	
    	su.displayName = displayName;
    	su.email = email;
    	su.id = new UserId();
    	su.id.id = externalId;
    	su.id.provider = provider;
    	su.lastAccess = lastAccess;
    	su.secret = secret;
    	su.token = token;
    	
    	su.serviceInfo = new ServiceInfo(requestTokenURL,
    			accessTokenURL,
    			authorizationURL,
    			consumerKey,
    			consumerSecret);
    	
    	return su;
    }
    
    public static SocialIdentity create(User user, SocialUser socialUser){
    	if(socialUser == null){
    		return null;
    	}
    	
    	SocialIdentity si = new SocialIdentity();
    	
    	si.user = user;    	
    	si.accessToken = socialUser.accessToken;    	
    	si.authenticationMethod = socialUser.authMethod;    	
    	si.avatarUrl = socialUser.avatarUrl; 	
    	si.displayName = socialUser.displayName;
    	si.email = socialUser.email;
    	si.externalId = socialUser.id.id;
    	si.provider = socialUser.id.provider;
    	si.lastAccess = socialUser.lastAccess;
    	si.secret = socialUser.secret;
    	si.token = socialUser.token;
    	
    	if(socialUser.serviceInfo != null){
    		si.accessTokenURL = socialUser.serviceInfo.accessTokenURL;
    		si.authorizationURL = socialUser.serviceInfo.authorizationURL;
    		si.requestTokenURL = socialUser.serviceInfo.requestTokenURL;
    		si.consumerKey = socialUser.serviceInfo.consumerKey;
    		si.consumerSecret = socialUser.serviceInfo.consumerSecret;
    	}
    	
    	return si;
    }
}
