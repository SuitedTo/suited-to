package models.query;

import java.util.List;

import play.db.jpa.GenericModel;
import play.db.jpa.Model;

/**
 * Query results
 *
 * @param <T>
 * @author joel
 */
public class QueryResult<M extends GenericModel> {

    private long total;
    private List<M> list;

    /**
     * @param total The filtered total
     * @param list  Result list
     */
    public QueryResult(long total, List<M> list) {
        this.total = total;
        this.list = list;
    }

    public boolean add(M element) {
        if ((element != null) && !list.contains(element)) {
            list.add(element);
            ++total;
            return true;
        }
        return false;
    }

    public boolean remove(M element) {
        if ((element != null) && list.remove(element)) {
            --total;
            return true;
        }
        return false;
    }

    public void addAll(List<M> elements) {
        if (elements != null) {
            int originalSize = list.size();
            list.addAll(elements);
            total += (list.size() - originalSize);
        }
    }

    public void retainAll(List<M> elements) {
        if (elements != null) {
            int originalSize = list.size();
            list.retainAll(elements);
            total -= (originalSize - list.size());
        }
    }

    public long getTotal() {
        return total;
    }

    public List<M> getList() {
        return list;
    }

}
