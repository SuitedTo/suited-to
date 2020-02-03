package integration.social;

import models.User;

public interface ProviderAPI {
	
	public ProviderStatus getStatus(User user);

	/**
	 * @param user Share will be sent on behalf of this user.
	 * @param title Optional title may not show up on some networks
	 * @param comment Mandatory comment will show up on all networks
	 * @param url Href for the image
	 * @param imageUrl The image url
	 */
	public void share(User user, String title, String comment, String url, String imageUrl);
}
