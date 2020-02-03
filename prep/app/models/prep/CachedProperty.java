package models.prep;

import exceptions.prep.CacheMiss;

public interface CachedProperty<T> {

	public T getValue() throws CacheMiss;
	public void setValue(T value);
	public void clear();
}
