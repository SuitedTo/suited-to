package models.query;

import models.ModelBase;
import org.apache.commons.lang.StringUtils;
import org.hibernate.CacheMode;

import play.Play;
import play.db.jpa.JPA;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

/**
 * Base class for db queries.
 *
 * @author joel
 */
public abstract class QueryBase<M extends ModelBase> {

    protected Integer iSortCol_0;
    protected String sSortDir_0;
    protected Integer firstResult;
    protected Integer maxResults;
    protected List<QueryBase> and;


    public QueryBase() {
        and = new ArrayList<QueryBase>();
    }

    /**
     * Adds the selection criteria from the given query to this one.
     *
     * @param and
     * @return
     */
    public QueryBase<M> and(QueryBase and) {
        this.and.add(and);
        return this;
    }

    public QueryBase(Integer iSortCol_0,
                     String sSortDir_0,
                     String sSearch,
                     Integer iDisplayStart,
                     Integer iDisplayLength) {
        this();
        init(iSortCol_0, sSortDir_0, iDisplayStart, iDisplayLength);
    }

    public void init(Integer iSortCol_0,
                     String sSortDir_0,
                     Integer iDisplayStart,
                     Integer iDisplayLength) {

        this.iSortCol_0 = iSortCol_0;

        this.sSortDir_0 = sSortDir_0;

        firstResult = iDisplayStart != null ? iDisplayStart : 0;

        if (iDisplayLength != null) {
            maxResults = iDisplayLength;
        } else {
            String processing = Play.configuration.getProperty("dataTable.processing");
            if ((processing != null) && (processing.equals("client"))) {
                maxResults = Integer.MAX_VALUE;
            } else {
                maxResults = Integer.valueOf(Play.configuration.getProperty("default.query.max"));
            }
        }

    }

    public abstract String[] getSortableColumnNames();

    public abstract Class<M> getEntityClass();

    public abstract Predicate buildCriteria(CriteriaBuilder criteriaBuilder,
                                            Root<M> root,
                                            CriteriaQuery<Object> criteriaQuery);
    
    private Predicate getCriteria(CriteriaBuilder criteriaBuilder, CriteriaQuery<Object> entityQuery, Root<M> root){
    	Predicate criteria = buildCriteria(criteriaBuilder, root, entityQuery);

        for (QueryBase b : and) {
            criteria = criteriaBuilder.and(criteria, b.buildCriteria(criteriaBuilder, root, entityQuery));
        }
        
        return criteria;
    }
    
    private TypedQuery buildTypedQuery(CriteriaBuilder criteriaBuilder,
            Root<M> root,
            CriteriaQuery<Object> entityQuery,
            Predicate criteria){
    	
    	entityQuery.select(root);

        entityQuery.where(criteria);

        String orderBy = iSortCol_0 != null ? getSortableColumnNames()[iSortCol_0] : null;
        if (StringUtils.isNotBlank(orderBy)) {
            if ("asc".equals(sSortDir_0)) {
                entityQuery.orderBy(criteriaBuilder.asc(root.get(orderBy)));
            } else {
                entityQuery.orderBy(criteriaBuilder.desc(root.get(orderBy)));
            }
        } else { //default to ordering by id in descending order
            entityQuery.orderBy(criteriaBuilder.desc(root.get("id")));
        }

        TypedQuery query = JPA.em().createQuery(entityQuery);

        if (firstResult != null) {
            query.setFirstResult(firstResult);
        }

        if (maxResults != null) {
            query.setMaxResults(maxResults);
        }
        
        return query;
    }
    
    private long getCount(CriteriaBuilder criteriaBuilder,
            Root<M> root,
            CriteriaQuery<Object> entityQuery,
            Predicate criteria){
    	
    	entityQuery.select(criteriaBuilder.count(root)).where(criteria).groupBy(root.get("id"));
    	
        TypedQuery<Object> countQuery = JPA.em().createQuery(entityQuery);
        
        countQuery.setHint("org.hibernate.cacheable", true);
        
        return countQuery.getResultList().size();
    }
    
    /**
     * Call this method only in the rare case where only a count is needed
	 * and the full query results with entity objects will not be needed. This
	 * improves performance because hibernate doesn't have to load the objects
	 * at all.
     * 
     * @return
     */
    public long executeCountQuery(){
    	
    	CriteriaBuilder criteriaBuilder = JPA.em().getCriteriaBuilder();

        CriteriaQuery<Object> entityQuery = criteriaBuilder.createQuery();

        Root<M> root = entityQuery.from(getEntityClass());

        Predicate criteria = getCriteria(criteriaBuilder, entityQuery, root);

        return getCount(criteriaBuilder, root, entityQuery, criteria);
    }

    /**
     * Executes this query
     *
     * @return
     */
    public QueryResult<M> executeQuery() {

        CriteriaBuilder criteriaBuilder = JPA.em().getCriteriaBuilder();

        CriteriaQuery<Object> entityQuery = criteriaBuilder.createQuery();

        Root<M> root = entityQuery.from(getEntityClass());

        Predicate criteria = getCriteria(criteriaBuilder, entityQuery, root);

        TypedQuery query = buildTypedQuery(criteriaBuilder, root, entityQuery, criteria);

        long count = getCount(criteriaBuilder, root, entityQuery, criteria);
        
        query.setHint("org.hibernate.cacheable", true);
        
        return new QueryResult(count, query.getResultList());
    }

}
