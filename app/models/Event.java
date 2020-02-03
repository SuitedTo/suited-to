package models;

import com.google.gson.Gson;
import enums.EventType;
import newsfeed.metadata.EventMetadata;
import play.Play;
import scheduler.TaskArgs;
import scheduler.TaskScheduler;
import tasks.NewsFeedPublicationTask;

import javax.persistence.*;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * The result of an Action or a set of Actions
 */
@Entity
public class Event extends ModelBase {
    /**
     * The type of the event. Typically should not be null but we may want to create events with no event type at some
     * point so you must handle scenarios where the type is null.
     */
    @Enumerated(EnumType.STRING)
    public EventType eventType;

    /**
     * The user that this Event is related to.  For example if the Event is describing a User receiving a new Badge
     * then the relatedUser would be the user that is receiving the badge.  Not all events will have a relatedUser
     */
    @ManyToOne
    public User relatedUser;

    /**
     * Extra information about the event will vary based on eventType.  Serialized as a JSON String
     */
    @Column(columnDefinition = "LONGTEXT")
    public String metadata;

    /**
     * If the creation of this Event was triggered by a propertyChangeEvent, this field can temporarily store the
     * triggering propertyChangeEvent so that we can verify that the entity's value for the given property hasn't changed
     * since the Event was created.  This allows us to queue up Events in the Cache immediately upon properties changing
     * but not save the Events until actually persisting the changes to the entity.
     */
    @Transient
    public PropertyChangeEvent propertyChangeEvent;


    /***********************************************************************************
     * Relationships to Child Entities                                                 *
     ***********************************************************************************/

    /**
     * Stories for this Event. Need this field so that deletes are completely cascaded but we don't want to access this
     * property outside of the class itself.
     */
    @OneToMany(mappedBy = "event", cascade = CascadeType.REMOVE)
    private List<Story> stories = new ArrayList<Story>();

    /**
     * After saving a new event schedule a Task to Publish any new Stories related to the Event
     */
    @PostPersist
    public void scheduleStoryPublicationTask(){
        TaskArgs args = new TaskArgs();
        args.add("eventId", this.id);
        final boolean testMode = Play.runingInTestMode();
        if(!testMode) {
            TaskScheduler.schedule(NewsFeedPublicationTask.class, args, CronTrigger.getASAPTrigger());
        }
    }

    /**
     * Stores an EventMetadata instance as JSON data
     * @param metadata EventMetadata instance to serialize and store
     */
    public void serializeAndSetEventMetadata(EventMetadata metadata) {
        this.metadata = new Gson().toJson(metadata);
    }

    /**
     * Determines the appropriate name to show to the given user for the relatedUser of this event. If the user in
     * context is from the same company as the user related to this event then the related user's actual name will be shown
     * otherwise the displayname will be used.
     * @return String
     */
    public String getRelatedUserNameForDisplay(User userInContext){
        if(userInContext != null && userInContext.company != null && userInContext.company.equals(relatedUser.company)){
            return relatedUser.fullName;
        } else {
            return relatedUser.displayName;
        }
    }

}
