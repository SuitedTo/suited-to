package models.query.feedback;

import enums.RoleValue;
import models.ActiveInterview;
import models.Company;
import models.Feedback;
import models.User;
import play.db.jpa.JPA;
import utils.FilteredCriteriaHelper;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

public class AccessibleFeedbackHelper extends FilteredCriteriaHelper {

    private TableKey feedbackKey = addSourceEntity(Feedback.class);
    private CriteriaBuilder builder = JPA.em().getCriteriaBuilder();

    public AccessibleFeedbackHelper(User user) {
        if (user == null) {
            //A null user cannot access any feedback
            addCondition(builder.disjunction());
        } else {
            if (user.company == null) {

                /** A user with APP_ADMIN priveledges necessarily does not have a
                 *  company.  He should be able to see all feedback, which means just
                 *  don't restrict the query further
                 **/
                if (!user.hasRole(RoleValue.APP_ADMIN)) {
                    //A user with a null company who is not APP_ADMIN is a loner
                    //and can't view any feedback
                    addCondition(builder.disjunction());
                }
            } else {

                /** We need to restrict feedback access to within the standard user
                 *  or company admin's copmany. Thus we need to join
                 *  with the candidate table to verify that the candidate's company
                 *  matches the company of the user who requested the query.
                 */

                TableKey candidateKey = join(feedbackKey, "candidate");
                Expression entryCompany = field(candidateKey, "company");

                Company userCompany = user.company;

                addCondition(builder.isNotNull(entryCompany));
                addCondition(builder.equal(entryCompany, userCompany));

                /** Standard users may have access to more restrictions than company admins **/
                if(user.hasRole(RoleValue.QUESTION_ENTRY)) {

                    TableKey companyKey = join(candidateKey, "company");

                    Expression<User> feedbackSource = field(feedbackKey, "feedbackSource");
                    Expression<Boolean> adminVisibleOnlyFlag = field(feedbackKey, "adminVisibleOnlyFlag");
                    Expression<Boolean> feedbackDisplay = field(companyKey, "feedbackDisplay");

                    /** If the company's feedbackDisplay flag is true, the all users have full
                     *  access to every feedback not marked as adminVisibleOnly. If feedbackDisplay
                     *  is false, the user can only see feedback s/he has created.
                     */
                    Predicate feedbackDisplayCriteria = builder.isTrue(feedbackDisplay);
                    Predicate fDAndUserEqual = builder.isFalse(feedbackDisplay);
                    fDAndUserEqual = builder.and(fDAndUserEqual, builder.equal(feedbackSource, user));
                    fDAndUserEqual = builder.or(feedbackDisplayCriteria, fDAndUserEqual);
                    addCondition(fDAndUserEqual);

                    /** Similar to the above, but dealing with the feedback's
                     *  adminVisibleOnly flag.
                     */
                    Predicate adminVisibleCriteria = builder.isFalse(adminVisibleOnlyFlag);
                    Predicate flagAndUserEqual = builder.isTrue(adminVisibleOnlyFlag);
                    flagAndUserEqual = builder.and(flagAndUserEqual, builder.equal(feedbackSource, user));
                    adminVisibleCriteria = builder.or(adminVisibleCriteria, flagAndUserEqual);

                    addCondition(adminVisibleCriteria);
                }
            }
        }
    }
}
