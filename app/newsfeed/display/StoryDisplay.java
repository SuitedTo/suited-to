package newsfeed.display;

import models.Event;
import models.Story;
import models.User;
import org.apache.commons.lang.StringEscapeUtils;
import play.mvc.Router;

import java.util.HashMap;
import java.util.Map;

/**
 * Base class for transferring Story model objects to a JSON format consumable by client side rendering engine. Fields
 * with transient modifier will not be rendered in the JSON output.  The Gson processor will render private fields so
 * no need to expose all fields.
 */
public abstract class StoryDisplay {

    /*************************************************************
     * Constants                                                 *
     *************************************************************/

    private static final String MESSAGE_PREFIX = "newsfeed.message.";
    protected static final String SELF_TYPE = "user";
    private static final String COMPANY_TYPE = "company";
    private static final String ALL_TYPE = "all";


    /*************************************************************
     * Transient fields used for processing                      *
     *************************************************************/

    /**
     * The user that is viewing the Story display, not necessarily the user that the story is related to. Newsfeed may
     * be shown in an unauthenticated session so userInContext may potentially be null
     */
    protected final transient User userInContext;

    /**
     * The Story being displayed
     */
    protected final transient Story story;


    /*************************************************************
     * Fields to be rendered in JSON Output                      *
     *************************************************************/

    /**
     * Primary key of the Story itself
     */
    private final Long storyId;

    /**
     * The "type" of Story.  Will be the same as the EventType if an event is related to the story but not all stories
     * have Events associated so some logic will need to occur to figure this field out.  Will be used by front end code
     * to specify css classes and potentially other "stuff" as well.
     */
    protected final String type;

    /**
     * The relationship of this story to the userInContext - "self", "company", or "all"
     */
    private final String relationship;

    /**
     * The primary message to be rendered in the Story display - may include html values for links etc as specified in
     * the messages files.  The message should not include any unsanitized user-generated content since we're going to
     * have to output raw html.
     */
    private final String message;

    /**
     * Indicates that share messages should be shown in the user interface
     */
    private final boolean shouldShowShareMessages;


    /**
     * number of milliseconds since the epoch when this story was created
     */
    private final long timestamp;

    private final String relatedUserPictureUrl;


    /**
     * Indicates that Messages should always be rendered in the third person
     */
    protected final boolean thirdPersonMessages;


    /*************************************************************
     * Constructors                                              *
     *************************************************************/

    protected StoryDisplay(User userInContext, Story story) {
        this(userInContext, story, false);
    }

    protected StoryDisplay(User userInContext, Story story, Boolean thirdPersonMessages){
        //set the argument-bound fields first these will be needed to set subsequent fields
        this.userInContext = userInContext;
        this.story = story;
        this.thirdPersonMessages = thirdPersonMessages;

        this.storyId = story.id;
        this.type = determineType();
        this.relationship = getRelationshipDescription();
        this.message = buildMessage();
        this.shouldShowShareMessages = shareMessageNeeded();
        this.timestamp = story.created.getTime();
        this.relatedUserPictureUrl = determineRelatedUserPictureUrl();

    }


    /*************************************************************
     * Abstract Methods                                          *
     *************************************************************/

    protected abstract String buildMessage();


    /*************************************************************
     * Helper/Utility methods                                    *
     *************************************************************/

    private String determineRelatedUserPictureUrl(){
        String url = null;
        Event event = story.event;
        if(event != null){
            User relatedUser = event.relatedUser;
            if(relatedUser != null){
                url = relatedUser.getPublicPictureUrl();
            }
        }
        return url;
    }

    protected boolean shareMessageNeeded(){
        return userInContext != null && userInContext.isCommunityMember() && this.relationship.equals(SELF_TYPE) && !thirdPersonMessages;
    }

    protected String getUserProfileUrl() {
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("id", story.event.relatedUser.id);
        return Router.getFullUrl("Community.profile", args);
    }

    protected String determineType(){
        String type = null;
        Event event = story.event;
        if(event != null){
            type = event.eventType.name();
        }

        return type;
    }

    protected String getMessageKey() {
        return MESSAGE_PREFIX + type + "." + getRelationshipDescription();
    }



    /**
     * A description of how the Story might be related to the current user.  Mostly useful for testing purposes
     * @return String
     */
    public String getRelationshipDescription(){
        if(story.allUsers || thirdPersonMessages){
            return ALL_TYPE;
        } else if(story.company != null){
            return COMPANY_TYPE;
        } else if (story.user != null){
            return SELF_TYPE;
        }

        return null;
    }

    protected boolean useFirstPersonMessages() {
        return getRelationshipDescription().equals(SELF_TYPE) && !thirdPersonMessages;
    }

    /**
     * Gets the appropriate name of the user to display in the newsfeed Assumes that an event is associated with this
     * StoryDisplay's associated story
     * @return
     */
    protected String getEventRelatedUserDisplay() {
        if(story.event == null){
            throw new IllegalStateException("StoryDisplay.getEventRelatedUserDisplay called with a story without an " +
                    "event:  " + story);
        }
        return StringEscapeUtils.escapeHtml(story.event.getRelatedUserNameForDisplay(userInContext));
    }
    
    public boolean isActive(){
    	return true;
    }
}

