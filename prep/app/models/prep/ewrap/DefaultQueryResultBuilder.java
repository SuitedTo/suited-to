package models.prep.ewrap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


import org.apache.commons.lang.StringUtils;

import play.libs.F.Option;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Expr;
import com.avaje.ebean.Expression;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Junction;
import com.avaje.ebean.OrderBy;
import com.avaje.ebean.OrderBy.Property;
import com.avaje.ebean.Query;
import com.avaje.ebean.QueryIterator;
import com.avaje.ebean.SqlQuery;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.api.SpiExpression;
import com.avaje.ebeaninternal.api.SpiExpressionList;
import com.avaje.ebeaninternal.api.SpiQuery;
import com.avaje.ebeaninternal.server.core.OrmQueryRequest;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.util.DefaultExpressionRequest;

/**
 * The default QueryResultBuilder implementation. This is a helper class to
 * provide a way to apply paging, sorting, filtering, etc to a query.
 * 
 * @author joel
 * 
 * @param <T>
 */
public class DefaultQueryResultBuilder<T> implements QueryResultBuilder<T> {

	/**
	 * Max result size limit
	 */
	private static final int MAX_RESULT_SIZE = 
			Integer.parseInt(play.Play.configuration.getProperty("query.all.max_result_size", "300"));

	/**
	 * Whether or not this query is paged
	 */
	private boolean paged = false;

	/**
	 * Actual result size limit
	 */
	private int limit = -1;

	/**
	 * Context for query building
	 */
	private final QueryBuildContext context;

	/**
	 * The underlying query
	 */
	private Query<T> query;
	
	private List<Expression> searches = new ArrayList<Expression>();

	private DefaultQueryResultBuilder(QueryBuildContext context) {
		this.context = context;
	}

	public static <T> DefaultQueryResultBuilder<T> instance(Query<T> query, QueryBuildContext context) {
		DefaultQueryResultBuilder<T> instance = new DefaultQueryResultBuilder<T>(context);
		instance.query = query;
		return instance;
	}
	
	private static Option noneIfNull(Option option){
		return (option == null)?play.libs.F.None:option;
	}

	/* (non-Javadoc)
	 * @see models.queries.QueryResultBuilder#apply(play.libs.F.Option, play.libs.F.Option)
	 */
	@Override
	public QueryResultBuilder<T> applyFilter(Option<QueryFilter<T>> maybeFilter,
			Option<Map<String, Object>> maybeParams) {
		maybeFilter = noneIfNull(maybeFilter);
		maybeParams = noneIfNull(maybeParams);
		for (QueryFilter<T> f : maybeFilter) {
			f.apply(query.where(), context, maybeParams);
		}
		return this;
	}

	/* (non-Javadoc)
	 * @see models.queries.QueryResultBuilder#apply(play.libs.F.Option)
	 */
	@Override
	public QueryResultBuilder<T> applyFilters(Option<Filters> maybeFilters) {
		maybeFilters = noneIfNull(maybeFilters);
		for (Filters filters : maybeFilters) {
			filters.apply(query.where(), context);
		}
		return this;
	}

	/* (non-Javadoc)
	 * @see models.queries.QueryResultBuilder#getResult()
	 */
	@Override
	public QueryResult<T> getResult() {

		// Here we impose a universal limit on query result size
		if ((limit < 1) || (limit > MAX_RESULT_SIZE)) {
			int maxRows = query.getMaxRows();
			if((maxRows <= 0) || (maxRows > MAX_RESULT_SIZE)){
				query.setMaxRows(MAX_RESULT_SIZE);
			}
		}

		List<T> results = query.findList();

		if(paged){

			int rowCount;

			if(query.getRawSql() == null){

				rowCount = query.findRowCount();

			} else {

				/*
				 * Found out the hard way that findRowCount doesn't work for RawSql
				 * based queries. There's no exception thrown to tell us that it's not available.
				 * In some cases it actually returns a count without any exceptions thrown at all
				 * but that count is typically incorrect.
				 * So we're on our own. Not sure if there's a more direct way to
				 * do this. The difficulty comes with the fact that binding isn't done until the query
				 * is being executed so to get the bind values to rebuild the where clause
				 * we build a query request and add bind values to it from each expression, then
				 * we get the collected bind values from that request and apply them.
				 */
				
				//We want the count query to exclude search criteria so remove them here from the underlying list
				List<SpiExpression> preList = ((SpiExpressionList)query.where()).getUnderlyingList();
				Iterator<SpiExpression> it = preList.iterator();
				while(it.hasNext()){
					SpiExpression e = it.next();
					if(searches.contains(e)){
						it.remove();
					}
				}

				SpiEbeanServer server = ((SpiEbeanServer)Ebean.getServer("default"));
				BeanDescriptor<T> desc = server.getBeanDescriptor(((SpiQuery)query).getBeanType());
				OrmQueryRequest<T> request = (OrmQueryRequest<T>) server.createQueryRequest(desc, (SpiQuery)query, Ebean.currentTransaction());
				DefaultExpressionRequest whereReq = new DefaultExpressionRequest(request, request.createDeployParser());
				

				List<SpiExpression> list = ((SpiExpressionList)query.where()).getUnderlyingList();
				for(SpiExpression expression : list){
					expression.addBindValues(whereReq);
				}
				
				//Need to regenerate the sql string so build a new query here from the request that was built with searches removed.
				String sql = StringUtils.lowerCase(server.getQueryEngine().buildQuery(request).getGeneratedSql());

				sql = "select count(*) " + sql.substring(sql.indexOf("from"), sql.lastIndexOf("limit"));

				List<Object> bindValues = whereReq.getBindValues();
				for(Object value : bindValues){
					sql = sql.replaceFirst("\\?", String.valueOf(value));
				}
				
				boolean sorted = !query.orderBy().isEmpty();
				if(sorted){
					sql = sql.replaceAll("(order by)\\s+[\\w,.]+\\s+(asc|desc)?", "");
				}

				SqlQuery sqlQuery = Ebean.createSqlQuery(sql);

				rowCount = sqlQuery.findUnique().getInteger("count(*)");

			}
			return new PagedQueryResult<T>(results, rowCount);
		}
		return new QueryResult<T>(results);

	}

	/* (non-Javadoc)
	 * @see models.queries.QueryResultBuilder#setPage(play.libs.F.Option)
	 */
	@Override
	public QueryResultBuilder<T> setPage(Option<Page> maybePage) {
		maybePage = noneIfNull(maybePage);
		paged = false;
		for (Page page : maybePage) {
			if (page.size >= 0) {
				query.setFirstRow(page.index * page.size);
				query.setMaxRows(page.size);
				limit = page.size;
				paged = true;
			}
		}
		return this;
	}

	/* (non-Javadoc)
	 * @see models.queries.QueryResultBuilder#applySearch(play.libs.F.Option)
	 */
	@Override
	public QueryResultBuilder<T> applySearch(Option<Search> maybeSearch) {
		maybeSearch = noneIfNull(maybeSearch);
		for (Search search : maybeSearch) {
			while(search.hasNext()){
				Search next = search.next();
				for (String searchString : next.searchStrings) {
					Junction<T> dj = query.where().disjunction();
					for (String propName : next.columns) {
						dj.add(Expr.ilike(propName, "%" + searchString + "%"));
					}
					searches.add(dj);
					query.where().add(dj);
				}
			}
		}
		return this;
	}

	/* (non-Javadoc)
	 * @see models.queries.QueryResultBuilder#setSort(play.libs.F.Option)
	 */
	@Override
	public QueryResultBuilder<T> applySort(Option<Sort> maybeSort) {
		maybeSort = noneIfNull(maybeSort);
		for (Sort sort : maybeSort) {
			String[] sortParts = sort.next().split(" ");
			
			query.orderBy()
			.add(new Property(sortParts[0], sortParts[1].equalsIgnoreCase("asc")));
		}
		return this;
	}

	public QueryBuildContext getContext() {
		return context;
	}
}
