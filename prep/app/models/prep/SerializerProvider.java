package models.prep;

import com.google.gson.JsonSerializer;

public interface SerializerProvider<T> {

	public JsonSerializer<T> serializer();
}
