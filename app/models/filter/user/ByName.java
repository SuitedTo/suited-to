package models.filter.user;

import models.User;
import org.apache.commons.lang.StringUtils;
import play.db.jpa.JPA;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

public class ByName extends UserFilter<String> {


    protected static Predicate addSearchCriteria(Path<String> attributePath, String sSearch, Predicate criteria, boolean include) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        Predicate result = criteria;
        if (StringUtils.isNotBlank(sSearch)) {
            Predicate textSearch;
            if (include) {
                textSearch = cb.like(cb.upper(attributePath), "%" + sSearch.toUpperCase() + "%");
                result = cb.or(criteria, textSearch);
            } else {
                textSearch = cb.notLike(cb.upper(attributePath), "%" + sSearch.toUpperCase() + "%");
                result = cb.and(criteria, textSearch);
            }
        }
        return result;
    }

    public Predicate asPredicate(Root<User> root) {

        CriteriaBuilder criteriaBuilder = JPA.em().getCriteriaBuilder();

        Predicate criteria = (include.size() == 0) ? criteriaBuilder.conjunction() : criteriaBuilder.disjunction();

        for (String member : include) {
            criteria = addSearchCriteria(root.<String>get(getAttributeName()), member, criteria, true);
        }

        for (String member : exclude) {
            criteria = addSearchCriteria(root.<String>get(getAttributeName()), member, criteria, false);
        }

        return criteria;
    }

    @Override
    public String getAttributeName() {
        return "fullName";
    }

    @Override
    protected String toString(String txt) {
        return txt;
    }

    @Override
    public String fromString(String txt) {
        return txt;
    }
}
