package models.events;

import java.util.List;
import play.utils.FastRuntimeException;
import utils.publishSubcribe.PublicationSupport;


public abstract class ModelDrivenEventChannel<T> implements EntityEventSink,
	EntityEventTransformer<T>, EventChannel<T> {
	
	protected EventSinkAttributes attr;
	
	public ModelDrivenEventChannel(){
		attr = buildAttributes();
		if(attr == null){
			throw new FastRuntimeException("Event sink lacks attributes :" + getClass().getName());
		}
	}
	
	public final EventSinkAttributes getAttributes(){
		return attr;
	}
	
	protected abstract EventSinkAttributes buildAttributes();
	
	public void processEvent(EntityEvent event){
		List<T> outbound = transformEvent(event);
		for(T out : outbound){
			distributeEvent(out);
		}
	}

	public void distributeEvent(T event){
		PublicationSupport.publish(getClass(), event);
	}
}
