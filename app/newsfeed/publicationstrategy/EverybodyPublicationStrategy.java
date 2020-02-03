package newsfeed.publicationstrategy;

import models.Company;
import models.Event;
import models.Story;
import models.User;
import utils.NewsFeed;

public class EverybodyPublicationStrategy implements PublicationStrategy {
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

            Company company = relatedUser.company;
            if(company != null){
                //publish a story for the company associated with the user
                Story companyStory = new Story();
                companyStory.event = event;
                companyStory.company = company;
                companyStory.affinity = NewsFeed.determineAffinity(companyStory);
                companyStory.weight = weight;
                companyStory.save();
            }
        }

        /*publish the event for everyone if the story is not related to a specific user or if the related user is
        a community member*/
        if(relatedUser == null || relatedUser.isCommunityMember()) {
            Story everybodyStory = new Story();
            everybodyStory.event = event;
            everybodyStory.allUsers = true;
            everybodyStory.affinity = NewsFeed.determineAffinity(everybodyStory);
            everybodyStory.weight = weight;
            everybodyStory.save();
        }
    }
}
