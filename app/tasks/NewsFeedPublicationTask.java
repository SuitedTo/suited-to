package tasks;

import enums.EventType;
import models.Event;
import models.Story;
import newsfeed.publicationstrategy.PublicationStrategy;
import play.Logger;
import scheduler.Task;
import scheduler.TaskExecutionContext;
import utils.TimingUtil;

/**
 * Responsible for taking new Events and creating the appropriate Story instances based on those Events
 */
public class NewsFeedPublicationTask extends Task {

    public NewsFeedPublicationTask(TaskExecutionContext context) {
        super(context);
    }

    @Override
    public void doTask() {
    	super.doTask();

    	//get the event ID from the args
    	Long eventId = (Long) getArg("eventId");
    	Event event = TimingUtil.<Event>wait(Event.class, eventId);
    	if(event != null){

    		/*find out if stories have already been published for this event.
        	todo: future implementations may want to selectively check for stories by user or groups of users so that
        	stories may be published at different times for the same events.*/
    		boolean storiesHaveBeenPublished = Story.count("byEvent", event) > 0;
    		if(storiesHaveBeenPublished){
    			return;
    		}
    		//if no stories have been published go ahead and publish the appropriate stories now
    		final EventType eventType = event.eventType;
    		if(eventType != null) {
    			final PublicationStrategy publicationStrategy = eventType.getPublicationStrategy();
    			logStoryPublication(eventId, publicationStrategy);
    			publicationStrategy.publishStories(event);
    		}
    	} else {
    		Logger.error("Unable to publish story based on missing event: %s", getArg("eventId"));
    	}
    }

    private static final void logStoryPublication(Long eventId, PublicationStrategy publicationStrategy){
        StringBuilder builder = new StringBuilder();
        builder.append(Thread.currentThread().getName());
        builder.append(" Publishing Stories for Event ");
        builder.append(eventId);
        builder.append(" using publication strategy " );
        builder.append(publicationStrategy.getClass().getName());
        Logger.info(builder.toString());
    }


}
