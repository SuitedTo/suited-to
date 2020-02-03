package models.events;

import java.util.List;

public interface EntityEventTransformer<T> {
	public List<T> transformEvent(EntityEvent event);
}
