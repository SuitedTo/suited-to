package models.events.channels.newsfeed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Event;
import models.Question;
import models.UserMetrics;
import models.events.EntityEvent;
import models.events.EntityUpdated;
import models.events.EventFilter;
import models.events.EventSinkAttributes;
import models.events.ModelDrivenEventChannel;
import models.events.subscribers.newsfeed.UserEventManager;
import utils.publishSubcribe.Subscribers;

import com.google.gson.Gson;

import enums.EventType;

@Subscribers(UserEventManager.class)
public class UserEventChannel extends ModelDrivenEventChannel<Event>{

	@Override
	public List<Event> transformEvent(EntityEvent event) {
		List<Event> result = new ArrayList<Event>();
		
		Map<String, Object> metadata = new HashMap<String, Object>();
		Event e = new Event();
		EntityUpdated uEvent = (EntityUpdated)event;
		if(event.getSource() instanceof UserMetrics){
			e.relatedUser = ((UserMetrics)event.getSource()).user;
			e.eventType = EventType.QUESTIONS_SUBMITTED;
			metadata.put("questionsSubmitted", uEvent.getNewValue());
		} else if(event.getSource() instanceof Question){
			e.relatedUser = ((Question)event.getSource()).user;
			e.eventType = EventType.QUESTION_RATING;
			metadata.put("questionRating", uEvent.getNewValue());
		}
		e.metadata = new Gson().toJson(metadata);;
		result.add(e);
		return result;
	}

	@Override
	protected EventSinkAttributes buildAttributes() {
		EventSinkAttributes attr = new EventSinkAttributes();
		attr.addEventPattern(Question.class).updated("standardScore")
			.addFilter(new EventFilter(){
				@Override
				public boolean willAccept(EntityEvent event) {
					int newVal = (Integer)((EntityUpdated)event).getNewValue();
					return  (newVal > 0) && (newVal % 50 == 0);
				}
		});
		attr.addEventPattern(UserMetrics.class).updated("numberOfSubmittedQuestions")
			.addFilter(new EventFilter(){
				@Override
				public boolean willAccept(EntityEvent event) {
					return (Integer)((EntityUpdated)event).getNewValue() == 5;
				}
		});
		return attr;
	}

}
