package models.query.user;

import models.User;
import models.query.QueryBase;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * Base class for User queries.
 *
 * @author joel
 */
public abstract class UserQuery extends QueryBase<User> {

    public UserQuery() {
        super();
    }

    public UserQuery(Integer iSortCol_0, String sSortDir_0, String sSearch,
                         Integer iDisplayStart, Integer iDisplayLength) {
        super(iSortCol_0, sSortDir_0, sSearch, iDisplayStart, iDisplayLength);
    }

    public static final UserQuery getDefaultQuery() {
        return new AccessibleUsers();
    }

    public String[] getSortableColumnNames() {
        return new String[]{"email", "fullName", "status", "created"};
    }

    public Class<User> getEntityClass() {
        return User.class;
    }

    public abstract Predicate buildCriteria(CriteriaBuilder criteriaBuilder, Root<User> root,
                                            CriteriaQuery<Object> criteriaQuery);

}
