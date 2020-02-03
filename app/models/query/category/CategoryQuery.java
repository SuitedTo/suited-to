package models.query.category;

import models.Category;
import models.query.QueryBase;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * Base class for Category queries.
 *
 * @author joel
 */
public abstract class CategoryQuery extends QueryBase<Category> {

    public CategoryQuery() {
        super();
    }

    public CategoryQuery(Integer iSortCol_0, String sSortDir_0, String sSearch,
                         Integer iDisplayStart, Integer iDisplayLength) {
        super(iSortCol_0, sSortDir_0, sSearch, iDisplayStart, iDisplayLength);
    }

    public static final CategoryQuery getDefaultQuery() {
        return new AccessibleCategories();
    }

    public String[] getSortableColumnNames() {
        return new String[]{"id", "name", "created", "questionCount", "companyName", "isPrep", "isPrepSearchable", "status"};
    }

    public Class<Category> getEntityClass() {
        return Category.class;
    }

    public abstract Predicate buildCriteria(CriteriaBuilder criteriaBuilder, Root<Category> root,
                                            CriteriaQuery<Object> criteriaQuery);

}
