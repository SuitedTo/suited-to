package data.binding.types.prep;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Hashtable;
import java.util.Map;

import models.prep.ewrap.Sort;
import play.data.binding.TypeBinder;
import play.libs.F.Option;

public class SortBinder implements TypeBinder<String>{

	@Override
	public Option<Sort> bind(String name, Annotation[] annotations, String value,
			Class actualClass, Type genericType) throws Exception {
		
		String key = "sort";
		Map<String, String[]> data = new Hashtable<String, String[]>();
		data.put(key, new String[]{value});
		return new Sort().bind("sort", data);
	}

}
