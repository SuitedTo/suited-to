package models.query.category;

import controllers.Security;
import enums.RoleValue;
import models.Category;
import models.Category.CategoryStatus;
import models.Company;
import models.User;

import javax.persistence.criteria.*;

/**
 * Query for categories that the given user is allowed to access
 *
 * @author joel
 */
public class AccessibleCategories extends CategoryQuery {
    private User user;

    public AccessibleCategories() {
        super();
        this.user = Security.connectedUser();
    }

    public AccessibleCategories(User user, Integer iSortCol_0, String sSortDir_0, String sSearch,
                                Integer iDisplayStart, Integer iDisplayLength) {
        super(iSortCol_0, sSortDir_0, sSearch, iDisplayStart, iDisplayLength);

        if (user == null) {
            this.user = Security.connectedUser();
        } else {
            this.user = user;
        }
    }

    @Override
    public Predicate buildCriteria(CriteriaBuilder criteriaBuilder, Root<Category> root,
                                   CriteriaQuery<Object> criteriaQuery) {

        if (user.hasRole(RoleValue.APP_ADMIN)) {
            return criteriaBuilder.conjunction();
        }


        final Path<CategoryStatus> status = root.get("status");
        final Path<User> creator = root.get("creator");

        //status is public
        Predicate criteria = criteriaBuilder.equal(status, CategoryStatus.PUBLIC);

        //or status is beta
        criteria = criteriaBuilder.or(criteria, criteriaBuilder.equal(status, CategoryStatus.BETA));

        //or status is null
        criteria = criteriaBuilder.or(criteria, criteriaBuilder.isNull(status));

        Company connectedUserCompany = user.company;
        if (connectedUserCompany != null) {
            Predicate newStatus = criteriaBuilder.equal(status, CategoryStatus.NEW);
            Predicate privateStatus = criteriaBuilder.equal(status, CategoryStatus.PRIVATE);
            Subquery<User> subquery = criteriaQuery.subquery(User.class);
            Root fromUser = subquery.from(User.class);
            subquery.select(fromUser).where(criteriaBuilder.equal(fromUser.get("company"), connectedUserCompany));

            //OR ((status is new) AND (category.creator in user.coworkers))
            criteria = criteriaBuilder.or(criteria, criteriaBuilder.and(newStatus, criteriaBuilder.in(creator).value(subquery)));

            //OR ((status is private) AND (category.creator in user.coworkers))
            criteria = criteriaBuilder.or(criteria, criteriaBuilder.and(privateStatus, criteriaBuilder.in(creator).value(subquery)));
        }


        return criteria;
    }

}
