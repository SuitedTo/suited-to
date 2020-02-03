package models.query.interview;

import models.Interview;
import models.query.QueryBase;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * Base class for Interview queries.
 *
 * @author joel
 */
public abstract class InterviewQuery extends QueryBase<Interview> {

    public InterviewQuery() {
        super();
    }

    public InterviewQuery(Integer iSortCol_0, String sSortDir_0, String sSearch,
                          Integer iDisplayStart, Integer iDisplayLength) {
        super(iSortCol_0, sSortDir_0, sSearch, iDisplayStart, iDisplayLength);
    }

    public static final InterviewQuery getDefaultQuery() {
        return new AccessibleInterviews();
    }

    public String[] getSortableColumnNames() {
        return new String[]{"id", "name", "created"};
    }

    public Class<Interview> getEntityClass() {
        return Interview.class;
    }

    public abstract Predicate buildCriteria(CriteriaBuilder criteriaBuilder, Root<Interview> root,
                                            CriteriaQuery<Object> criteriaQuery);

}
