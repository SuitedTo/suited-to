package newsfeed.display;

import com.google.gson.Gson;
import models.Story;
import models.User;
import models.UserBadge;
import newsfeed.metadata.UserBadgeEventMetadata;
import org.apache.commons.lang.StringEscapeUtils;
import play.i18n.Messages;

public class BadgeStoryDisplay extends ShareableStoryDisplay {

    private final String badgeTitle;
    private final String badgeImageUrl;
    private transient UserBadge.BadgeInfo badgeInfo;
    private transient UserBadge userBadge;
    private transient UserBadgeEventMetadata metadata;

    public BadgeStoryDisplay(User userInContext, Story story, Boolean thirdPersonMessages) {
        super(userInContext, story, thirdPersonMessages);
        UserBadge badge = getUserBadge();
        final UserBadge.BadgeInfo badgeInfo = (badge == null)?null:badge.getInfo();
        this.badgeTitle = (badgeInfo == null)?null:badgeInfo.title;
        this.badgeImageUrl = (badgeInfo == null)?null:badgeInfo.icon;
    }

    private UserBadgeEventMetadata getMetadata() {
        if(this.metadata == null){
            this.metadata = new Gson().fromJson(story.event.metadata, UserBadgeEventMetadata.class);
        }
        return this.metadata;
    }

    private UserBadge getUserBadge() {
        if(this.userBadge == null){
            this.userBadge = UserBadge.findById(getMetadata().getUserBadgeId());
        }
        return this.userBadge;
    }

    private UserBadge.BadgeInfo getBadgeInfo() {
        if(this.badgeInfo == null){
        	UserBadge badge = getUserBadge();
        	if(badge == null){
        		return null;
        	}
            this.badgeInfo = getUserBadge().getInfo();
        }
        return this.badgeInfo;
    }

    @Override
    protected String buildMessage() {
        UserBadge.BadgeInfo badgeInfo = getBadgeInfo();
        if(badgeInfo == null){
        	return null;
        }
        if(useFirstPersonMessages()){
            return Messages.get(getMessageKey(), badgeInfo.title);
        } else {
            return Messages.get(getMessageKey(), getEventRelatedUserDisplay(), badgeInfo.title);
        }
    }



    @Override
    protected ShareMessage buildStandardShareMessage() {
    	UserBadge.BadgeInfo badgeInfo = getBadgeInfo();
        if(badgeInfo == null){
        	return null;
        }
        return buildDefaultShareMessage(badgeInfo.title);
    }

    @Override
    protected ShareMessage buildTwitterShareMessage() {
    	UserBadge.BadgeInfo badgeInfo = getBadgeInfo();
        if(badgeInfo == null){
        	return null;
        }
        return buildDefaultShareMessage(badgeInfo.title);
    }
    
    public boolean isActive(){
    	return (this.userBadge != null);
    }
}
