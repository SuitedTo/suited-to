
package models.tables;

import java.util.*;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import models.tables.CriteriaQueryInfoExposer.ColumnSynthesisEntry;
import play.db.jpa.JPA;
import utils.CriteriaHelper;
import utils.ObjectTransformer;
import utils.Transformer;

/**
 * <p>A {@link Paginator Paginator} backed by a <code>CriteriaQuery</code>.</p>
 */
public class CriteriaQueryPaginator implements Paginator {
    
    private Transformer <Map<Object, Object>> resultTransformer;
    private final CriteriaQuery myBaseQuery;
    private final HashMap<Object, ColumnSynthesisEntry> mySelectedExpressions;
    
    public CriteriaQueryPaginator(CriteriaQuery q, 
                Set<Expression> selectedExpressions) {
        myBaseQuery = q;
        
        mySelectedExpressions = new HashMap<Object, ColumnSynthesisEntry>();
        for (Expression p : selectedExpressions) {
            mySelectedExpressions.put(p, 
                    new ColumnSynthesisEntry(p, 
                    ObjectTransformer.DummyTransformer.INSTANCE));
        }
    }
    
    public CriteriaQueryPaginator(CriteriaQuery q, 
            Map<Object, ColumnSynthesisEntry> selectedExpressions) {
        myBaseQuery = q;
        
        mySelectedExpressions = new HashMap<Object, ColumnSynthesisEntry>();
        mySelectedExpressions.putAll(selectedExpressions);
    }
    
    public CriteriaQueryPaginator setResultTransformer(Transformer <Map<Object, Object>> resultTransformer){
    	this.resultTransformer = resultTransformer;
    	return this;
    }
    
    @Override
    public List<Map<Object, Object>> view(Object sortColumn, 
                boolean sortAscending, int startIndex, int runLength) {
        
        EntityManager manager = JPA.em();
        CriteriaBuilder builder = manager.getCriteriaBuilder();
        
        if (sortColumn != null) {
            myBaseQuery.orderBy(
                    getOrder(mySelectedExpressions.get(sortColumn).source, 
                    sortAscending, manager));
        }
        
        //Note that the order here is undefined--which is fine, just so long as
        //we're consistent
        Object[] columnNames =
                mySelectedExpressions.keySet().toArray();
        
        Expression[] columnSources = new Expression[columnNames.length];
        for (int i = 0; i < columnSources.length; i++) {
            columnSources[i] = mySelectedExpressions.get(columnNames[i]).source;
        }
        
        myBaseQuery.select(builder.array(columnSources));
        
        List<Object[]> queryResults = CriteriaHelper.getResultList(
                myBaseQuery, startIndex, runLength, true);
        
        List<Map<Object, Object>> result = 
                new ArrayList<Map<Object, Object>>(queryResults.size());
        
        Map<Object, Object> resultMap;
        
        List<Object> columnsList = Arrays.asList(columnNames);
        
        for (Object[] queryResult : queryResults) {
            
            resultMap = new HashMap<Object, Object>();
            
            //Note that these lists are guaranteed to be the same length
            Iterator<Object> columnIter = columnsList.iterator();
            Iterator<Object> resultsIter = 
                    Arrays.asList(queryResult).iterator();
            while (columnIter.hasNext()) {
                Object key = columnIter.next();
                Object datum = resultsIter.next();
                resultMap.put(key, 
                        mySelectedExpressions.get(key)
                        .transformer.transform(datum));
            }
            
            if(resultTransformer != null){
            	resultMap = resultTransformer.transform(resultMap);
            }
            result.add(resultMap);
        }
        
        return result;
    }
 
    /**
     * Note that this must be called last, as it may arbitrarily modify the
     * query upon which this paginator is based.
     */
    public long getTotalEntryCount() {
        return CriteriaHelper.count(myBaseQuery);
    }
    
    private Order getOrder(Expression column, boolean ascending, 
                EntityManager manager) {
        Order result;
        
        CriteriaBuilder builder = manager.getCriteriaBuilder();
        
        if (ascending) {
            result = builder.asc(column);
        }
        else {
            result = builder.desc(column);
        }
        
        return result;
    }
}
