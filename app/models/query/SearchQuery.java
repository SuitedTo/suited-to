package models.query;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import play.Play;
import play.db.jpa.JPA;

import models.ModelBase;
import models.filter.EntityAttributeFilter;
import models.query.QueryBase;
import models.query.QueryFilterListBinder;
import models.query.QueryResult;
import models.query.question.QuestionQuery;

/**
 * Wrapper to decorate query classes with search functionality.
 *
 * @author joel
 */
public final class SearchQuery<M extends ModelBase> extends QueryBase<M> {
    private final String DELIMITER = ",";

    private final QueryBase<M> query;
    private String searchString;
    private final List<SearchFilter> filters;

    public SearchQuery(QueryBase<M> query, String searchString) {
        this.query = query;
        this.searchString = searchString;
        filters = new ArrayList<SearchFilter>();
    }

    public <T> SearchQuery<M> addFilter(EntityAttributeFilter<M, T> filter) {
        return addFilter(filter, false);
    }

    public <T> SearchQuery<M> addFilter(EntityAttributeFilter<M, T> filter, boolean interpret) {
        filters.add(new SearchFilter<T>(filter, interpret));
        return this;
    }

    private List<EntityAttributeFilter> collectApplicableFilters(String string) {

        List<EntityAttributeFilter> list = new ArrayList<EntityAttributeFilter>();

        for (SearchFilter sf : filters) {
            EntityAttributeFilter filter = sf.filter;
            boolean interpret = sf.interpret;

            String include = new StringBuilder(interpret ? "$" : "").append(string).toString();
            filter.clear();
            filter.include(include);
            //Changing the include method to return true or false makes
            //Play's action invoker angry somehow so check size
            if (filter.getIncludes().size() == 1) {
                list.add(filter);
            }
        }
        return list;
    }

    @Override
    public String[] getSortableColumnNames() {
        return query.getSortableColumnNames();
    }

    @Override
    public Class<M> getEntityClass() {
        return query.getEntityClass();
    }

    @Override
    public Predicate buildCriteria(CriteriaBuilder criteriaBuilder, Root<M> root, CriteriaQuery<Object> criteriaQuery) {

        Predicate criteria = query.buildCriteria(criteriaBuilder, root, criteriaQuery);
        if ((searchString != null) && (searchString.length() != 0)) {
            String[] tokens = searchString.split(DELIMITER);
            for (int i = 0; i < tokens.length; ++i) {
                String searchItem = tokens[i].trim();
                if (searchItem.length() > 0) {
                    Predicate applicableCriteria = criteriaBuilder.disjunction();
                    List<EntityAttributeFilter> searchFilters = collectApplicableFilters(searchItem);
                    if ((searchFilters != null) && (searchFilters.size() > 0)) {
                        for (EntityAttributeFilter filter : searchFilters) {
                            applicableCriteria = criteriaBuilder.or(applicableCriteria, filter.asPredicate(root));
                        }
                        criteria = criteriaBuilder.and(criteria, applicableCriteria);
                    }
                }
            }
        }
        return criteria;
    }

    private class SearchFilter<T> {
        boolean interpret;
        EntityAttributeFilter<M, T> filter;

        public SearchFilter(EntityAttributeFilter<M, T> filter, boolean interpret) {
            this.interpret = interpret;
            this.filter = filter;
        }
    }

}
