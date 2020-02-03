package models.filter.activeinterview;

import models.ActiveInterview;
import models.User;
import play.db.jpa.JPA;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

public class ByUser extends ActiveInterviewFilter<Long> {
    public Predicate asPredicate(Root<ActiveInterview> root) {

        CriteriaBuilder criteriaBuilder = JPA.em().getCriteriaBuilder();
        Predicate criteria = (include.size() == 0) ? criteriaBuilder.conjunction() : criteriaBuilder.disjunction();


        for (Long id : include) {
            User user = User.findById(id);
            if (user != null) {
                criteria = criteriaBuilder.or(criteria, criteriaBuilder.equal(root.<User>get("user"), user));
            }
        }

        for (Long id : exclude) {
            User user = User.findById(id);
            if (user != null) {
                criteria = criteriaBuilder.and(criteria,
                        criteriaBuilder.not(
                                criteriaBuilder.equal(root.<User>get("user"), user)));

            }
        }

        return criteria;
    }

    public boolean willAccept(ActiveInterview activeInterview) {
        if ((activeInterview != null) && (activeInterview.user != null)) {

            if (!include.contains(activeInterview.user.id)) {
                return false;
            }
            if (exclude.contains(activeInterview.user.id)) {
                return false;
            }

            return true;
        }
        return false;
    }

    @Override
    public String getAttributeName() {
        return "user";
    }

    @Override
    protected String toString(Long id) {
        return String.valueOf(id);
    }

    @Override
    public Long fromString(String idStr) {
        return Long.parseLong(idStr);
    }
}
