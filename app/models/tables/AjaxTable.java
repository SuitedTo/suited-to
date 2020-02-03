
package models.tables;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.User;
import models.tables.CriteriaQueryInfoExposer.TableSearchAppendage;

import org.apache.commons.lang.StringEscapeUtils;

/**
 * <p>An <code>AjaxTable</code> represents the server-side data source of some 
 * client-side table view.  It returns a model of its underlying data as a
 * {@link Paginator Paginator}.  It also defines an ordering of object keys that
 * appear in the paginator rows and so indicates in what order the columns ought
 * be displayed.  Finally, it defines an access-control method that determines
 * if a given user is permitted to pull the data in this table.</p>
 * 
 * <p>Any concrete subclass of <code>AjaxTable</code> that is defined in the 
 * same package as this class, or in a package rooted in the package of this 
 * class, will be automatically accessible via the 
 * {@link controllers.Data#getReport getReport()} method, which can either be
 * invoked directly, or accessed via the helper tag #{ajaxTable}.</p>
 */
public abstract class AjaxTable {
    
    /**
     * <p>This field is lazily initialized.  Any method that would like to work
     * with it must check if it is null and, if so, initialize it with
     * {@link #lazyInitializeMapping() lazyInitializeMapping()}.</p>
     */
    private Map<Object, Integer> myColumnMapping;
    
    private void lazyInitializeMapping() {
        myColumnMapping = new HashMap<Object, Integer>();
        
        Object[] columns = getColumnOrder();
        for (int index = 0; index < columns.length; index++) {
            myColumnMapping.put(columns[index], index);
        }
    }
    
    /**
     * <p>Returns the (0-indexed) position of the named column.  If the given
     * object does not name a column of this table, throws an 
     * <code>IllegalArgumentException</code>.</p>
     * 
     * @param column The column name.
     * 
     * @return The column index.
     */
    public final int getColumnIndex(Object column) {
        
        if (myColumnMapping == null) {
            lazyInitializeMapping();
        }
        
        if (!myColumnMapping.containsKey(column)) {
            throw new IllegalArgumentException("No such column: " + column);
        }
        
        return myColumnMapping.get(column);
    }
    
    /**
     * <p>Takes a map containing the data of a row, and returns the same data
     * as a list in the proper order, discarding the column names.</p>
     * 
     * @param data The (unordered) map-based data.
     * @return The (ordered) list-based data.
     */
    public final List<Object> orderMap(Map<Object, Object> data) {
        Object[] columns = getColumnOrder();
        List<Object> result = new ArrayList<Object>(columns.length);
        
        for (Object column : columns) {
            if (!data.containsKey(column)) {
                throw new RuntimeException("Map does not contain an expected " +
                        "column: " + column);
            }
            //automatically escape HTML from string values to prevent XSS attacks
            Object columnValue = data.get(column);
            if(columnValue instanceof String){
                result.add(StringEscapeUtils.escapeHtml((String)columnValue));
            } else {
                result.add(columnValue);
            }
        }
        
        return result;
    }
    
    /**
     * <p>Defines whether or not the given user may access the data of this
     * table.  A <code>null</code> user can be passed to ask if the table is
     * publicly accessible.</p>
     * 
     * @param u The user, or <code>null</code> if the question is whether or not
     *              this table is publicly accessible.
     * 
     * @return <code>true</code> <strong>iff</strong> the given user (or the 
     *         public, in the case of <code>null</code>) is permitted to access
     *         this table.
     */
    public abstract boolean canAccess(User u);
    
    /**
     * <p>Returns an array indicating the order in which the data columns of
     * this table ought be displayed.  Every column listed in the result is
     * guaranteed to be a valid column name in the result rows of a call to
     * {@link #getPaginatableData(java.lang.String) getPaginatableData()}, but
     * every column name that appears in the paginator need not appear in this
     * list (which provides a mechanism for selecting out only certain columns,
     * for example.)</p>
     * 
     * @return The column names in the order they should be displayed.
     */
    public abstract Object[] getColumnOrder();
    
    /**
     * <p>Returns a paginatable model of the data that matches the given
     * search string.</p>
     * 
     * @param searchString A search string to be used to restrict the data of
     *              the table.  How this search string is applied is 
     *              contextually determined.
     * 
     * @return A {@link Paginator Paginator} over the data of this table.
     */
    public abstract Paginator getPaginatableData(String searchString);
    
    public Paginator getPaginatableData(List<TableSearchAppendage> searches){
    	return null;
    }
}
