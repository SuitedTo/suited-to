package newsfeed.listener;

import enums.EventType;
import models.Category;
import models.Event;
import models.User;
import newsfeed.metadata.ProInterviewerEventMetadata;
import newsfeed.metadata.ReviewerEventMetadata;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

public class ProInterviewerNewsFeedListener extends NewsFeedEventListener {

    private static final String PRO_INTERVIEWER_CATEGORIES_PROPERTY = "proInterviewerCategories";

    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        Object source = propertyChangeEvent.getSource();
        if(!(source instanceof User)){
            throw new IllegalStateException(this.getClass().getName() + " registered with invalid Type.  " +
                    "Expected " + User.class.getName() + " found " + source.getClass().getName());
        }
        User user = (User) source;

        final String propertyName = propertyChangeEvent.getPropertyName();
        if (propertyName.equals(PRO_INTERVIEWER_CATEGORIES_PROPERTY)) {

            Object newValue = propertyChangeEvent.getNewValue();
            Object oldValue = propertyChangeEvent.getOldValue();

            //create the common shell of the event here, it may not end up getting actually registered depending on
            //the state of the old and new values
            Event event = new Event();
            event.eventType = EventType.PRO_INTERVIEWER;
            event.relatedUser = user;

            if (propertyName.equals(PRO_INTERVIEWER_CATEGORIES_PROPERTY)) { //we know its "reviewCategories"
                List<Category> oldCategories = (List<Category>) oldValue;
                List<Category> newCategories = (List<Category>) newValue;
                List<Category> newlyAddedCategories = new ArrayList<Category>();
                List<Long> newCategoryIds = new ArrayList<Long>();
                List<String> newCategoryNames = new ArrayList<String>();

                ProInterviewerEventMetadata metadata = new ProInterviewerEventMetadata();

                //the user didn't used to have pro interviewer categories but now he does
                if ((oldCategories == null || oldCategories.isEmpty()) && newCategories != null && !newCategories.isEmpty()) {
                    newlyAddedCategories = newCategories;

                //they used to have some pro interviewer categories and they still do, so check to see if they changed
                } else if (oldCategories != null && !oldCategories.isEmpty() && newCategories != null && !newCategories.isEmpty()) {
                    for (Category category : newCategories) {
                        if(!oldCategories.contains(category)){
                            newlyAddedCategories.add(category);
                        }
                    }
                }

                if (!newlyAddedCategories.isEmpty()) {
                    for (Category newCategory : newlyAddedCategories) {
                        newCategoryIds.add(newCategory.id);
                        newCategoryNames.add(newCategory.name);

                    }
                    metadata.setCategoryIds(newCategoryIds);
                    metadata.setCategoryNames(newCategoryNames);
                    event.serializeAndSetEventMetadata(metadata);
                    registerEvent(user, event, propertyChangeEvent);
                }
            }

        }
    }
}
