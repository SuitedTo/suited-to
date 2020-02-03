package newsfeed.display;

import models.Story;
import models.User;
import play.i18n.Messages;

public abstract class ShareableStoryDisplay extends StoryDisplay {
    private static final String STANDARD_SHARE_MESSAGE_PREFIX = "newsfeed.standardShareMessage.";
    private static final String TWITTER_SHARE_MESSAGE_PREFIX = "newsfeed.twitterShareMessage.";

    /**
     * Message to use when sharing the story with Facebook, LinkedIn, or Google+ if anyone actually ever shares anything
     * on Google+ - same security warning as with message
     */
    private final ShareMessage standardShareMessage;

    /**
     * Message to user when sharing the story on Twitter - same security warning as message
     */
    private final ShareMessage twitterShareMessage;


    protected ShareableStoryDisplay(User userInContext, Story story, Boolean thirdPersonMessages) {
        super(userInContext, story, thirdPersonMessages);
        this.standardShareMessage = buildStandardShareMessage();
        this.twitterShareMessage = buildTwitterShareMessage();
    }



    protected abstract ShareMessage buildStandardShareMessage();
    protected abstract ShareMessage buildTwitterShareMessage();



    protected String getStandardShareMessageKey() {
        return STANDARD_SHARE_MESSAGE_PREFIX + type;
    }

    protected String getTwitterShareMessageKey() {
        return TWITTER_SHARE_MESSAGE_PREFIX + type;
    }

    protected ShareMessage buildDefaultShareMessage(String... args){
        if(!shareMessageNeeded()) {
            return null;
        }
        return new ShareMessage(Messages.get(getStandardShareMessageKey(), args), getUserProfileUrl());
    }

    protected ShareMessage buildDefaultTwitterShareMessage(String... args){
        if(!shareMessageNeeded()) {
            return null;
        }
        return new ShareMessage(Messages.get(getTwitterShareMessageKey(), args), getUserProfileUrl());
    }
}
