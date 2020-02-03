
package utils;

import java.util.*;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import play.db.jpa.JPA;

/**
 * <p>This class implements the Builder pattern and is designed to take some of
 * the pain out of creating JPA queries.</p>
 * 
 * <p>The builder keeps track of a list of
 * <em>source JPA entities</em> which defines both the set of entries to be 
 * considered, as well as the result type of the final query.  For example, if
 * the list of source entities contains only <code>Question.class</code>, the 
 * entries to be considered will be "all Questions" and the result type of the 
 * final query would be instances of <code>Question</code>.  If the list of 
 * entities was [<code>Question.class</code>, <code>Question.class</code>, 
 * <code>Category.class</code>], then the entries to be considered would be all
 * possible pairings of (i.e. the cartesian product over) questions with other
 * questions and categories.  In this case the result type of the final query
 * would be instances of <code>Object[]</code> with length 3, where the 0th 
 * element was the first <code>Question</code> of the pairing, the 1st
 * element was the second <code>Question</code> of the pairing, and the 2nd
 * element was the <code>Category</code> of the pairing.</p>
 * 
 * <p>The builder also keeps track of a set of <em>accessible entities</em>,
 * which define those entities from which data can be retrieved.  Each source
 * entity defines a unique accessible entity.  The set of accessible entities
 * can be further extended with calls to 
 * {@link #join(utils.CriteriaHelper.TableKey, java.lang.String) join()} which
 * uses a left join to add an entity linked to through some named 
 * attribute of some already-accessible entity.  Each accessible entity is
 * represented by a unique instance of <code>TableKey</code>, which are returned
 * by those methods that make a new entity accessible.  The keys of source 
 * entities are further refined into <code>SourceTableKey</code>s.</p>
 * 
 * <p>Finally, methods are provided for easily accessing data from any 
 * accessible entity.  Most commonly, this is in the form of a JPA 
 * <code>Expression</code> via a call to 
 * {@link #field(utils.CriteriaHelper.TableKey, java.lang.String) field()}, by
 * identifying the accessible entity from which the field should be returned
 * and the name of the field to return.  Additionally, where a JPA 
 * <code>Root</code> is required, 
 * {@link #getSourceRoot(utils.CriteriaHelper.RootTableKey) getSourceRoot()} may
 * be called with the <code>SourceTableKey</code> corresponding to a source
 * entity.</p>
 * 
 * <p>When the query has been set up, a final <code>CriteriaQuery</code> can be 
 * returned with a call to <code>finish()</code>.  Because the query is 
 * constructed piecemeal over the lifetime of a <code>CriteriaHelper</code>,
 * <code>finish()</code> may only be called once.  Further calls to 
 * <code>finish()</code> will result in an <code>IllegalStateException</code>.
 * Additionally, methods which could potentially modify the query, such as
 * {@link #addSourceEntity(java.lang.Class) addSourceEntity()} will also throw
 * an <code>IllegalStateException</code>.  Methods which only provide a handle
 * on data can continue to be used.</p>
 * 
 * <p>As no reference is maintained to the returned <code>CriteriaQuery</code>,
 * it is safe to modify the returned query after a call to 
 * <code>finish()</code>.</p>
 */
public class CriteriaHelper {
    private CriteriaBuilder myBuilder = JPA.em().getCriteriaBuilder();
    
    private CriteriaQuery myQuery = myBuilder.createQuery();
    
    private Map<TableKey, From> myPathMap = new HashMap<TableKey, From>();
    private Map<RootTableKey, Root> mySourcePathMap = 
            new HashMap<RootTableKey, Root>();
    private List<Root> mySources = new LinkedList<Root>();
    
    private final List<Expression<Boolean>> myAdditionalConditions = 
            new LinkedList<Expression<Boolean>>();

    private final List<Expression> myGroups = new LinkedList<Expression>();
    
    /**
     * <p>Returns the set of keys to all accessible entities.</p>
     * 
     * @return The set of keys to all accessible entities.
     */
    public Set<TableKey> getKeys() {
        return new HashSet<TableKey>(myPathMap.keySet());
    }
    
    /**
     * <p>Returns the set of keys to accessible entities of the given type.</p>
     * 
     * @return The set of keys to accessible entities of the given type.
     */
    public Set<TableKey> getKeys(Class c) {
        Set<TableKey> result = new HashSet<TableKey>();
        
        for (Map.Entry<RootTableKey, Root> entry : mySourcePathMap.entrySet()) {
            if (c.isAssignableFrom(entry.getValue().getJavaType())) {
                result.add(entry.getKey());
            }
        }
        
        return result;
    }
    
    /**
     * <p>Adds a new entity type to the <em>source list</em>.  This performs
     * a Cartesian product with the existing entry set and entities of the
     * new type.  The given entity becomes part of the <em>accessible set</em>,
     * and a corresponding <code>TableKey</code> is returned.
     * </p>
     * 
     * <p>Note that including the same entity twice or more is fine.  You could,
     * for example, construct a criteria query over all pairwise matchings of
     * questions.</p>
     * 
     * <p>Once {@link #finish() finish()} has been called, additional calls to
     * this method will result in an <code>IllegalStateException</code>.</p>
     * 
     * @param source The new entity type.  This class must have the @Entity
     *              annotation.
     * 
     * @return A key used to identify the newly included entity.
     */
    public RootTableKey addSourceEntity(Class source) {
        
        if (myQuery == null) {
            throw new IllegalStateException("Cannot modify the query after " +
                    "a call to finish().");
        }
        
        RootTableKey result = new RootTableKey();
        
        Root path = myQuery.from(source);
        mySources.add(path);
        myPathMap.put(result, path);
        mySourcePathMap.put(result, path);
        
        return result;
    }
    
    /**
     * <p>This method is a variation on 
     * {@link #getEntity(utils.CriteriaHelper.TableKey) getEntity()} that 
     * provides a typesafe way of retrieving a <code>Root</code> instead of a 
     * <code>From</code> for situations when we know the key in question 
     * identifies a <em>source entity</em>.</p>
     * 
     * <p>Returns a JPA <code>Root</code> corresponding to the given
     * <code>RootTableKey</code>.  Useful for constructing predicates or 
     * exposing table columns.</p>
     * 
     * @param entityKey The key representing the entity to which the 
     *              returned <code>Root</code> should correspond.
     * @return The <code>Root</code> corresponding to <code>entityKey</code>.
     */
    public Root getSourceRoot(RootTableKey entityKey) {
        if (!mySourcePathMap.containsKey(entityKey)) {
            throw new IllegalArgumentException("No such source.");
        }
        
        return mySourcePathMap.get(entityKey);
    }
    
    /**
     * <p>Returns a JPA <code>From</code> corresponding to the given
     * <code>TableKey</code>.  Useful for constructing predicates or 
     * exposing table columns.</p>
     * 
     * @param entityKey The key representing the entity to which the 
     *              returned <code>From</code> should correspond.
     * @return The <code>From</code> corresponding to <code>entityKey</code>.
     */
    public From getEntity(TableKey entityKey) {
        if (!myPathMap.containsKey(entityKey)) {
            throw new IllegalArgumentException("No such table.");
        }
        
        return myPathMap.get(entityKey);
    }
    
    /**
     * <p>"Follows" a field on an accessible entity, left-joining the 
     * information from the identified field into the existing entities of this 
     * query and making the target of the identified field accessible, returning
     * a corresponding key.</p>
     * 
     * @param from The existing accessible entity from which to follow a field.
     * @param on The name of the field to follow.
     * 
     * @return A <code>TableKey</code> identifying the newly-accessible entity.
     */
    public TableKey join(TableKey from, String on) {
        return join(from, JoinType.LEFT, on);
    }

    public TableKey join(TableKey from, JoinType type, String on) {

        if (!myPathMap.containsKey(from)) {
            throw new IllegalArgumentException("No such table.");
        }

        From f = myPathMap.get(from);

        Join j = f.join(on, type);

        TableKey result = new TableKey();

        myPathMap.put(result, j);

        return result;
    }

    public Subquery<?> createSubquery() {
        return myQuery.subquery(Object.class);
    }

    /**
     * <p>Adds a conjunct to be added to the <code>where</code> clause of the
     * final query.  I.e., the given condition will be "and"ed with all previous
     * conditions.</p>
     * 
     * <p>Once {@link #finish() finish()} has been called, additional calls to
     * this method will result in an <code>IllegalStateException</code>.</p>
     * 
     * @param condition The conjunct to be added.
     */
    public void addCondition(Expression<Boolean> condition) {
        if (myQuery == null) {
            throw new IllegalStateException("Cannot modify the query after " +
                    "a call to finish().");
        }

        myAdditionalConditions.add(condition);
    }

    public void addGroup(Expression group) {
        if(myQuery == null) {
            throw new IllegalStateException("Cannot modify the query after " +
                    "a call to finish().");
        }

        myGroups.add(group);
    }

    /**
     * <p>Returns <code>true</code> <strong>iff</strong> 
     * {@link #finish() finish()} has already been called.</p>
     * 
     * @return <code>true</code> <strong>iff</strong> {@link #finish() finish()}
     *         has already been called.
     */
    public boolean isClosed() {
        return (myQuery == null);
    }
    
    /**
     * <p>A helper method to return a field from an accessible entity as a JPA 
     * <code>Expression</code>.</p>
     * 
     * <p>This is functionally equivalent to:</p>
     * 
     * <pre>
     * getEntity(from).get(fieldName)
     * </pre>
     * 
     * @param <T> The expected type of the field.
     * @param from The key associated with the accessible entity from which to
     *              draw the field.
     * @param fieldName The field name.
     * 
     * @return An <code>Expression</code> that corresponds to the named 
     *         attribute.
     */
    public <T> Expression<T> field(TableKey from, String fieldName) {
        
        if (!myPathMap.containsKey(from)) {
            throw new IllegalArgumentException("No such table.");
        }
        
        return myPathMap.get(from).get(fieldName);
    }
    
    /**
     * <p>A helper method to return entries from a <code>CriteriaQuery</code>.
     * Returns <code>runLength</code> matched entries as a <code>List</code>,
     * starting with <code>startIndex</code>, caching the query 
     * <strong>iff</strong> <code>cache</code> is true.</p>
     * 
     * @param query The query from which to return the entries.
     * @param startIndex The first entry to return.
     * @param runLength The number of entries to return.
     * @param cache Whether or not to set the "cacheable" hint.
     * 
     * @return The list of matched entries from <code>startIndex</code> to
     *         <code>(startIndex + runLength - 1)</code>.
     */
    public static List getResultList(CriteriaQuery query, int startIndex, 
            int runLength, boolean cache) {
        EntityManager manager = JPA.em();
        TypedQuery finalQuery = manager.createQuery(query);
        finalQuery.setFirstResult(startIndex);
        finalQuery.setMaxResults(runLength);
        
        if (cache) {
            finalQuery.setHint("org.hibernate.cacheable", true);
        }
        
        return finalQuery.getResultList();
    }
    
    /**
     * <pA helper method to return entries from a <code>CriteriaQuery</code>.
     * The query will be cached.</p>
     * 
     * @param query The query from which to return the entries.
     * 
     * @return The list of matched entries from <code>startIndex</code> to
     *         <code>(startIndex + runLength - 1)</code>.
     */
    public static List getResultList(CriteriaQuery query) {
        return getResultList(query, 0, Integer.MAX_VALUE, true);
    }
    
    /**
     * <p>Helper method to return the number of entries that would be returned
     * by a <code>CriteriaQuery</code>.  Note that this method may arbitrarily 
     * modify the query, so the query should not be used after passing it to 
     * this method.</p>
     * 
     * @param query The query from which to return the count.
     * 
     * @return The entry count.
     */
    public static long count(CriteriaQuery query) {        
        EntityManager manager = JPA.em();
        CriteriaBuilder builder = manager.getCriteriaBuilder();
        
        long result;
        
        Set<Root> rootSet = (Set<Root>) query.getRoots();

        if (rootSet.isEmpty()) {
            //It's entirely unclear how to select nothing in JPA, so we:
            result = 0;
        }
        else {
            //If there are multiple roots, it shouldn't matter which we
            //choose, since we're not selecting DISTINCT elements
            //TODO : This is not tested, however, since all our queries 
            //       currently contain only one root
            Root root = rootSet.iterator().next();
            query.select(builder.count(root));

            //Defeats a bug in H2 that causes counting to fail if there's an
            //order set.  See: http://jira.grails.org/browse/GRAILS-8162
            query.orderBy();

            /** TODO : Counting rows with groups requires a subquery, which is messy in
             JPA. This is a little hacky, but it getes the job done. **/
            List<Long> counts = manager.createQuery(query).getResultList();
            if(query.getGroupList().isEmpty()) {
                result = counts.get(0);
            }
            else {
                result = counts.size();
            }
        }
        
        return result;
    }
    
    /**
     * <p>Returns <code>runLength</code> matched entries as a <code>List</code>,
     * starting with <code>startIndex</code>, caching the query 
     * <strong>iff</strong> <code>cache</code> is true.  Note that calling this 
     * defers to {@link #finish() finish()}, so this must be the last call to 
     * this helper.</p>
     * 
     * <p>Equivalent to: 
     * <code>getResultList(finish(), startIndex, runLength, cache)</code>.</p>
     * 
     * @param startIndex The first entry to return.
     * @param runLength The number of entries to return.
     * @param cache Whether or not to set the "cacheable" hint.
     * 
     * @return The list of matched entries from <code>startIndex</code> to
     *         <code>(startIndex + runLength - 1)</code>.
     */
    public List getResultList(int startIndex, int runLength, boolean cache) {
        return getResultList(finish(), startIndex, runLength, cache);
    }
    
    /**
     * <p>Returns matched entries as a <code>List</code>, caching the query in
     * the process.  Note that calling this defers to 
     * {@link #finish() finish()}, so this must be the last call to this 
     * helper.</p>
     * 
     * <p>Equivalent to: <code>getResultList(finish())</code>.</p>
     * 
     * @return The matched entries.
     */
    public List getResultList() {
        return getResultList(0, Integer.MAX_VALUE, true);
    }
    
    /**
     * <p>Returns the number of entries that would be returned by this query.
     * Note that calling this defers to {@link #finish() finish()}, so this
     * must be the last call to this helper.</p>
     * 
     * <p>Equivalent to: <code>count(finish())</code>.</p>
     * 
     * @return The entry count.
     */
    public long count() {
        return count(finish());
    }
    
    /**
     * <p>Finalizes the build process, returning the constructed 
     * <code>CriteriaQuery</code>.  This method may only be called once.  
     * Future attempts to call it will result in an 
     * <code>IllegalStateException</code>.</p>
     * 
     * <p>The <code>select</code> clause of the resulting query will be either
     * <code>null</code>, if no <em>source entities</em> have been added, the 
     * root of the lone <em>source entity</em>, if only one source has been
     * added, or a <code>CompoundSelection</code> based on each source 
     * otherwise.</p>
     * 
     * @return The constructed <code>CriteriaQuery</code>.
     */
    public CriteriaQuery finish() {
        if (myQuery == null) {
            throw new IllegalStateException("Must call finish() only once.");
        }
        
        Selection select;
        switch(mySources.size()) {
            case 0:
                select = null;
                break;
            case 1:
                select = mySources.get(0);
                break;
            default:
                select = myBuilder.array(mySources.toArray(new Root[0]));
        }
        myQuery.select(select);
        
        Expression<Boolean> where = myBuilder.conjunction();
        for (Expression<Boolean> condition : myAdditionalConditions) {
            where = myBuilder.and(where, condition);
        }
        myQuery.where(where);

        myQuery.groupBy(myGroups.toArray(new Expression[0]));

        CriteriaQuery result = myQuery;
        
        myQuery = null;
        
        return result;
    }
    
    /**
     * <p>Instances of this class simply serve as type-safe tokens for accessing
     * different paths into the underlying criteria query.</p>
     */
    public static class TableKey {
        
    }
    
    public static class RootTableKey extends TableKey {
        
    }
}
