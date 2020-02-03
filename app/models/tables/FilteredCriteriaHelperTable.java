
package models.tables;

import java.util.Iterator;
import java.util.List;
import models.filter.Filter;
import utils.CriteriaHelper.TableKey;
import utils.FilteredCriteriaHelper;

/**
 * <p>A refined version of {@link CriteriaHelperTable CriteriaHelperTable} that
 * operates specifically on a 
 * {@link utils.FilteredCriteriaHelper FilteredCriteriaHelper} and thus is able
 * to implement {@link models.tables.Filterable Filterable}.</p>
 */
public abstract class FilteredCriteriaHelperTable extends CriteriaHelperTable
            implements Filterable {

    private final FilteredCriteriaHelper myFilteredHelper;
    
    public FilteredCriteriaHelperTable() {
        this(new FilteredCriteriaHelper());
    }
    
    public FilteredCriteriaHelperTable(FilteredCriteriaHelper h) {
        super(h);
        myFilteredHelper = h;
    }
    
    public void addFilter(Filter filter) {
        myFilteredHelper.addFilter(filter);
    }
    
    public void addFilters(List<Filter> filters) {
        for (Filter f : filters) {
            addFilter(f);
        }
    }
    
    public TableKey getSoleEntityKey(Class c) {
        Iterator<TableKey> keys = getKeys(c).iterator();
        
        if (!keys.hasNext()) {
            throw new IllegalArgumentException("No root of type " + c + ".");
        }
        
        TableKey result = keys.next();
        
        if (keys.hasNext()) {
            throw new IllegalArgumentException(
                    "Multiple roots of type " + c + ".");
        }
        
        return result;
    }
}
