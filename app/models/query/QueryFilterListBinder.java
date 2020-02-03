package models.query;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import models.ModelBase;
import models.filter.EntityAttributeFilter;

/**
 * Binds a list of filters to a query.
 *
 * @author joel
 */
public class QueryFilterListBinder<M extends ModelBase, F extends EntityAttributeFilter> extends QueryBase<M> {
    private QueryBase<M> query;
    private List<F> filters;

    public QueryFilterListBinder(QueryBase<M> query, List<F> filters) {
        this.filters = mergeFilters(filters);
        this.query = query;
    }

    /**
     * Combines all instances of each type of filter into one instance by
     * pulling in all of the different include and exclude lists.
     * <p/>
     * We merge filters for two reasons:
     * 1) We don't want duplicate QuestionSet objects all over the place
     * for each different combination of includes and excludes for one type
     * of filter.
     * 2) If two different instances of the same filter come in with different
     * includes then a developer mistake has been made - this corrects the
     * mistake and logs it as an error.
     *
     * @param filters
     * @return
     */
    private List<F> mergeFilters(List<F> filters) {
        List<F> results = new ArrayList<F>();
        if (filters != null) {
            Hashtable<String, F> instances = new Hashtable<String, F>();
            for (F filter : filters) {
                String className = filter.getClass().getName();
                F currentInstance = instances.get(className);
                if (currentInstance == null) {
                    currentInstance = filter;
                    instances.put(className, filter);
                } else {
                    List<String> excludes = filter.getExcludes();
                    for (String exclude : excludes) {
                        //filter prevents duplicates
                        currentInstance.exclude(exclude);
                    }

                    List<String> includes = filter.getIncludes();
                    for (String include : includes) {
                        //filter prevents duplicates
                        currentInstance.include(include);
                    }
                }

            }

            Enumeration<F> elements = instances.elements();
            while (elements.hasMoreElements()) {
                results.add(elements.nextElement());
            }
        }
        return results;
    }

    @Override
    public Predicate buildCriteria(CriteriaBuilder criteriaBuilder,
                                   Root<M> root, CriteriaQuery<Object> criteriaQuery) {
        Predicate criteria = query.buildCriteria(criteriaBuilder, root, criteriaQuery);
        for (F filter : filters) {
            criteria = criteriaBuilder.and(criteria, filter.asPredicate(root));
        }
        return criteria;
    }

    @Override
    public String[] getSortableColumnNames() {
        return query.getSortableColumnNames();
    }

    @Override
    public Class<M> getEntityClass() {
        return query.getEntityClass();
    }

}
