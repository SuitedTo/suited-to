package models.filter.userBadge;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import models.Company;
import models.User;
import models.UserBadge;
import play.db.jpa.JPA;

public class ByUser extends UserBadgeFilter<Long>{

    public Predicate asPredicate(Root<UserBadge> root) {

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

	@Override
	public String getAttributeName() {
		return "user";
	}

	@Override
	public Long fromString(String idStr) {
		return Long.parseLong(idStr);
	}

	@Override
	protected String toString(Long id) {
		return String.valueOf(id);
	}
}
