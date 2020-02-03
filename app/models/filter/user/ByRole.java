package models.filter.user;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import enums.RoleValue;
import models.User;

import play.db.jpa.JPA;
import play.i18n.Messages;

public class ByRole extends UserFilter<RoleValue> {

    public Predicate asPredicate(Root<User> root) {

        CriteriaBuilder criteriaBuilder = JPA.em().getCriteriaBuilder();
        Predicate criteria = (include.size() == 0) ? criteriaBuilder.conjunction() : criteriaBuilder.disjunction();

        Join roles = root.join("roles");
        for (RoleValue role : include) {
                criteria = criteriaBuilder.or(criteria, criteriaBuilder.in(roles).value(role));
        }

        for (RoleValue role : exclude) {
                criteria = criteriaBuilder.and(criteria,
                        criteriaBuilder.not(
                        		criteriaBuilder.in(roles).value(role)));
        }

        return criteria;
    }

    public boolean willAccept(RoleValue role) {
        if (role != null) {

            if (!include.contains(role)) {
                return false;
            }
            if (exclude.contains(role)) {
                return false;
            }

            return true;
        }
        return false;
    }

    @Override
    public String getAttributeName() {
        return "roles";
    }

    @Override
    protected String toString(RoleValue role) {
        return role.name();
    }

    @Override
    public RoleValue fromString(String roleStr) {
        return RoleValue.valueOf(roleStr);
    }

}

