package models.query.question;

import controllers.Security;
import enums.QuestionStatus;
import enums.RoleValue;
import models.Question;
import models.User;

import javax.persistence.criteria.*;

/**
 * Query for questions that the given user is allowed to access
 *
 * @author joel
 */
public class AccessibleQuestions extends QuestionQuery {
    protected User user;

    public AccessibleQuestions() {
        super();
        this.user = Security.connectedUser();
    }

    public AccessibleQuestions(User user, Integer iSortCol_0, String sSortDir_0, String sSearch,
                               Integer iDisplayStart, Integer iDisplayLength) {
        super(iSortCol_0, sSortDir_0, sSearch, iDisplayStart, iDisplayLength);

        if (user == null) {
            this.user = Security.connectedUser();
        } else {
            this.user = user;
        }
    }

    @Override
    public Predicate buildCriteria(CriteriaBuilder criteriaBuilder, Root<Question> root,
                                   CriteriaQuery<Object> criteriaQuery) {
        Predicate resultPredicate;

        if (user == null) {
            //Non-logged users can't access any questions
            resultPredicate = criteriaBuilder.disjunction();
        } else if(user.hasRole(RoleValue.APP_ADMIN)){
            //APP_ADMINs can access all questions
            resultPredicate = criteriaBuilder.conjunction();
        } else {

            //Everybody can access public, accepted questions
            final Path<Object> questionStatus = root.get("status");
            Expression<Boolean> result = criteriaBuilder.equal(questionStatus, QuestionStatus.ACCEPTED);

            if(user.company == null){
                result = criteriaBuilder.or(result, criteriaBuilder.equal(root.get("user"), user));
            } else {
                Expression<Boolean> fromSameCompany = criteriaBuilder.equal(
                        root.get("user").get("company"), user.company);

                //Users in a company can access all questions from the same company,
                //regardless of question status
                result = criteriaBuilder.or(result, fromSameCompany);
            }

            Expression<Boolean> canReview;
            Expression<Boolean> reviewStatus = criteriaBuilder.equal(questionStatus, QuestionStatus.OUT_FOR_REVIEW);
            Expression<Boolean> awaitingCompleteStatus =
                    criteriaBuilder.equal(questionStatus, QuestionStatus.AWAITING_COMPLETION);
            canReview = criteriaBuilder.or(reviewStatus, awaitingCompleteStatus);

            //Super reviewers can review anything, but the rest of us...
            if (!user.superReviewer) {
                Expression<Boolean> notOwn = criteriaBuilder.not(
                        criteriaBuilder.equal(root.get("user"), user));

                Expression<Boolean> reviewableCategory;

                //See https://hibernate.onjira.com/browse/HHH-2045
                if (user.reviewCategories.isEmpty()) {
                    reviewableCategory = criteriaBuilder.disjunction();
                }
                else {
                    reviewableCategory = criteriaBuilder.in(
                        root.get("category")).value(user.reviewCategories);
                }

                canReview = criteriaBuilder.and(canReview, reviewableCategory);
                canReview = criteriaBuilder.and(canReview, notOwn);
            }

            //Any user can access questions he can review
            result = criteriaBuilder.or(result, canReview);

            //No inactive questions can be accessed by any non-APP_ADMIN user
            Expression<Boolean> isActive =
                    criteriaBuilder.isTrue(root.<Boolean>get("active"));
            result = criteriaBuilder.and(result, isActive);

            resultPredicate = criteriaBuilder.and(result, criteriaBuilder.notEqual(questionStatus, QuestionStatus.QUICK));
        }
        return resultPredicate;
    }

}
