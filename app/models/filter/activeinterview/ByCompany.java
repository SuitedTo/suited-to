package models.filter.activeinterview;

import models.ActiveInterview;
import models.Company;
import play.db.jpa.JPA;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

public class ByCompany extends ActiveInterviewFilter<Long> {

    public Predicate asPredicate(Root<ActiveInterview> root) {

        CriteriaBuilder criteriaBuilder = JPA.em().getCriteriaBuilder();
        Predicate criteria = (include.size() == 0) ? criteriaBuilder.conjunction() : criteriaBuilder.disjunction();


        for (Long id : include) {
            Company company = Company.findById(id);
            if (company != null) {
                criteria = criteriaBuilder.or(criteria, criteriaBuilder.equal(root.<Company>get("company"), company));
            }
        }

        for (Long id : exclude) {
            Company company = Company.findById(id);
            if (company != null) {
                criteria = criteriaBuilder.and(criteria,
                        criteriaBuilder.not(
                                criteriaBuilder.equal(root.<Company>get("company"), company)));

            }
        }

        return criteria;
    }

    public boolean willAccept(ActiveInterview activeInterview) {
        if ((activeInterview != null) && (activeInterview.company != null)) {

            if (!include.contains(activeInterview.company.id)) {
                return false;
            }
            if (exclude.contains(activeInterview.company.id)) {
                return false;
            }

            return true;
        }
        return false;
    }

    @Override
    public String getAttributeName() {
        return "company";
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
