
package utils;

import java.util.LinkedList;
import java.util.List;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;
import models.filter.Filter;
import models.query.QueryBase;
import play.db.jpa.JPA;

/**
 * <p>Extends {@link CriteriaHelper CriteriaHelper} to assist in creating a
 * <code>CriteriaQuery</code> based on a 
 * {@link models.query.QueryBase QueryBase}.  To create a 
 * <code>CriteriaQuery</code> that is equivalent to a <code>QueryBase</code>,
 * simply do:</p>
 * 
 * <pre>
 * new QueryBaseHelper(queryBaseInstance).finish()
 * </pre>
 * 
 * <p>However, between instantiation and a call to 
 * {@link CriteriaHelper#finish() finish()}, additional manipulations can be
 * made to modify the query.</p>
 */
public class QueryBaseHelper extends CriteriaHelper {
    
    private final QueryBase myQueryBase;
    
    private final RootTableKey myRootKey;
    
    private List<Filter> myFilters = new LinkedList<Filter>();
    
    public QueryBaseHelper(QueryBase qb) {
        myQueryBase = qb;
        myRootKey = addSourceEntity(qb.getEntityClass());
    }
    
    /**
     * <p>Returns the <code>RootTableKey</code> associated with the source 
     * entity corresponding to the entity type of the <code>QueryBase</code> 
     * upon which this <code>QueryBaseHelper</code> is based.</p>
     * 
     * @return The key associated with the <code>QueryBase</code>'s entity type.
     */
    public RootTableKey getQueryBaseRootKey() {
        return myRootKey;
    }
    
    /**
     * <p>Returns a JPA <code>Root</code> corresponding to the source entity 
     * corresponding to the entity type of the <code>QueryBase</code> 
     * upon which this <code>QueryBaseHelper</code> is based.</p>
     * 
     * @return The <code>Root</code> corresponding to the 
     *         <code>QueryBase</code>'s entity type.
     */
    public Root getQueryBaseRoot() {
        return getSourceRoot(myRootKey);
    }
    
    /**
     * <p>Returns a JPA <code>Expression</code> corresponding to the named field
     * of the <code>QueryBase</code>'s entity type.</p>
     * 
     * @param <T> The expected type of the field.
     * @param name The name of the field.
     * 
     * @return The <code>Expression</code> corresponding to the named field of
     *         the <code>QueryBase</code>'s entity type.
     */
    public <T> Expression<T> rootField(String name) {
        return getQueryBaseRoot().get(name);
    }
    
    /**
     * <p>Returns a JPA <code>Expression</code> corresponding to the named field
     * of the <code>QueryBase</code>'s entity type.</p>
     * 
     * @param name The name of the field.
     * @param type The expected type of the field.
     * 
     * @return The <code>Expression</code> corresponding to the named field of
     *         the <code>QueryBase</code>'s entity type.
     */
    public <T> Expression<T> rootField(String name, Class<T> type) {
        return rootField(name);
    }
    
    /**
     * <p>Adds the given filter as a conjunct of the resulting query.  
     * The filter must operate on the same type as the <code>QueryBase</code>.
     * </p>
     * 
     * <p>Once {@link #finish() finish()} has been called, additional calls to
     * this method will result in an <code>IllegalStateException</code>.</p>
     * 
     * @param f The filter to add as a conjunct.
     */
    public void addFilter(Filter f) {
        if (isClosed()) {
            throw new IllegalStateException("Cannot modify the query after " +
                    "a call to finish().");
        }
        
        myFilters.add(f);
    }
    
    @Override
    public CriteriaQuery finish() {
        CriteriaBuilder builder = JPA.em().getCriteriaBuilder();
        Root queryBaseRoot = getQueryBaseRoot();
        
        CriteriaQuery query = super.finish();
        
        Expression<Boolean> originalWhere = query.getRestriction();
        
        Expression<Boolean> queryBaseWhere = 
                myQueryBase.buildCriteria(builder, getQueryBaseRoot(), query);
        
        Expression<Boolean> finalWhere = 
                builder.and(originalWhere, queryBaseWhere);
        
        for (Filter filter : myFilters) {
            finalWhere = 
                    builder.and(finalWhere, filter.asPredicate(queryBaseRoot));
        }
        
        query.where(finalWhere);
        
        return query;
    }
}
