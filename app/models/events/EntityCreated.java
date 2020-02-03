package models.events;

import models.ModelBase;

public final class EntityCreated extends EntityEvent {

	public EntityCreated(ModelBase src) {
		super(src);
	}
	
	public String toString(){
		return "EntityCreated[" + src.getClass().getSimpleName() + "[" + src.id + "]]";
	}

	@Override
	public EntityEventType getType() {
		return EntityEventType.CREATED;
	}

	@Override
	public EventPattern getPattern() {
		return EventPattern.build(src.getClass()).created();
	}

}
