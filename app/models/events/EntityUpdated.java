package models.events;

import models.ModelBase;

public final class EntityUpdated extends EntityEvent {
	private final String propertyName;
	private final Object oldValue;
	private final Object newValue;

	public EntityUpdated(ModelBase src, String propertyName, Object oldValue, Object newValue) {
		super(src);
		this.propertyName = propertyName;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	public String getPropertyName() {
		return propertyName;
	}

	public Object getOldValue() {
		return oldValue;
	}

	public Object getNewValue() {
		return newValue;
	}
	
	public String toString(){
		return "EntityUpdated[" + src.getClass().getSimpleName() + "[" + src.id + "]][" + propertyName + "] " +
				String.valueOf(oldValue) + " -> " + String.valueOf(newValue);
	}

	@Override
	public EntityEventType getType() {
		return EntityEventType.UPDATED;
	}

	@Override
	public EventPattern getPattern() {
		return EventPattern.build(src.getClass()).updated(propertyName);
	}

}
