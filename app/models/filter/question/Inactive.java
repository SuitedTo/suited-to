/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.filter.question;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import models.Question;
import models.filter.Filter;
import play.db.jpa.JPA;

/**
 *
 * @author hamptos
 */
public class Inactive implements Filter<Question> {

    public Predicate asPredicate(Root<Question> root) {
        
        CriteriaBuilder criteriaBuilder = JPA.em().getCriteriaBuilder();
        
        Predicate criteria = criteriaBuilder.conjunction();

        criteria = criteriaBuilder.and(criteria, criteriaBuilder.equal(root.get("active"), false));

        return criteria;
    }

    public Class<Question> getEntityClass() {
        return Question.class;
    }
}
