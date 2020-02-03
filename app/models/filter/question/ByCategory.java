package models.filter.question;

import models.Category;
import models.Question;
import play.db.jpa.JPA;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * Filter by category
 *
 * @author joel
 */
public class ByCategory extends QuestionFilter<Long> {

    public Predicate asPredicate(Root<Question> root) {

        CriteriaBuilder criteriaBuilder = JPA.em().getCriteriaBuilder();
        Predicate criteria = (include.size() == 0) ? criteriaBuilder.conjunction() : criteriaBuilder.disjunction();


        for (Long id : include) {
            Category category = Category.findById(id);
            if (category != null) {
                criteria = criteriaBuilder.or(criteria, criteriaBuilder.equal(root.<Category>get("category"), category));
            }
        }

        for (Long id : exclude) {
            Category category = Category.findById(id);
            if (category != null) {
                criteria = criteriaBuilder.and(criteria,
                        criteriaBuilder.not(
                        		criteriaBuilder.equal(root.<Category>get("category"), category)));

            }
        }

        return criteria;
    }

    public boolean willAccept(Question question) {
        if ((question != null) && (question.category != null)) {

            if (!include.contains(question.category.id)) {
                return false;
            }
            if (exclude.contains(question.category.id)) {
                return false;
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
    protected String toString(Long id) {
        return String.valueOf(id);
    }

    @Override
    public Long fromString(String idStr) {
        return Long.parseLong(idStr);
    }

}
