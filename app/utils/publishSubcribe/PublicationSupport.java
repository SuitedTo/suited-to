package utils.publishSubcribe;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import jobs.RobustJob;

public final class PublicationSupport {
	private final static Map<Class,List<Subscriber>> subscriberMap =
			new Hashtable<Class,List<Subscriber>>();
	
	public static void publish(Object publication){
		throw new UnsupportedOperationException(
				"This method may only be invoked from a class that has it's own list of @Subscribers"
				);
	}
	
	public static void publish(Class publisher, final Object publication){
		List<Subscriber> subscribers = subscriberMap.get(publisher);
		if(subscribers != null){
			for(final Subscriber subscriber : subscribers){
				subscriber.handlePublication(publication);
			}
		}
	}
	
	public static void addSubscriber(Class publisher, Subscriber subscriber){
		List<Subscriber> subscribers = subscriberMap.get(publisher);
		if(subscribers == null){
			subscribers = new ArrayList<Subscriber>();
			subscriberMap.put(publisher, subscribers);
		}
		subscribers.add(subscriber);
	}
}
