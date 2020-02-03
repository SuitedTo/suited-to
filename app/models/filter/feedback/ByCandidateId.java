package models.filter.feedback;

import models.Feedback;
import play.db.jpa.JPA;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;

public class ByCandidateId extends FeedbackFilter<Long> {

    public Predicate asPredicate(Root<Feedback> root) {

        CriteriaBuilder criteriaBuilder = JPA.em().getCriteriaBuilder();
        Predicate criteria = (include.size() == 0) ? criteriaBuilder.conjunction() : criteriaBuilder.disjunction();

        for (Long  id : include) {
            criteria = criteriaBuilder.or(criteria, criteriaBuilder.equal(root.get("candidate").get("id"), id));
        }

        for (Long id : exclude) {
            criteria = criteriaBuilder.and(criteria, criteriaBuilder.notEqual(root.get("candidate").get("id"), id));
        }

        return criteria;
    }

    @Override
    public String getAttributeName() {
        return "id";
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
