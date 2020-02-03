
package models.tables;

import java.util.List;
import models.filter.Filter;

public interface Filterable {
    public void addFilters(List<Filter> filters);
}
