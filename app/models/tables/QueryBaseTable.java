
package models.tables;

import java.util.List;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;
import models.filter.Filter;
import models.query.QueryBase;
import utils.CriteriaHelper.RootTableKey;
import utils.QueryBaseHelper;

/**
 * <p>Adapts an existing {@link models.query.QueryBase QueryBase} into a
 * table, maintaining its <code>where</code> clause and starting with a root set
 * consisting of the entity on which the query is defined.  After being
 * constructed, the table may be further modified via the normal 
 * {@link CriteriaHelperTable CriteriaHelperTable} interface.</p>
 */
public abstract class QueryBaseTable extends CriteriaHelperTable 
            implements Filterable {

    private final QueryBaseHelper myHelper;
    
    public QueryBaseTable(QueryBase qb) {
        super(new QueryBaseHelper(qb));
        
        myHelper = (QueryBaseHelper) getHelper();
    }
    
    /**
     * <p>Returns the root associated with the QueryBase's entity.</p>
     */
    public Root getQueryBaseRoot() {
        return myHelper.getQueryBaseRoot();
    }
    
    /**
     * <p>Returns the key to the accessible entity spawned by the QueryBase's 
     * entity.</p>
     */
    public RootTableKey getQueryBaseRootKey() {
        return myHelper.getQueryBaseRootKey();
    }
    
    public <T> Expression<T> rootField(String name) {
        return getQueryBaseRoot().get(name);
    }
    
    public <T> Expression<T> rootField(String name, Class<T> type) {
        return rootField(name);
    }
    
    public void addFilter(Filter f) {
        myHelper.addFilter(f);
    }

    public void addFilters(List<Filter> filters) {
        for (Filter f : filters) {
            myHelper.addFilter(f);
        }
    }    
}
