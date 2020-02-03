/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;
import models.filter.Filter;
import play.db.jpa.JPA;

/**
 * <p>A <code>FilteredCriteriaHelper</code> allows the conditions of existing
 * {@link models.filter.Filter Filter}s to be added as conjuncts of its
 * <code>CriteriaQuery</code>, using a "best guess" of what accessible entity
 * the filter applies to.</p>
 * 
 * <p>At issue (and the reason this class exists as separate from 
 * {@link QueryBaseHelper QueryBaseHelper}) is that in JPA terms, in order to
 * filter the entries of a query, you must have full knowledge of the entire
 * structure of the query (e.g., you need handles on its roots to extract
 * values from and build queries around them.)  
 * {@link models.query.QueryBase QueryBase}s operate under the assumption that
 * they have exactly one root, period.  But an arbitrary query may have many
 * or none.  This class solves the problem of which root a filter should apply
 * to for the (common) case that a filter operates on an entity type for which 
 * only a single root exists in the query.</p>
 * 
 * <p>So, for example, the ByText filter can be applied to a query that involves
 * only one question root.  But if the query were over entities involving all
 * pairwise matching of questions, if would fail.</p>
 */
public class FilteredCriteriaHelper extends CriteriaHelper {
    
    private List<Filter> myFilters = new LinkedList<Filter>();
    
    /**
     * <p>Adds the given filter as a conjunct of the resulting query.  
     * The filter must operate on a type for which, at <code>finish()</code>
     * time, <em>exactly one</em> root exists in the underlying query.</p>
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
    
    /**
     * <p>This <code>finish()</code> operates just as in the super class,
     * but may throw an <code>IllegalStateException</code> if one or more 
     * filters operates on an entity type for which zero or multiple roots
     * exists.</p> 
     */
    @Override
    public CriteriaQuery finish() {
        CriteriaBuilder builder = JPA.em().getCriteriaBuilder();
        
        CriteriaQuery query = super.finish();
        
        Expression<Boolean> originalWhere = query.getRestriction();
        
        Root matchingRoot;
        Expression<Boolean> finalWhere = originalWhere;
        for (Filter filter : myFilters) {
            matchingRoot = getIntendedRoot(query, filter.getEntityClass());
            finalWhere = 
                    builder.and(finalWhere, filter.asPredicate(matchingRoot));
        }
        
        query.where(finalWhere);
        
        return query;
    }
    
    private Root getIntendedRoot(CriteriaQuery query, Class entityType) {
        Set<Root> roots = (Set<Root>) query.getRoots();
        Root result = null;
        
        for (Root r : roots) {
            if (r.getJavaType().equals(entityType)) {
                if (result != null) {
                    throw new IllegalStateException("Multiple roots match " +
                            entityType + ".");
                }
                
                result = r;
            }
        }
        
        if (result == null) {
            throw new IllegalStateException("No roots match " + entityType + 
                    ".");
        }
        
        return result;
    }
}
