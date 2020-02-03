package data.binding.types;

import models.filter.EntityAttributeFilter;
import play.data.binding.TypeBinder;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses the given filter string to create a list of filters.
 * 
 * Examples of filter string format:
 * 
 * includeByCreator:me
 * excludeByDifficulty:EASY
 * includeByDifficulty:MEDIUM|HARD
 * 
 * @author joel
 *
 */
public class FilterBinder  implements TypeBinder<String>{

	@Override
	public EntityAttributeFilter bind(String name, Annotation[] annotations, String value,
			Class actualClass, Type genericType) throws Exception {
		
		Pattern p = Pattern.compile("(include|exclude)(\\w+)(:)([\\s\\.\\|\\$\\w]+)");
		Matcher matcher = p.matcher(value);
		if(matcher.matches()){
			boolean include = matcher.group(1).equals("include");
			String filterClassName = actualClass.getName();
			filterClassName = filterClassName.substring(0,filterClassName.lastIndexOf('.'))+ "." + matcher.group(2);
			EntityAttributeFilter filter = (EntityAttributeFilter)Class.forName(filterClassName).newInstance();
			if(include){
				String[] includes = matcher.group(4).split("\\|");
				for(String s : includes){
					filter.include(s);
				}
			}else{
				String[] excludes = matcher.group(4).split("\\|");
				for(String s : excludes){
					filter.exclude(s);
				}
			}
			return filter;
			
		} else {
			String filterClassName = actualClass.getName();
			filterClassName = filterClassName.substring(0,filterClassName.lastIndexOf('.'))+ "." + value;
			
			//try block is really just for readability/debugging
			try{
				EntityAttributeFilter filter = (EntityAttributeFilter)Class.forName(filterClassName).newInstance();
				return filter;
			}catch(Exception e){
				return null;
			}
		}
	}

}
