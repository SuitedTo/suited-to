package data.binding.types.prep;

import static play.libs.F.Option.None;
import static play.libs.F.Option.Some;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import play.data.binding.TypeBinder;
import play.libs.F.Option;

public class PathBinder implements TypeBinder<String>{

	@Override
	public Option<String> bind(String name, Annotation[] annotations, String value,
			Class actualClass, Type genericType) throws Exception {
		
		if(value != null){
			return Some(value);
		}
		return None();
	}
}