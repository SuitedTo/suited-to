package models.query.userBadge;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import models.UserBadge;
import models.query.QueryBase;

/**
 * Base class for UserBadge queries.
 *
 * @author joel
 */
public abstract class UserBadgeQuery extends QueryBase<UserBadge> {

    public UserBadgeQuery() {
        super();
    }

    public UserBadgeQuery(Integer iSortCol_0, String sSortDir_0, String sSearch,
                         Integer iDisplayStart, Integer iDisplayLength) {
        super(iSortCol_0, sSortDir_0, sSearch, iDisplayStart, iDisplayLength);
    }

    public static final UserBadgeQuery getDefaultQuery() {
        return new AccessibleBadges();
    }

    public String[] getSortableColumnNames() {
        return new String[]{"id", "name", "multiplier", "progress", "earned"};
    }

    public Class<UserBadge> getEntityClass() {
        return UserBadge.class;
    }

    public abstract Predicate buildCriteria(CriteriaBuilder criteriaBuilder, Root<UserBadge> root,
                                            CriteriaQuery<Object> criteriaQuery);

}
