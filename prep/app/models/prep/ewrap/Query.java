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
 * Query needs to be a fully qualified class name but "models.queries." will be
 * prefixed for you.
 * Queries can have parameters of the format <paramName>:<paramValue>
 * Separate Query parameters with two colons.
 *  
 * 
 * query=testPercentile.FindPercentile(year:2012::location:NATIONAL::test:SAT_MATH::score:790)
 * 
 * @author joel
 *
 */
public class Query {

	private static final String queryPackage = "models.prep.queries.providers.";

	@SuppressWarnings("rawtypes")
	public QueryProvider queryContainer = null;

	public Option<Query> bind(String key, Map<String, String[]> data) {
		if ("query".equalsIgnoreCase(key)) {
			try {
				queryContainer = parse(data.get(key)[0]);
				if(queryContainer == null){
					return None();
				}
				return Some(this);
			} catch (Exception e) {
				return None();
			}
		} else {
			return None();
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static QueryProvider parse(String data){
		String queryStr = data;
		int startParams = queryStr.indexOf('(');
		int endParams = queryStr.lastIndexOf(')');
		String className = (startParams == -1)?queryStr:queryStr.substring(0,startParams);
		String params = (startParams == -1)?null:queryStr.substring(startParams + 1,endParams);
		try {
			Class<QueryProvider> clazz = (Class<QueryProvider>) Class.forName(queryPackage + className);
			QueryProvider query = clazz.newInstance();
			if(params != null){
				String[] paramsArray = params.split("::");
				for(String paramString : paramsArray){
					String[] paramParts = paramString.split(":");
					if(paramParts.length != 2){
						Logger.error("Invalid params passed to query " + className + "(" + paramString + ")");
						return null;
					}
					String key = paramParts[0];
					String value = paramParts[1];

					query.getQuery().setParameter(key, value);
				}
			}
			return query;
		} catch (ClassNotFoundException e) {
			Logger.error("Unrecognized query class " + className);
			return null;
		} catch (InstantiationException e) {
			Logger.error("Unable to instantiate query " + className + ". A no arg constructor is required.");
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String unbind(String key) {
		return "";
	}

	public String javascriptUnbind() {
		return null;
	}
}
