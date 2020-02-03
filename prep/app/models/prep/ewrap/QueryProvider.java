package models.prep.ewrap;

import com.avaje.ebean.Query;

/**
 * 
 * A QueryProvider provides queries for binding to params. Don't use
 * a Query unless there's a good reason - use Filters where possible.
 * 
 * When building the query, use as much of the Ebean api as possible. For
 * example, if you build an ordered query then use query.orderBy instead of
 * adding the order by clause to your raw sql. If you build a limited query then
 * use query.setMaxRows instead of adding the limit clause to your raw sql.
 * 
 * 
 * @author joel
 *
 * @param <T>
 */
public interface QueryProvider<T> {

	/**
	 * Return the query that was built during object construction. Don't build
	 * the query here because each call would then overwrite cumulative changes
	 * to the query.
	 *
	 * 
	 * @return The query
	 */
	public Query<T> getQuery();
}
