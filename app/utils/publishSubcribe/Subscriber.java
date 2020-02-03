package utils.publishSubcribe;

public interface Subscriber<T> {

	public void handlePublication(T publication);
}
