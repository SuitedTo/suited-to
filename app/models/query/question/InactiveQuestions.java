package models.query.question;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import models.Question;
import models.User;


/**
 * Query for questions that are not active
 *
 * @author joel
 */
public class InactiveQuestions extends QuestionQuery {

    public InactiveQuestions() {
        super();
    }

    public InactiveQuestions(User user, Integer iSortCol_0, String sSortDir_0, String sSearch,
                             Integer iDisplayStart, Integer iDisplayLength) {
        super(iSortCol_0, sSortDir_0, sSearch, iDisplayStart, iDisplayLength);

    }

    @Override
    public Predicate buildCriteria(CriteriaBuilder criteriaBuilder, Root<Question> root,
                                   CriteriaQuery<Object> criteriaQuery) {
        Predicate criteria = criteriaBuilder.conjunction();

        criteria = criteriaBuilder.and(criteria, criteriaBuilder.equal(root.get("active"), false));

        return criteria;
    }

}
