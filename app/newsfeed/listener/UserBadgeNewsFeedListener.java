package newsfeed.listener;

import models.Event;
import models.UserBadge;

import java.beans.PropertyChangeEvent;

public class UserBadgeNewsFeedListener extends NewsFeedEventListener {
    private static final String MULTIPLIER_FIELD = "multiplier";

    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        Object source = propertyChangeEvent.getSource();
        if(!(source instanceof UserBadge)){
            throw new IllegalStateException(this.getClass().getName() + " registered with invalid Type.  " +
                    "Expected " + UserBadge.class.getName() + " found " + source.getClass().getName());
        }
        UserBadge userBadge = (UserBadge) source;

        /*There is corresponding logic in UserBadge.save() to handle creation of events for newly created UserBadges
        * need this check here to guard against creating duplicate events when a UserBadge is constructed and then has
        * its multiplier modified prior to saving.*/
        if(!userBadge.hasBeenSaved()){
            return;
        }

        final String propertyName = propertyChangeEvent.getPropertyName();
        if(propertyName.equals(MULTIPLIER_FIELD)){
            Integer newValue = (Integer) propertyChangeEvent.getNewValue();
            Integer oldValue = (Integer) propertyChangeEvent.getOldValue();
            if((oldValue == null && newValue != null) || (oldValue != null && newValue != null && oldValue < newValue)){
                Event event = userBadge.buildDefaultEvent();
                registerEvent(userBadge, event, propertyChangeEvent);
            }

        }
    }
}
