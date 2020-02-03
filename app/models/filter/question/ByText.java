package models.filter.question;

import models.Category;
import models.Question;
import org.apache.commons.lang.StringUtils;
import play.db.jpa.JPA;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

public class ByText extends QuestionFilter<String> {


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

    public Predicate asPredicate(Root<Question> root) {

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

    public boolean willAccept(Question question) {
        final Category category = question != null ? question.category : null;
        if (category != null) {

            if (!include.contains(category.name)) {
                return false;
            }
            if (exclude.contains(category.name)) {
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public String getAttributeName() {
        return "text";
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
