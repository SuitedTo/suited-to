package db;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import models.ModelBase;
import models.events.EntityCreated;
import models.events.EntityDeleted;
import models.events.EntityEvent;
import models.events.EntityUpdated;
import models.events.EventDistributionCenter;
import models.events.EventPattern;
import play.PlayPlugin;

public final class EventCollector extends PlayPlugin{
	
	private boolean ready = false;

	private static ThreadLocal<List<EntityEvent>> eventList = new ThreadLocal<List<EntityEvent>>();

	public void afterApplicationStart(){
		ready = true;
	}

	@Override
	public void onEvent(String message, Object context) {
		
		if(!ready){
			return;
		}

		if (eventList.get() == null){
			eventList.set(Collections.synchronizedList(new ArrayList<EntityEvent>()));
		}
		
		if((context instanceof PropertyChangeEvent) && !(((PropertyChangeEvent)context).getSource() instanceof ModelBase)){
			//Not propagating events for prep at this time
			return;
		}
		
		if(!(context instanceof PropertyChangeEvent)  && !(context instanceof ModelBase)){
			//Not propagating events for prep at this time
			return;
		}

		if(message.equals("JPASupport.objectPersisted")){
			EntityEvent eEvent = new EntityCreated((ModelBase) context);
			eventList.get().add(eEvent);

		} else if(message.equals("JPASupport.objectDeleted")){
			EntityEvent eEvent = new EntityDeleted((ModelBase) context);
			eventList.get().add(eEvent);

		} else if(message.equals("ModelBase.objectUpdated")){
			PropertyChangeEvent event = (PropertyChangeEvent)context;

			EntityUpdated eEvent = new EntityUpdated(
					(ModelBase)event.getSource(),
					event.getPropertyName(),
					event.getOldValue(),
					event.getNewValue());
			
			eventList.get().add(eEvent);
		}
	}

	static void afterTransactionCompletion(boolean committed){
		if(committed){
			List<EntityEvent> events = eventList.get();
			if(events != null){
				EventDistributionCenter.distributeEvents(events);
				eventList.remove();
			}
		}
	}

}
