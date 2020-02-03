package models.filter.activeinterview;

import models.ActiveInterview;
import models.Candidate;
import play.db.jpa.JPA;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

public class ByInterviewee extends ActiveInterviewFilter<Long> {

    public Predicate asPredicate(Root<ActiveInterview> root) {

        CriteriaBuilder criteriaBuilder = JPA.em().getCriteriaBuilder();
        Predicate criteria = (include.size() == 0) ? criteriaBuilder.conjunction() : criteriaBuilder.disjunction();


        for (Long id : include) {
            Candidate candidate = Candidate.findById(id);
            if (candidate != null) {
                criteria = criteriaBuilder.or(criteria, criteriaBuilder.equal(root.<Candidate>get("interviewee"), candidate));
            }
        }

        for (Long id : exclude) {
            Candidate candidate = Candidate.findById(id);
            if (candidate != null) {
                criteria = criteriaBuilder.and(criteria,
                        criteriaBuilder.not(
                                criteriaBuilder.equal(root.<Candidate>get("interviewee"), candidate)));

            }
        }

        return criteria;
    }

    public boolean willAccept(ActiveInterview activeInterview) {
        if ((activeInterview != null) && (activeInterview.interviewee != null)) {

            if (!include.contains(activeInterview.interviewee.id)) {
                return false;
            }
            if (exclude.contains(activeInterview.interviewee.id)) {
                return false;
            }

            return true;
        }
        return false;
    }

    @Override
    public String getAttributeName() {
        return "interviewee";
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
