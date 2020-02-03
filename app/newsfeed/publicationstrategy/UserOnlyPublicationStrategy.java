package newsfeed.publicationstrategy;

import models.Event;
import models.Story;
import models.User;
import utils.NewsFeed;

public class UserOnlyPublicationStrategy implements PublicationStrategy {

    @Override
    public void publishStories(Event event) {
        final User relatedUser = event.relatedUser;
        final Double weight = NewsFeed.determineWeight(event);
        if(relatedUser != null){
            //publish a story for the  specific user
            Story story = new Story();
            story.event = event;
            story.user = relatedUser;
            story.affinity = NewsFeed.determineAffinity(story);
            story.weight = weight;
            story.save();
        }
    }
}
