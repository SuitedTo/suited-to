package models.events.subscribers.newsfeed;

import jobs.RobustJob;
import models.Event;
import utils.publishSubcribe.Subscriber;

public class UserEventManager implements Subscriber<Event> {

	@Override
	public void handlePublication(final Event event) {
		new RobustJob(){
			public void tryJob(){
				event.save();
			}
		}.now();
	}
}
