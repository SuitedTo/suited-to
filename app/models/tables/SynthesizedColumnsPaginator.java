
package models.tables;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * <p>Decorates an existing {@link Paginator Paginator}, synthesizing new 
 * columns from the columns of the base paginator.</p>
 */
public class SynthesizedColumnsPaginator implements Paginator {

    private final Paginator mySourcePaginator;
    
    private final Map<Object, Synthesizer> mySynthesizers = 
            new HashMap<Object, Synthesizer>();
    
    public SynthesizedColumnsPaginator(Paginator sourcePaginator) {
        mySourcePaginator = sourcePaginator;
    }
    
    /**
     * <p>Creates a new synthesized column called <code>columnName</code>,
     * whose value if synthesized by <code>value</code>.</p>
     * 
     * @param columnName The name of the synthesized column.
     * @param value The value synthesizer of the column.
     */
    public void addSynthesizedColumn(Object columnName, Synthesizer value) {
        if (mySynthesizers.containsKey(columnName)) {
            throw new IllegalArgumentException("Duplicate column.");
        }
        
        mySynthesizers.put(columnName, value);
    }
    
    public List<Map<Object, Object>> view(Object sortColumn, 
            boolean sortAscending, int startIndex, int runLength) {
        
        if (mySynthesizers.containsKey(sortColumn)) {
            throw new IllegalArgumentException("Cannot sort on a " +
                    "synthesized column.");
        }
        
        List<Map<Object, Object>> sourceResults = mySourcePaginator.view(
                sortColumn, sortAscending, startIndex, runLength);
        
        List<Map<Object, Object>> finalResults = 
                new LinkedList<Map<Object, Object>>();
        
        Map<Object, Object> finalEntry;
        for (Map<Object, Object> sourceEntry : sourceResults) {
            
            finalEntry = new HashMap<Object, Object>(sourceEntry);
            
            for (Map.Entry<Object, Synthesizer> synthesizer : 
                        mySynthesizers.entrySet()) {
                
                finalEntry.put(synthesizer.getKey(), 
                        synthesizer.getValue().synthesizeValue(sourceEntry));
            }
            
            finalResults.add(finalEntry);
        }
        
        return finalResults;
    }

    public long getTotalEntryCount() {
        return mySourcePaginator.getTotalEntryCount();
    }
    
    /**
     * <p>Accepts row data from a table, stored as a map from column names to
     * entry values, and synthesizes the value of a new column.</p>
     */
    public static interface Synthesizer {
        public Object synthesizeValue(Map<Object, Object> sourceResults);
    }
}
