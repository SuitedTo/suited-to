package models.events;

public interface EventChannel<T> {

	public void distributeEvent(T event);
}
