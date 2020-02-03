package models.query.question;

import controllers.Security;
import enums.QuestionStatus;
import models.Question;
import models.User;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * Query for questions that the connected user can review.
 *
 * @author joel
 */
public class NeedsReview extends QuestionQuery {

    protected User user;

    public NeedsReview() {
        super();
        user = Security.connectedUser();
    }

    public NeedsReview(User user, Integer iSortCol_0, String sSortDir_0,
                       String sSearch, Integer iDisplayStart, Integer iDisplayLength) {
        super(iSortCol_0, sSortDir_0, sSearch, iDisplayStart, iDisplayLength);

        if (user == null) {
            this.user = Security.connectedUser();
        } else {
            this.user = user;
        }
    }
    
    @Override
    public Predicate buildCriteria(CriteriaBuilder criteriaBuilder,
                                   Root<Question> root, CriteriaQuery<Object> criteriaQuery) {

        //only OUT_FOR_REVIEW questions can be reviewed
        Predicate criteria = criteriaBuilder.equal(root.get("status"), QuestionStatus.OUT_FOR_REVIEW);
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

}
