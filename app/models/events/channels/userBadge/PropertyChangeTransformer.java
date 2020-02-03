package models.events.channels.userBadge;

import java.util.List;


interface PropertyChangeTransformer {
	
	static class PropertyChange{
		private String name;
		private Object previousValue;
		private Object value;
		public PropertyChange(String name, Object previousValue, Object value) {
			super();
			this.name = name;
			this.previousValue = previousValue;
			this.value = value;
		}
		public final String getName() {
			return name;
		}
		public final void setName(String name) {
			this.name = name;
		}
		public final Object getPreviousValue() {
			return previousValue;
		}
		public final void setPreviousValue(Object previousValue) {
			this.previousValue = previousValue;
		}
		public final Object getValue() {
			return value;
		}
		public final void setValue(Object value) {
			this.value = value;
		}
	}

	/**
	 * @param property
	 * @return List of properties for processing or null to skip the given property.
	 */
	public List<PropertyChange> transform(PropertyChange property);
}
