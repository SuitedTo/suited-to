package models.events;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import models.ModelBase;


public class EventSinkAttributes {

	private final Collection<EventPattern> eventPatterns = new ArrayList<EventPattern>();
	private final Map<EventPattern, EventFilter> eventFilters = new Hashtable<EventPattern,EventFilter>();
	
	
	Collection<EventPattern> getEventPatterns() {
		return eventPatterns;
	}
	
	Collection<EventFilter> getEventFilters() {
		return eventFilters.values();
	}
	
	EventFilter getEventFilter(EventPattern pattern) {
		return eventFilters.get(pattern);
	}

	EventSinkAttributes addEventPattern(EventPattern pattern) {
		this.eventPatterns.add(pattern);
		return this;
	}
	
	public EventPattern.Builder addEventPattern(Class<? extends ModelBase> clazz) {
		return EventPattern.build(this, clazz);
	}
	
	EventSinkAttributes addEventFilter(EventPattern pattern, EventFilter filter) {
		this.eventFilters.put(pattern, filter);
		return this;
	}
	
}
