package models.events;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import play.Logger;


public final class EventDistributionCenter {
	
	private static Map<EventPattern,List<EntityEventSink>> patternMap = null;

	private EventDistributionCenter(){}
	
	public static void distributeEvents(final Collection<EntityEvent> events){
		for(EntityEvent event: events){
			distributeEvent(event);
		}
		/* If we blow away the event context in the above loop then
		 * other events that share the same source entity are SOL so
		 * clear the context after all events are distributed. Still
		 * need to put some more thought into the whole event context
		 * thing but it's ok for now.
		 */
		for(EntityEvent event: events){
			event.getSource().eventContext.clear();
		}
	}
	
	private static void distributeEvent(EntityEvent event){
		if(patternMap == null){
			buildPatternMap();
		}
		List<EntityEventSink> sinks = patternMap.get(event.getPattern());
		if(sinks != null){
			for(EntityEventSink sink : sinks){
				EventFilter filter = sink.getAttributes().getEventFilter(event.getPattern());
				if((filter == null) || filter.willAccept(event)){
					sink.processEvent(event);
				}
			}
		}
	}
	
	private static void buildPatternMap(){
		patternMap = new Hashtable<EventPattern,List<EntityEventSink>>();
		List<Class> allClasses = play.Play.classloader.getAllClasses();
		for(Class clazz : allClasses){
			if(EntityEventSink.class.isAssignableFrom(clazz) && !Modifier.isAbstract(clazz.getModifiers())){
				try {
					EntityEventSink sink = (EntityEventSink) clazz.newInstance();
					Collection<EventPattern> patterns = sink.getAttributes().getEventPatterns();
					for(EventPattern pattern : patterns){
						List<EntityEventSink> sinks = patternMap.get(pattern);
						if(sinks == null){
							sinks = new ArrayList<EntityEventSink>();
							patternMap.put(pattern, sinks);
						}
						if(!sinks.contains(sink)){
							sinks.add(sink);
						}
					}
				} catch (Exception e) {
					Logger.error("Unable to instantiate event sink %s", clazz.getName());
					e.printStackTrace();
				}
			}
		}

	}
}
