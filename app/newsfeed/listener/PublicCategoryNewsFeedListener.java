package newsfeed.listener;

import enums.EventType;
import models.Category;
import models.Event;
import newsfeed.metadata.PublicCategoryEventMetadata;

import java.beans.PropertyChangeEvent;

public class PublicCategoryNewsFeedListener extends NewsFeedEventListener {
    private static final String STATUS_FIELD = "status";

    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        Object source = propertyChangeEvent.getSource();
        if(!(source instanceof Category)){
            throw new IllegalStateException(this.getClass().getName() + " registered with invalid Type.  " +
                    "Expected " + Category.class.getName() + " found " + source.getClass().getName());
        }
        Category category = (Category) source;

        final String propertyName = propertyChangeEvent.getPropertyName();
        if(propertyName.equals(STATUS_FIELD)){
            Category.CategoryStatus newValue = (Category.CategoryStatus) propertyChangeEvent.getNewValue();
            Category.CategoryStatus oldValue = (Category.CategoryStatus) propertyChangeEvent.getOldValue();

            if(!newValue.equals(oldValue) && Category.CategoryStatus.PUBLIC.equals(newValue)){
                Event event = new Event();
                event.eventType = EventType.NEW_PUBLIC_CATEGORY;
                event.relatedUser = category.creator;
                //build the event metadata
                PublicCategoryEventMetadata metadata = new PublicCategoryEventMetadata();
                metadata.setCategoryId(category.id);
                event.serializeAndSetEventMetadata(metadata);

                registerEvent(category, event, propertyChangeEvent);
            }
        }



    }
}
