package models.query.userBadge;

import javax.persistence.criteria.*;

import models.User;
import models.UserBadge;
import controllers.Security;
import enums.RoleValue;

/**
 * Query for badges belonging to users who are community members
 *
 * @author joel
 */
public class AccessibleBadges extends UserBadgeQuery {

    public AccessibleBadges() {
        super();
    }

    public AccessibleBadges(Integer iSortCol_0, String sSortDir_0, String sSearch,
                               Integer iDisplayStart, Integer iDisplayLength) {
        super(iSortCol_0, sSortDir_0, sSearch, iDisplayStart, iDisplayLength);
    }

    @Override
    public Predicate buildCriteria(CriteriaBuilder criteriaBuilder, Root<UserBadge> root,
                                   CriteriaQuery<Object> criteriaQuery) {

        Path user = root.join("user");
        Predicate result = criteriaBuilder.conjunction();
        result = criteriaBuilder.and(result, criteriaBuilder.isNotNull(user.get("displayName")));
        result = criteriaBuilder.and(result, criteriaBuilder.isTrue(user.get("badgesPublic")));
        result = criteriaBuilder.and(result, criteriaBuilder.isFalse(user.get("privacyLockdown")));

        return result;
    }

}
