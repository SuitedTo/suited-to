package newsfeed.publicationstrategy;

import models.Company;
import models.Event;
import models.Story;
import models.User;
import utils.NewsFeed;

public interface PublicationStrategy {

    public void publishStories(Event event);

}
