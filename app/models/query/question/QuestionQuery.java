package models.query.question;

import models.Question;
import models.query.QueryBase;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * Base class for Question queries.
 *
 * @author joel
 */
public abstract class QuestionQuery extends QueryBase<Question> {

    public QuestionQuery() {
        super();
    }

    public QuestionQuery(Integer iSortCol_0, String sSortDir_0, String sSearch,
                         Integer iDisplayStart, Integer iDisplayLength) {
        super(iSortCol_0, sSortDir_0, sSearch, iDisplayStart, iDisplayLength);
    }

    public static final QuestionQuery getDefaultQuery() {
        return new AccessibleQuestions();
    }

    public String[] getSortableColumnNames() {
        return new String[]{"id", "status", "text", "time", "difficulty", "standardScore", "category"};
    }

    public Class<Question> getEntityClass() {
        return Question.class;
    }

    public abstract Predicate buildCriteria(CriteriaBuilder criteriaBuilder, Root<Question> root,
                                            CriteriaQuery<Object> criteriaQuery);

}
