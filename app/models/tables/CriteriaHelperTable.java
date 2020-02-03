
package models.tables;

import controllers.ControllerBase;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.criteria.*;

import models.filter.AttributeFilter;
import models.tables.CriteriaQueryInfoExposer.Parser;
import models.tables.CriteriaQueryInfoExposer.TableSearchAppendage;
import play.db.jpa.JPA;
import play.i18n.Messages;
import utils.CriteriaHelper;
import utils.CriteriaHelper.RootTableKey;
import utils.CriteriaHelper.TableKey;
import utils.ObjectTransformer;

/**
 * <p>A <code>CriteriaHelperTable</code> is an {@link AjaxTable AjaxTable} that 
 * draws its data from the results of a <code>CriteriaQuery</code> that is
 * built up through a series of helper methods that mimic the interface of
 * {@link utils.CriteriaHelper CriteriaHelper}.</p>
 * 
 * <p>Defining such a table generally has two parts: creating a 
 * <code>CriteriaQuery</code> (via calls to the various helper methods of this
 * class) whose root set and where clause select the proper
 * entries, then exposing some of the data from those returned entries as either
 * columns of the resulting table or searchable portions of the data.  The 
 * column ordering defined by this table will reflect the order in which the 
 * columns were exposed.</p>
 */
public abstract class CriteriaHelperTable extends AjaxTable {
    
    private final CriteriaBuilder myBuilder = JPA.em().getCriteriaBuilder();
    
    private final CriteriaHelper myHelper;
    private final CriteriaQueryInfoExposer myInfoExposer = 
            new CriteriaQueryInfoExposer();

    public CriteriaHelperTable() {
        this(new CriteriaHelper());
    }
    
    public CriteriaHelperTable(CriteriaHelper h) {
        if (h.isClosed()) {
            throw new IllegalArgumentException("Cannot create a " + 
                    this.getClass() + " with a finish()ed " + h.getClass() + 
                    ".");
        }
        
        myHelper = h;
    }
    
    protected CriteriaHelper getHelper() {
        return myHelper;
    }

    public Subquery createSubquery() {
        return myHelper.createSubquery();
    }

    public void addGroup(Expression expr) {
        myHelper.addGroup(expr);
    }

    /**
     * <p>Returns the set of keys to all accessible entities.</p>
     * 
     * @return The set of keys to all accessible entities.
     */
    public Set<TableKey> getKeys() {
        return myHelper.getKeys();
    }


    /**
     * <p>Returns the set of keys to accessible entities of the given type.</p>
     * 
     * @return The set of keys to accessible entities of the given type.
     */
    public Set<TableKey> getKeys(Class c) {
        return myHelper.getKeys(c);
    }
    
    /**
     * @see utils.CriteriaHelper#addSourceEntity(java.lang.Class) 
     */
    public RootTableKey addSourceEntity(Class source) {
        return myHelper.addSourceEntity(source);
    }
    
    /**
     * @see utils.CriteriaHelper#getSourceRoot(utils.CriteriaHelper.RootTableKey)  
     */
    public Root getSourceRoot(RootTableKey source) {
        return myHelper.getSourceRoot(source);
    }
    
    /**
     * @see utils.CriteriaHelper#getEntity(utils.CriteriaHelper.TableKey) 
     */
    public From getEntity(TableKey table) {
        return myHelper.getEntity(table);
    }
    
    /**
     * @see utils.CriteriaHelper#field(utils.CriteriaHelper.TableKey, java.lang.String) 
     */
    public <T> Expression<T> field(TableKey from, String fieldName) {
        return myHelper.field(from, fieldName);
    }
    
    /**
     * @see utils.CriteriaHelper#field(utils.CriteriaHelper.TableKey, java.lang.String) 
     */
    public <T> Expression<T> field(TableKey from, String fieldName, 
                Class<T> type) {
        return myHelper.field(from, fieldName);
    }
    
    /**
     * <p>Adds a new column, as the next column in order, that reflects the 
     * value extracted from each entry by the given <code>Expression</code>.</p>
     * 
     * @param value The value to be included in the column.
     */
    public void addColumn(Expression data) {
        myInfoExposer.addColumn(data);
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
    public void addColumn(Expression data, ObjectTransformer transform) {
        myInfoExposer.addColumn(data, transform);
    }
    
    /**
     * <p>Adds a new column, as the next column in order, that reflects the 
     * value extracted from the named attribute of the indicated accessible
     * entity of each entry.</p>
     * 
     * @param from The accessible entity from which to draw the field.
     * @param fieldName The name of the field from which to draw the value.
     */
    public void addColumn(TableKey from, String fieldName) {
        myInfoExposer.addColumn(myHelper.field(from, fieldName));
    }
    
    /**
     * <p>Adds a new column, as the next column in order, that reflects the 
     * value extracted from the named attribute of the indicated accessible
     * entity of each entry and processed through the given transform..</p>
     * 
     * @param from The accessible entity from which to draw the field.
     * @param fieldName The name of the field from which to draw the value.
     * @param transform A mapping from the value extracted from the entry to
     *              the actual value that should appear in the table.
     */
    public void addColumn(TableKey from, String fieldName, 
            ObjectTransformer transform) {
        
        myInfoExposer.addColumn(myHelper.field(from, fieldName), transform);
    }
    
    /**
     * <p>Adds a new column, as the next column in order, that reflects the
     * value of the indicated accessible entity.</p>
     * 
     * @param from The accessible entity to include in the table.
     */
    public void addColumn(TableKey from) {
        myInfoExposer.addColumn(myHelper.getEntity(from));
    }
    
    /**
     * <p>Adds a new column, as the next column in order, that reflects the
     * value of the indicated accessible entity, processed through the given
     * transformer.</p>
     * 
     * @param from The accessible entity to include in the table.
     * @param transform A mapping from the entity to the actual value that
     *             should appear in the table.
     */
    public void addColumn(TableKey from, ObjectTransformer transform) {
        myInfoExposer.addColumn(myHelper.getEntity(from), transform);
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
    
    public void includeInDynamicSearch(String name, Expression<String> searchable) {
        myInfoExposer.includeInDynamicSearch(name, searchable);
    }
    
    /**
     * <p>Exposes the given value from an entry to exact-match search by first
     * decoding the search string into a comparable value, and then using
     * .equals() to compare.</p>
     * 
     * @param <T> The type of the extracted value.
     * @param searchable The extracted value.
     * @param parser A parser capable of transforming a search string into a
     *              value for matching.
     */
    public <T> void includeAsExactMatchSearch(Expression<T> searchable, 
                Parser<T> parser) {
        myInfoExposer.includeAsExactMatchSearch(searchable, parser);
    }
    
    /**
     * <p>Exposes the given accessible entity to exact-match search by first 
     * using a filter to decode the search string into a comparable value, and 
     * then using .equals() to compare.  Note that this does not actually apply
     * the filter, only uses the filter to extract the value from the search
     * string.</p>
     * 
     * @param table The accessible entity to which the filter's extraction 
     *              should be applied.
     * @param filter A filter whose {@link models.filter.AttributeFilter#fromString(java.lang.String) fromString()}
     *              method should be applied to extract the value.
     */
    public void includeAsExactMatchSearch(TableKey table,
                AttributeFilter filter) {
        myInfoExposer.includeAsExactMatchSearch(
                field(table, filter.getAttributeName()), 
                new FilterToParserAdapter(filter));
    }
    
    /**
     * @see utils.CriteriaHelper#join(utils.CriteriaHelper.TableKey, java.lang.String)
     */
    public TableKey join(TableKey from, String on) {
        return myHelper.join(from, on);
    }

    @Override
    public Object[] getColumnOrder() {
        return myInfoExposer.getColumnOrder();
    }

    /**
     * <p>A template method that permits concrete subclasses to provide 
     * additional conditions on entities beyond those enforced by search.  This
     * condition will be "and"ed with any search conditions.</p>
     * 
     * @return A condition.
     */
    public Expression<Boolean> getCondition() {
        return myBuilder.conjunction();
    }
    
    @Override
    public Paginator getPaginatableData(List<TableSearchAppendage> searches) {
    	
    	Expression<Boolean> searchPredicate = null;
    	for(TableSearchAppendage search : searches){
    		searchPredicate = myInfoExposer.append(search, searchPredicate);
    	}
    	
    	myHelper.addCondition(myInfoExposer.getWhereClause(
    			getCondition(), searchPredicate));
        
    	CriteriaQuery query = myHelper.finish();
        
        return new CriteriaQueryPaginator(query, 
                myInfoExposer.getSelectedColumns());
    }
    
    @Override
    public Paginator getPaginatableData(String searchString) {
    	myHelper.addCondition(
                myInfoExposer.getWhereClause(getCondition(), searchString));
        
        CriteriaQuery query = myHelper.finish();
        
        return new CriteriaQueryPaginator(query, 
                myInfoExposer.getSelectedColumns());
    }
    
    /**
     * <p>Uses an Object's toString() method as a key to look up the associated
     * text in the Messages file, using the resulting text as the object's
     * representation in the table.</p>
     */
    public static class ObjectToMessage implements ObjectTransformer {

        public static final ObjectTransformer INSTANCE = new ObjectToMessage();
        
        public Object transform(Object input) {
            return Messages.get(input);
        }       
    }
    
    /**
     * <p>Uses an Object's toString() method as the object's representation in 
     * the table.</p>
     */
    public static class ObjectToString implements ObjectTransformer {

        public static final ObjectTransformer INSTANCE = new ObjectToString();
        
        public Object transform(Object input) {
            return input.toString();
        }       
    }
    
    /**
     * <p>Transforms a <code>Date</code>-valued datum into a formatted 
     * <code>String</code> representation of the date.</p>
     */
    public static class DateToFormattedDateString implements ObjectTransformer {
        
        public static final ObjectTransformer INSTANCE = 
                new DateToFormattedDateString();
        
        public Object transform(Object input) {
            return ControllerBase.dateFormat.format((Date) input);
        }
    }
    
    public static class DefaultForNull implements ObjectTransformer {
        
        private final Object myDefault;
        
        public DefaultForNull(Object defaultValue) {
            myDefault = defaultValue;
        }

        public Object transform(Object input) {
            Object result = input;
            
            if (result == null) {
                result = myDefault;
            }
            
            return result;
        }
    }
    
    private class FilterToParserAdapter<T> implements Parser<T> {

        private final AttributeFilter<?, T> myFilter;
        
        public FilterToParserAdapter(AttributeFilter<?, T> filter) {
            myFilter = filter;
        }
        
        public T parse(String s) throws FormatException {
            T result = myFilter.fromString(s);
            
            if (result == null) {
                throw new FormatException();
            }
            
            return result;
        }
    }
}
