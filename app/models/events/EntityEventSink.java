package models.events;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import play.Logger;
import play.utils.FastRuntimeException;



/**
 * An entity event sink receives entity events. An entity event represents
 * a change in the model. To receive entity events just implement this interface.
 * 
 * @author joel
 *
 */
public interface EntityEventSink {

	public EventSinkAttributes getAttributes();
	
	/**
	 * Override this method to handle entity events.
	 * 
	 * Note that when this method is called there will be no active session so you will
	 * likely need to start up a job/task. The event source entity will be detached and represents
	 * a snapshot of the object at the moment that the change occurred - the event source entity
	 * does not necessarily reflect exactly what's in the database, the state of the database
	 * could have changed since this event was generated.
	 * 
	 * @param event
	 */
	public void processEvent(EntityEvent event);
	
	
}
