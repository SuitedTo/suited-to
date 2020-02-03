package models;

import com.google.gson.Gson;
import utils.NewsFeed;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import java.util.Date;

/**
 * An entity-specific representation of an event
 */
@Entity
public class Story extends ModelBase {

    /**
     * If the story applies at the user level this will be non-null and will indicate the user that this story applies
     * to
     */
    @ManyToOne
    public User user;

    /**
     * If the story applies at the company level this will be non-null and will indicate the company that this story
     * applies to
     */
    @ManyToOne
    public Company company;

    /**
     * The event that is represented by this Story.  Stories will typically have associated events, but we'll reserve
     * the right to create stories that are not tied to events which would leave the event field null.
     */
    @ManyToOne
    public Event event;

    /**
     * Indicates that this story applies to all users in the application
     */
    public boolean allUsers;

    /**
     * A measure of how closely the consumer(user, company, or all users if no user or company is defined) of the
     * story is related to the producer of the story.  It should not take any additional information such as the content
     * of the story itself into account.
     */
    public Double affinity;

    /**
     * Could also be defined as the "give a damn factor" This is a measure of how important we think the story will be
     * to the consumer.  This takes into account the event tied to the story and other factors but should not take into
     * account the relationship between the consumer and producer since that is already accounted for in affinity.
     */
    public Double weight;

    /**
     * Number of milliseconds since the epoch for the creation time of this Story, used in the calculation of the time
     * factor in determining the ranking of stories.
     */
    public Long creationTimeInMillis;

    /**
     * Provides metadata if there is no associated event to provide it.  If
     * <code>event != null</code>, then this value is ignored.
     */
    public String metadata;

    /***********************************************************************************
     * Constructors                                                                    *
     ***********************************************************************************/

    public Story() {
        
    }

    public Story(double affinity, double weight, String metadata) {
        this.affinity = affinity;
        this.weight = weight;
        this.metadata = metadata;
        allUsers = true;
    }

    /***********************************************************************************
     * Utility Methods                                                                 *
     ***********************************************************************************/

    /**
     * <p>Returns the metadata that ought be associated with this story--which
     * is the metadata of the event, if the event is not null, and the metadata
     * of this story otherwise.  If the appropriate metadata is null, we'll 
     * return the JSON representation of null.</p>
     * @return 
     */
    public String getLogicalMetadata() {
        String result;
        
        if (event == null) {
            result = metadata;
        }
        else {
            result = event.metadata;
        }
        
        if (result == null) {
            result = new Gson().toJson((Object) null);
        }
        
        return result;
    }
    
    @PrePersist
    private void initializeTimestamps(){
        creationTimeInMillis = new Date().getTime();
    }

    /**
     * Calculates and returns the "score" of a story as of the given time. This can be used to verify that the order that
     * stories are being shown is correct.  When calling this for a batch of stories it's recommended to create a single
     * Date instance to pass to every call within the batch.  It's worth noting that this isn't a perfect match to
     * the value that gets calculated in the database because the dates used in the calculations are different.
     * @param time Date for which to calculate the Story score
     * @return Double, the score of the Story
     */
    public Double getScore(Date time){
        return Math.pow(NewsFeed.timeDegradationFactor, ((time.getTime() - creationTimeInMillis)/NewsFeed.millisecondsPerSecond)) * affinity * weight;
    }
}
