package newsfeed.listener;

import enums.EventType;
import models.Category;
import models.Event;
import models.User;
import newsfeed.metadata.ReviewerEventMetadata;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

public class NewReviewerNewsFeedListener extends NewsFeedEventListener {

    private static final String SUPER_REVIEWER_PROPERTY = "superReviewer";
    private static final String REVIEW_CATEGORIES_PROPERTY = "reviewCategories";

    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        Object source = propertyChangeEvent.getSource();
        if(!(source instanceof User)){
            throw new IllegalStateException(this.getClass().getName() + " registered with invalid Type.  " +
                    "Expected " + User.class.getName() + " found " + source.getClass().getName());
        }
        User user = (User) source;

        final String propertyName = propertyChangeEvent.getPropertyName();
        if (propertyName.equals(SUPER_REVIEWER_PROPERTY) || propertyName.equals(REVIEW_CATEGORIES_PROPERTY)) {

            Object newValue = propertyChangeEvent.getNewValue();
            Object oldValue = propertyChangeEvent.getOldValue();

            //create the common shell of the event here, it may not end up getting actually registered depending on
            //the state of the old and new values
            Event event = new Event();
            event.eventType = EventType.REVIEWER;
            event.relatedUser = user;

            if (propertyName.equals(SUPER_REVIEWER_PROPERTY)) {
                if (!oldValue.equals(newValue) && Boolean.TRUE.equals(newValue)) {
                    ReviewerEventMetadata metadata = new ReviewerEventMetadata();
                    metadata.setSuperReviewer(true);
                    event.serializeAndSetEventMetadata(metadata);
                    registerEvent(user, event, propertyChangeEvent);
                }
            } else { //we know its "reviewCategories"
                List<Category> oldCategories = (List<Category>) oldValue;
                List<Category> newCategories = (List<Category>) newValue;
                List<Category> newlyAddedReviewCategories = new ArrayList<Category>();
                List<Long> newReviewCategoryIds = new ArrayList<Long>();
                List<String> newReviewCategoryNames = new ArrayList<String>();

                ReviewerEventMetadata metadata = new ReviewerEventMetadata();

                //the user didn't used to have reviewCategories but now he does
                if ((oldCategories == null || oldCategories.isEmpty()) && newCategories != null && !newCategories.isEmpty()) {
                    newlyAddedReviewCategories = newCategories;

                //they used to have some reviewCategories and they still do, so check to see if they changed
                } else if (oldCategories != null && !oldCategories.isEmpty() && newCategories != null && !newCategories.isEmpty()) {
                    for (Category category : newCategories) {
                        if(!oldCategories.contains(category)){
                            newlyAddedReviewCategories.add(category);
                        }
                    }
                }

                if (!newlyAddedReviewCategories.isEmpty()) {
                    for (Category newCategory : newlyAddedReviewCategories) {
                        newReviewCategoryIds.add(newCategory.id);
                        newReviewCategoryNames.add(newCategory.name);

                    }
                    metadata.setCategoryIds(newReviewCategoryIds);
                    metadata.setCategoryNames(newReviewCategoryNames);
                    event.serializeAndSetEventMetadata(metadata);
                    registerEvent(user, event, propertyChangeEvent);
                }
            }

        }
    }
}
