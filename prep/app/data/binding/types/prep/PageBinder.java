package data.binding.types.prep;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Hashtable;
import java.util.Map;

import models.prep.ewrap.Page;
import play.data.binding.TypeBinder;
import play.libs.F.Option;

public class PageBinder implements TypeBinder<String>{

	@Override
	public Option<Page> bind(String name, Annotation[] annotations, String value,
			Class actualClass, Type genericType) throws Exception {
		
		String key = "page";
		Map<String, String[]> data = new Hashtable<String, String[]>();
		data.put(key, new String[]{value});
		return new Page().bind("page", data);
	}
}