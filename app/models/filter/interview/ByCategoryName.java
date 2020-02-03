package models.filter.interview;

import models.Category;
import models.Interview;
import models.Question;
import play.db.jpa.JPA;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * Filter by category names
 *
 * @author joel
 */
public class ByCategoryName extends InterviewFilter<String> {

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

    public Predicate asPredicate(Root<Interview> root) {

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

    public boolean willAccept(Interview interview) {

        if (interview != null) {
            List<Question> questions = interview.getQuestions();
            if (questions != null) {
                for (Question question : questions) {
                    if (!include.contains(question.category.name)) {
                        return false;
                    }
                    if (exclude.contains(question.category.name)) {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public String getAttributeName() {
        return "categories";
    }

    @Override
    protected String toString(String name) {
        return name;
    }

    @Override
    public String fromString(String name) {
        return name;
    }

}
