package controllers;

import com.google.gson.Gson;
import enums.EventType;
import models.Story;
import models.User;
import newsfeed.display.StoryDisplay;
import newsfeed.metadata.StoryMetadata;
import play.Logger;
import play.mvc.Controller;
import utils.NewsFeed;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides access to a JSON representation of the news feed that is intended to be rendered in the UI through a template.
 */
public class NewsFeedHelper extends Controller {
    private static int DEFAULT_PAGE = 1;
    private static int DEFAULT_MAX = 10;


    public static void getNewsFeed(Integer page, Integer max, Long specifiedUserId){
        User specifiedUser = null;
        if(specifiedUserId != null){
            specifiedUser = User.findById(specifiedUserId);
        }
        final User user = Security.connectedUser();  //may be null
        if(page == null || max == null){
            page = DEFAULT_PAGE;
            max = DEFAULT_MAX;
        }

        List<Story> stories;
        if(specifiedUser == null){
            stories = NewsFeed.getNewsFeed(user, page, max);
        } else {
            stories = NewsFeed.getNewsFeed(specifiedUser, page, max, true);
        }

        //from the list of stories build the JSON Representation
        List<StoryDisplay> storyDisplayList = new ArrayList<StoryDisplay>();
        boolean thirdPersonMessages = specifiedUser != null;
        for (Story story : stories) {

            StoryDisplay storyDisplay = getStoryDisplay(story, user, thirdPersonMessages);
            if(storyDisplay != null){
                storyDisplayList.add(storyDisplay);
            }
        }
        Map<String, Object> results = new LinkedHashMap<String, Object>();
        results.put("data", storyDisplayList);
        renderJSON(new Gson().toJson(results));
    }


    /**
     * Gets the appropriate StoryDisplay instance for the given Story and User
     * @param story
     * @param user
     * @return
     */
    private static StoryDisplay getStoryDisplay(Story story, User user, boolean thirdPersonMessages){
        Class storyDisplayClass = null;

        EventType eventType = story.event != null ? story.event.eventType : null;
        if(eventType != null){
                storyDisplayClass = eventType.getDefaultStoryDisplayType();

        } else {
            //eventType is null so look at the metadata on the Story itself to figure out the type
                StoryMetadata storyMetadata = null;
            try {
                storyMetadata = new Gson().fromJson(story.metadata, StoryMetadata.class);
            } catch (Exception e) {
                Logger.error("Could not create StoryMetadataFrom string: " + story.getLogicalMetadata(), e);
            }
            storyDisplayClass = storyMetadata != null && storyMetadata.getStoryType() != null ?
                    storyMetadata.getStoryType().getDefaultStoryDisplayType() : null;
        }

        StoryDisplay storyDisplay = null;
        try {
            if(storyDisplayClass != null){
                Constructor constructor = storyDisplayClass.getConstructor(User.class, Story.class, Boolean.class);
                storyDisplay = (StoryDisplay) constructor.newInstance(user, story, thirdPersonMessages);
                if(!storyDisplay.isActive()){
                	Logger.error("Display for " + story + " is not active.");
                	return null;
                }
            }
        } catch (Exception e) {
            Logger.error("Could not instantiate StoryDisplay class. %s  will not be added to the news feed: %s", story, e.getMessage());
            e.printStackTrace();
        }

        return storyDisplay;

    }


}
