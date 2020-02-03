package data.binding.types;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import enums.PhoneType;

import models.embeddable.PhoneNumber;

import play.data.binding.Binder;
import play.data.binding.TypeBinder;

/**
 * Binder for PhoneNumber
 * 
 * @author joel
 *
 */
public class PhoneNumberBinder implements TypeBinder<PhoneNumber>{

	@Override
	public PhoneNumber bind(String name, Annotation[] annotations, String value,
			Class actualClass, Type genericType) throws Exception {
		
		if (value == null){
			return null;
		}
		
		PhoneType type = null;
		String phoneVal = null;
		
		Pattern p = Pattern.compile("(\\()(\\w*)(\\))([\\d\\s-.]*)");
		Matcher matcher = p.matcher(value);
		if(matcher.matches()){
			try{
				type = PhoneType.valueOf(matcher.group(2));
			}catch(Exception e){
				//no matching enum -> leave type as null
			}
			phoneVal = matcher.group(4);
		}
		
		
		return new PhoneNumber(type, phoneVal);
	}

}
