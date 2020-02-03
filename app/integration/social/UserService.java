package integration.social;

import controllers.Security;
import models.SocialIdentity;
import models.User;
import securesocial.provider.SocialUser;
import securesocial.provider.UserId;
import securesocial.provider.UserServiceDelegate;

public class UserService implements UserServiceDelegate{

    public SocialUser find(UserId id) {
    	SocialIdentity si = SocialIdentity.findFirst("byExternalIdAndProvider", id.id, id.provider);
    	if(si == null){
    		return null;
    	}
    	return si.extractSocialUser();
    }

    public SocialUser find(String email) {
    	SocialIdentity si = SocialIdentity.findFirst("byEmail", email);
    	
    	if(si == null){
    		return null;
    	}
    	return si.extractSocialUser();
    }

    public void save(SocialUser socialUser) {
    	User user = Security.connectedUser();
    	SocialIdentity si = SocialIdentity.findFirst("byUserAndProvider", user, socialUser.id.provider);
    	if(si == null){
    		si = SocialIdentity.create(user, socialUser);
    	}
    	user.socialIdentities.add(si);
        user.save();
    	
    }

	@Override
	public String createActivation(SocialUser user) {
		return null;
	}

	@Override
	public boolean activate(String uuid) {
		return false;
	}

	@Override
	public String createPasswordReset(SocialUser user) {
		return null;
	}

	@Override
	public SocialUser fetchForPasswordReset(String username, String uuid) {
		return null;
	}

	@Override
	public void disableResetCode(String username, String uuid) {
		
	}

	@Override
	public void deletePendingActivations() {
		
	}
}
