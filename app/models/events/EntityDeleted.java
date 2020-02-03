package models.events;

import models.ModelBase;
import models.events.EntityEvent.EntityEventType;

public final class EntityDeleted extends EntityEvent {

	public EntityDeleted(ModelBase src) {
		super(src);
	}
	
	public String toString(){
		return "EntityDeleted[" + src.getClass().getSimpleName() + "[" + src.id + "]]";
	}

	@Override
	public EntityEventType getType() {
		return EntityEventType.DELETED;
	}

	@Override
	public EventPattern getPattern() {
		return EventPattern.build(src.getClass()).deleted();
	}

}
