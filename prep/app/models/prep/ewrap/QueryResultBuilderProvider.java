package models.prep.ewrap;

import play.libs.F.Option;
import models.prep.EbeanModelBase;
import models.prep.mappedsuperclasses.SqlEntity;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Expr;
import common.utils.prep.SecurityUtil;

/**
 * 
 * Provides <code>QueryResultBuilder</code> instances for different types of entities
 * 
 * @author joel
 *
 */
public class QueryResultBuilderProvider {
	
	@SuppressWarnings("unchecked")
	public static <T extends EbeanModelBase> QueryResultBuilder<T> getQueryResultBuilder(Option<Query> maybeQuery, Class<T> clazz){
		
		for(Query query : maybeQuery){
			return DefaultQueryResultBuilder.instance(query.queryContainer.getQuery(), defaultQueryBuildContext());
		}
		return getQueryResultBuilder(clazz);
		
	}
	
	public static <T> QueryResultBuilder<T> getQueryResultBuilder(SqlEntity<T> sqlEntity){
		
		return SqlEntity.<T>getQueryResultBuilder(sqlEntity, defaultQueryBuildContext());
		
	}
	
	public static <T extends EbeanModelBase> QueryResultBuilder<T> getQueryResultBuilder(Class<T> clazz){
		
		return DefaultQueryResultBuilder.instance(Ebean.find(clazz), defaultQueryBuildContext());
		
	}

	public static <T> QueryResultBuilder<T> getQueryResultBuilder(SqlEntity<T> sqlEntity, 
			QueryBuildContext context){
		
		return SqlEntity.<T>getQueryResultBuilder(sqlEntity, context);
		
	}
	
	public static <T extends EbeanModelBase> QueryResultBuilder<T> getQueryResultBuilder(Class<T> clazz, 
			QueryBuildContext context){
		
		return DefaultQueryResultBuilder.instance(Ebean.find(clazz), context);
		
	}
	
	private static QueryBuildContext defaultQueryBuildContext(){
		return new QueryBuildContext(SecurityUtil.connectedUser());
	}
}
