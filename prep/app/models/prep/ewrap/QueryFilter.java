package models.prep.ewrap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import play.libs.F.Option;
import static play.libs.F.Option.None;
import static play.libs.F.Option.Some;

import com.avaje.ebean.Expr;
import com.avaje.ebean.Expression;
import com.avaje.ebean.ExpressionList;

/**
 * A Query filter applies one or more Expressions to an ExpressionList.
 * 
 * All filters must have a no arg constructor.
 * 
 * @author joel
 *
 * @param <T>
 */
public abstract class QueryFilter<T> {
	
	@SuppressWarnings("rawtypes")
	protected Map<String, ParamConverter> converters = new HashMap<String, ParamConverter>();
	
	protected final Expression TRUE = Expr.raw("1=1");
	
	protected final Expression FALSE = Expr.raw("1=0");

	public abstract ExpressionList<T> apply(ExpressionList<T> expressionList,
			QueryBuildContext context,
			Option<Map<String, Object>> params);
	
	@SuppressWarnings("serial")
	public static class ParamConversionException extends RuntimeException{};

	@SuppressWarnings("rawtypes")
	public Option<ParamConverter> getParamConverter(String paramName){
		ParamConverter pc = converters.get(paramName);
		if(pc == null){
			return None();
		}
		return Some(pc);
	}

    public static StringParamConverter asString(){
		return new StringParamConverter();
	}
	
	public static class StringParamConverter implements ParamConverter<String>{
		public String convert(String value){
			return value;
		}
	}
	
	public static class IntegerParamConverter implements ParamConverter<Integer>{
		public Integer convert(String value){
			try{
				return Integer.parseInt(value);
			} catch (NumberFormatException nfe){
				throw new ParamConversionException();
			}
		}
	}
	
	public static class LongParamConverter implements ParamConverter<Long>{
		public Long convert(String value){
			try{
				return Long.parseLong(value);
			} catch (NumberFormatException nfe){
				throw new ParamConversionException();
			}
		}
	}

    public static class DoubleParamConverter implements ParamConverter<Double>{
        public Double convert(String value){
            try{
                return Double.valueOf(value);
            } catch (NumberFormatException nfe){
                throw new ParamConversionException();
            }
        }
    }
	
	public static class LongListParamConverter implements ParamConverter<List<Long>>{
		public List<Long> convert(String value){
			try{
				List<Long> result = new ArrayList<Long>();
				String[] values = value.split(",");
				for(String s : values){
					result.add(Long.parseLong(s));
				}
				return result;
			} catch (NumberFormatException nfe){
				throw new ParamConversionException();
			}
		}
	}
	
	public static class IntegerListParamConverter implements ParamConverter<List<Integer>>{
		public List<Integer> convert(String value){
			try{
				List<Integer> result = new ArrayList<Integer>();
				String[] values = value.split(",");
				for(String s : values){
					result.add(Integer.parseInt(s));
				}
				return result;
			} catch (NumberFormatException nfe){
				throw new ParamConversionException();
			}
		}
	}

	public static class StringListParamConverter implements ParamConverter<List<String>>{
		public List<String> convert(String value){
			List<String> result = new ArrayList<String>();
			String[] values = value.split(",");
			for(String s : values){
				result.add(s);
			}
			return result;
		}
	}
	
	public static class BooleanParamConverter implements ParamConverter<Boolean>{
		public Boolean convert(String value){
			try{
				return Boolean.parseBoolean(value);
			} catch (Exception e){
				throw new ParamConversionException();
			}
		}
	}

}
