package models.tables;

import java.util.List;
import java.util.Map;

/**
 * <p>Sliceable, sortable table data.</p>
 */
public interface Paginator {
    
    public List<Map<Object, Object>> view(Object sortColumn, 
            boolean sortAscending, int startIndex, int runLength);
    
    public long getTotalEntryCount();
}
