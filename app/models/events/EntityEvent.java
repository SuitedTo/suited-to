package models.events;

import models.ModelBase;

public abstract class EntityEvent {

	public enum EntityEventType {CREATED,UPDATED,DELETED};
	protected final ModelBase src;
	
	public EntityEvent(ModelBase src){
		this.src = src;
	}

	public ModelBase getSource() {
		return src;
	}
	
	public abstract EntityEventType getType();
	
	public abstract EventPattern getPattern();
}
