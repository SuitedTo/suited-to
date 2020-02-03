package models.filter.question;

import models.Category;
import models.Question;
import play.db.jpa.JPA;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;

/**
 * Filter by category name
 *
 * @author joel
 */
public class ByCategoryName extends QuestionFilter<String> {

    public Predicate asPredicate(Root<Question> root) {

        CriteriaBuilder criteriaBuilder = JPA.em().getCriteriaBuilder();
        Predicate criteria = (include.size() == 0) ? criteriaBuilder.conjunction() : criteriaBuilder.disjunction();


        for (String name : include) {
            List<Category> categories = Category.find("byNameIlike", "%" + name + "%").fetch();
            if (categories != null) {
                for (Category category : categories) {
                    criteria = criteriaBuilder.or(criteria, criteriaBuilder.equal(root.get("category"), category));
                }
            }
        }

        for (String name : exclude) {
            List<Category> categories = Category.find("byNameIlike", "%" + name + "%").fetch();
            if (categories != null) {
                for (Category category : categories) {
                    criteria = criteriaBuilder.and(criteria,
                            criteriaBuilder.not(
                                    criteriaBuilder.isMember(category, root.<List<Category>>get("categories"))));
                }
            }
        }

        return criteria;
    }

    public boolean willAccept(Question question) {

        final Category category = question.category;
        if ((question != null) && (category != null)) {

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
        return "category";
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
