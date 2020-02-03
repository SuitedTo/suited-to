package data.binding.types;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import models.query.QueryBase;

import play.data.binding.TypeBinder;

/**
 * Binder for query objects.
 * 
 * @author joel
 *
 */
public class QueryBinder  implements TypeBinder<String>{

	@Override
	public QueryBase bind(String name, Annotation[] annotations, String value,
			Class actualClass, Type genericType) throws Exception {

		String queryClassName = actualClass.getName();
		queryClassName = queryClassName.substring(0,queryClassName.lastIndexOf('.'))+ "." + value;

		try{
			QueryBase query = (QueryBase)Class.forName(queryClassName).newInstance();
			return query;
		}catch(Exception e){
			return null;
		}

	}

}
