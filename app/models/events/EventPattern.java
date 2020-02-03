package models.events;

import models.ModelBase;
import models.events.EntityEvent.EntityEventType;

public class EventPattern {
	
	private final EntityEventType type;
	private final Class<? extends ModelBase> clazz;
	private final String propertyName;
	
	private EventPattern(EntityEventType type,
			Class<? extends ModelBase> clazz,
			String propertyName){
		this.type = type;
		this.clazz = clazz;
		this.propertyName = propertyName;
	}
	
	public static SignatureBuilder build(Class<? extends ModelBase> clazz){
		return new SignatureBuilder(clazz);
	}
	
	static Builder build(EventSinkAttributes owner, Class<? extends ModelBase> clazz){
		return new Builder(owner, clazz);
	}
			
	public static class Builder{
		private final EventSinkAttributes owner;
		private Class<? extends ModelBase> clazz;
		private Builder(EventSinkAttributes owner, Class<? extends ModelBase> clazz){
			this.owner = owner;
			this.clazz = clazz;
		}
		
		public CanAddFilter created(){
			return finish(new EventPattern(EntityEventType.CREATED, clazz, null));
		}
		
		public CanAddFilter deleted(){
			return finish(new EventPattern(EntityEventType.DELETED, clazz, null));
		}
		
		public CanAddFilter updated(String propertyName){
			return finish(new EventPattern(EntityEventType.UPDATED, clazz, propertyName));
			
		}
		
		private CanAddFilter finish(final EventPattern pattern){
			
			owner.addEventPattern(pattern);
			
			return new CanAddFilter(){
				@Override
				public void addFilter(EventFilter filter) {
					owner.addEventFilter(pattern, filter);
				}
			};
		}
	}
	
	public static class SignatureBuilder{
		private Class<? extends ModelBase> clazz;
		private SignatureBuilder(Class<? extends ModelBase> clazz){
			this.clazz = clazz;
		}
		
		public EventPattern created(){
			return new EventPattern(EntityEventType.CREATED, clazz, null);
		}
		
		public EventPattern deleted(){
			return new EventPattern(EntityEventType.DELETED, clazz, null);
		}
		
		public EventPattern updated(String propertyName){
			return new EventPattern(EntityEventType.UPDATED, clazz, propertyName);
			
		}
	}

	public EntityEventType getType() {
		return type;
	}

	public Class<? extends ModelBase> getClazz() {
		return clazz;
	}

	public String getPropertyName() {
		return propertyName;
	}
	
	public String toString(){
		StringBuilder result = new StringBuilder(type.name()).append(":").append(clazz.getName());
		if(propertyName != null){
			result.append(":").append(propertyName);
		}
		return result.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((clazz == null) ? 0 : clazz.hashCode());
		result = prime * result
				+ ((propertyName == null) ? 0 : propertyName.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EventPattern other = (EventPattern) obj;
		if (clazz == null) {
			if (other.clazz != null)
				return false;
		} else if (!clazz.equals(other.clazz))
			return false;
		if (propertyName == null) {
			if (other.propertyName != null)
				return false;
		} else if (!propertyName.equals(other.propertyName))
			return false;
		if (type != other.type)
			return false;
		return true;
	}
}
