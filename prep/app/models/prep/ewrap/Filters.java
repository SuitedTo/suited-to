package models.prep.ewrap;

import static play.libs.F.Option.None;
import static play.libs.F.Option.Some;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.avaje.ebean.ExpressionList;


import play.Logger;
import play.libs.F.Option;
import play.libs.F.Tuple;

/**
 * Filters need to be a fully qualified class name but "models.queries.filters." will be
 * prefixed for you.
 * Separate filters with '|'.
 * Filters can have parameters of the format <paramName>:<paramValue>
 * Separate filter parameters with two colons.
 *  
 * 
 * filters=user.Accessible|common.IncludeById(id:1,2,3)
 * 
 * @author joel
 *
 */
public class Filters {
	
	private static final String filtersPackage = "models.prep.queries.filters.";
	
	@SuppressWarnings("rawtypes")
	private List<Tuple<QueryFilter, Map<String, Object>>> filterList =
		new ArrayList<Tuple<QueryFilter, Map<String, Object>>>();

    public Option<Filters> bind(String key, Map<String, String[]> data) {
        if ("filters".equalsIgnoreCase(key)) {
            try {
            	filterList = parse(data.get(key)[0]);
                return Some(this);
            } catch (Exception e) {
                return None();
            }
        } else {
            return None();
        }
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static List<Tuple<QueryFilter, Map<String, Object>>> parse(String data){
    	List<Tuple<QueryFilter, Map<String, Object>>> result = new ArrayList<Tuple<QueryFilter, Map<String, Object>>>();
    	String[] filtersStr = data.split("\\|");
    	outer:
    		for(String filterStr : filtersStr){
    			int startParams = filterStr.indexOf('(');
    			int endParams = filterStr.lastIndexOf(')');
    			String className = (startParams == -1)?filterStr:filterStr.substring(0,startParams);
    			String params = (startParams == -1)?null:filterStr.substring(startParams + 1,endParams);
    			try {
    				Map<String, Object> paramMap = new Hashtable<String, Object>();
    				Class<QueryFilter> clazz = (Class<QueryFilter>) Class.forName(filtersPackage + className);
    				QueryFilter filter = clazz.newInstance();
    				if(params != null){
    					String[] paramsArray = params.split("::");
    					for(String paramString : paramsArray){
    						String[] paramParts = paramString.split(":");
    						if(paramParts.length != 2){
    							//If the param value is missing then just don't add it
    							continue outer;
    						}
    						String key = paramParts[0];
    						String valueStr = paramParts[1];
    						Object value = valueStr;
    						Option<ParamConverter> maybePc = filter.getParamConverter(key);
    						for(ParamConverter pc : maybePc){
    							value = pc.convert(valueStr);
    						}
    						paramMap.put(key, value);
    					}
    				}
    				result.add(new Tuple<QueryFilter, Map<String, Object>>(filter, paramMap));
    			} catch (ClassNotFoundException e) {
    				Logger.error("Unrecognized filter class " + className);
    				continue;
    			} catch (InstantiationException e) {
    				Logger.error("Unable to instantiate filter " + className + ". A no arg constructor is required.");
    				continue;
    			} catch (Exception e) {
    				e.printStackTrace();
    				continue;
    			}
    		}
    	return result;
    }

    public <T> ExpressionList<T> apply(ExpressionList<T> expressionList, QueryBuildContext context){
    	if(filterList == null){
    		return expressionList;
    	}
    	for(Tuple<QueryFilter, Map<String, Object>> tuple : filterList){
    		tuple._1.apply(expressionList, context, Some(tuple._2));
    	}
    	return expressionList;
    }

    public String unbind(String key) {
    	//TODO
       return "";
    }

    public String javascriptUnbind() {
    	//TODO
        return "function(k,v) {\n" +
            "    return encodeURIComponent(k+'.index')+'='+v.index+'&amp;'+encodeURIComponent(k+'.size')+'='+v.size;\n" +
            "}";
    }
}
