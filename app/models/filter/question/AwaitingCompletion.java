/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.filter.question;

import controllers.Security;
import enums.QuestionStatus;
import models.Question;
import models.User;
import models.filter.Filter;
import play.db.jpa.JPA;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 *
 * @author hamptos
 */
public class AwaitingCompletion implements Filter<Question> {

    public Predicate asPredicate(Root<Question> root) {

        User user = Security.connectedUser();
        CriteriaBuilder criteriaBuilder = JPA.em().getCriteriaBuilder();

        Predicate criteria = criteriaBuilder.equal(root.get("status"), QuestionStatus.AWAITING_COMPLETION);

        if(!user.superReviewer){

            //can't review your own questions unless you are a super reviewer
            criteria = criteriaBuilder.and(criteria,
                    criteriaBuilder.not(criteriaBuilder.equal(root.<User>get("user"), user)));

            criteria = criteriaBuilder.and(criteria,
                    criteriaBuilder.in(root.get("category")).value(user.reviewCategories));
        }

        criteria = criteriaBuilder.and(criteria, criteriaBuilder.equal(root.get("active"), true));

        return criteria;
    }

    public Class<Question> getEntityClass() {
        return Question.class;
    }

}
