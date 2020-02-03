package models.prep.ewrap;

import java.util.Map;

import play.libs.F.Option;

/**
 * Builds a query result.
 * 
 * @author joel
 *
 * @param <T>
 */
public interface QueryResultBuilder <T>{
	
	/**
	 * Apply the given filter to the underlying query.
	 * 
	 * @param filter The filter to apply.
	 * @param params Filter parameters
	 * @return Builder for chaining
	 */
	public QueryResultBuilder<T> applyFilter(Option<QueryFilter<T>> filter, Option<Map<String, Object>> params);
	
	/**
	 * Apply the given filter to the underlying query.
	 * 
	 * @param filters The filter to apply
	 * @return Builder for chaining
	 */
	public QueryResultBuilder<T> applyFilters(Option<Filters> filters);
	
	/**
	 * Apply the given search to the underlying query.
	 * 
	 * @param search The search to apply
	 * @return Builder for chaining
	 */
	public QueryResultBuilder<T> applySearch(Option<Search> search);
	
	/**
	 * Set the given page for the underlying query.
	 * 
	 * @param page The page to apply
	 * @return Builder for chaining
	 */
	public QueryResultBuilder<T> setPage(Option<Page> page);
	
	/**
	 * Apply the given sort to the underlying query.
	 * 
	 * @param sort The sort to apply
	 * @return Builder for chaining
	 */
	public QueryResultBuilder<T> applySort(Option<Sort> sort);
	
	/**
	 * Get the final result
	 * @return
	 */
	public QueryResult<T> getResult();
}
