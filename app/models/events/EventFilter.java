package models.events;

public interface EventFilter<T extends EntityEvent> {

	public boolean willAccept(T event);
}
