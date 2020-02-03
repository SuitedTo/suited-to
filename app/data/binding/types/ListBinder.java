package data.binding.types;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import play.data.binding.As;
import play.data.binding.TypeBinder;
import play.data.validation.Required;

/**
 * Play's default collection binding mechanism will return a list with one empty string if it
 * gets an empty string. This is really annoying because Required checks won't catch
 * an empty string. This binder will throw an exception if it gets an empty string or
 * if any element in the list is empty (eg: "a,b,,d") so you get a null in these cases
 * and the required check fails for you.
 * 
 * 
 * @author joel
 *
 */
public class ListBinder implements TypeBinder<String>{

	@Override
	public String bind(String name, Annotation[] annotations, String value,
			Class actualClass, Type genericType) throws Exception {
		
		if (value == null){
			return null;
		}
		
		if(value.length() == 0){
			throw new Exception();
		}
		
		return value;
	}

}
