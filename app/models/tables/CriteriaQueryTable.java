
package models.tables;

import java.util.List;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;

import models.tables.CriteriaQueryInfoExposer.TableSearch;
import models.tables.CriteriaQueryInfoExposer.TableSearchAppendage;
import play.db.jpa.JPA;
import utils.ObjectTransformer;

/**
 * <p>A <code>CriteriaQueryTable</code> is an {@link AjaxTable AjaxTable} that 
 * draws its data from the results of a pre-defined <code>CriteriaQuery</code>.
 * </p>
 * 
 * <p>Defining such a table generally has two parts: creating a 
 * <code>CriteriaQuery</code> whose root set and where clause select the proper
 * entries, then exposing some of the data from those returned entries as either
 * columns of the resulting table or searchable portions of the data.  The 
 * column ordering defined by this table will reflect the order in which the 
 * columns were exposed.</p>
 */
public abstract class CriteriaQueryTable extends AjaxTable {

    private final CriteriaQuery myCriteriaQuery;
    private Expression<Boolean> myOriginalRestriction;
    
    private CriteriaQueryInfoExposer myInfoExposer = 
            new CriteriaQueryInfoExposer();
    
    /**
     * <p>Creates a new <code>CriteriaQueryTable</code> based on the given
     * <code>CriteriaQuery</code>.  Note that this class will modify, edit, and
     * dissect this query brutally, so the client should cease to observe or
     * modify the query after using it to construct a 
     * <code>CriteriaQueryTable</code>.</p>
     * 
     * @param q The query to base this table on.
     */
    public CriteriaQueryTable(CriteriaQuery q) {
        myCriteriaQuery = q;
        myOriginalRestriction = q.getRestriction();
        
        if (myOriginalRestriction == null) {
            myOriginalRestriction = JPA.em().getCriteriaBuilder().conjunction();
        }
    }

    /**
     * <p>Adds a new column, as the next column in order, that reflects the 
     * value extracted from each entry by the given <code>Expression</code>.</p>
     * 
     * @param value The value to be included in the column.
     */
    public void addColumn(Expression value) {
        myInfoExposer.addColumn(value);
    }
    
    /**
     * <p>Adds a new column, as the next column in order, that reflects the 
     * value extracted from each entry by the given <code>Expression</code>,
     * processed through the given transform.</p>
     * 
     * @param value The value to be included in the column.
     * @param transform A mapping from the value extracted from the entry to
     *              the actual value that should appear in the table.
     */
    public void addColumn(Expression value, ObjectTransformer transform) {
        myInfoExposer.addColumn(value, transform);
    }
    
    /**
     * <p>Exposes the given expression to the search string passed to
     * {@link #getPaginatableData(java.lang.String) getPaginatableData()}.
     * Entries will only be included in the final table if one of their exposed
     * portions contains the search string as a substring, ignoring case.</p>
     * 
     * @param searchable The expression to expose to search.
     */
    public void includeInSearch(Expression<String> searchable) {
        myInfoExposer.includeInSearch(searchable);
    }
    
    @Override
    public Object[] getColumnOrder() {
        return myInfoExposer.getColumnOrder();
    }

    @Override
    public Paginator getPaginatableData(String searchString) {
       
    	 myCriteriaQuery.where(myInfoExposer.getWhereClause(
                 myOriginalRestriction, searchString));
         
         return new CriteriaQueryPaginator(myCriteriaQuery, 
                 myInfoExposer.getSelectedColumns());
    }
    
    @Override
    public Paginator getPaginatableData(List<TableSearchAppendage> searches) {
    	Expression<Boolean> searchPredicate = null;
    	for(TableSearchAppendage search : searches){
    		searchPredicate = myInfoExposer.append(search, searchPredicate);
    	}
    	
    	myCriteriaQuery.where(myInfoExposer.getWhereClause(
                myOriginalRestriction, searchPredicate));
    	
    	return new CriteriaQueryPaginator(myCriteriaQuery, 
                myInfoExposer.getSelectedColumns());
       
    }
}
