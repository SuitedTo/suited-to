package data.binding.types.prep;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import com.google.gson.Gson;


import play.data.binding.TypeBinder;
import play.utils.Java;

public class JsonBinder implements TypeBinder<String>{

	@Override
	public Object bind(String name, Annotation[] annotations, String value,
			Class actualClass, Type genericType) throws Exception {
		try{
			return Java.invokeStatic(actualClass, "fromJson", value);
		}catch(Exception e){
			
		}
        return new Gson().fromJson(value, actualClass);
	}
}
