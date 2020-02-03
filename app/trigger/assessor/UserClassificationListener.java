package trigger.assessor;

import models.Category;
import models.CategoryOverride;
import models.NearingReviewerNotification;
import models.Notification;
import models.Question;
import models.User;
import models.UserMetrics;
import notifiers.Mails;
import play.Play;
import play.db.jpa.GenericModel;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class UserClassificationListener implements PropertyChangeListener {
    //TODO create or integrate into something like the Data Triggers

    private static final String REVIEWER_MIN_CATEGORY_QUESTIONS_PROPERTY = "reviewer.minCategoryQuestions";
    private static final String REVIEWER_MIN_STREET_CRED_PROPERTY = "reviewer.minStreetCred";
    private static final String REVIEWER_LEEWAY_PROPERTY = "reviewer.leeway";
    private static final String REVIEWER_NEAR_QUESTION_THRESHOLD_PROPERTY = "reviewer.nearQuestionThreshold";
    private static final String REVIEWER_NEAR_STREETCRED_THRESHOLD_PROPERTY = "reviewer.nearStreetCredThreshold";
    private static final String PROINTERVIEWER_MIN_CATEGORIES_PROPERTY = "proInterviewer.minCategories";
    private static final String PROINTERVIEWER_STREET_CRED_PROPERTY = "proInterviewer.minStreetCred";
    private static final String PROINTERVIEWER_LEEWAY_PROPERTY = "proInterviewer.leeway";

    private static final String STREET_CRED_WATCH_VARIABLE = "streetCred";
    private static final String HR_COMPLIANT_WATCH_VARIABLE = "hrCompliant";
    private static final String NUM_ACCEPTED_QUESTIONS_WATCH_VARIABLE = "numberOfAcceptedQuestions";

    private static Long minCategoryQuestions =
            Long.valueOf(Play.configuration.getProperty(REVIEWER_MIN_CATEGORY_QUESTIONS_PROPERTY));
    private static Long nearQuestionThreshold =
            Long.valueOf(Play.configuration.getProperty(REVIEWER_NEAR_QUESTION_THRESHOLD_PROPERTY));
    private static Long reviewerMinStreetCred =
            Long.valueOf(Play.configuration.getProperty(REVIEWER_MIN_STREET_CRED_PROPERTY));
    private static Long nearStreetCredThreshold =
            Long.valueOf(Play.configuration.getProperty(REVIEWER_NEAR_STREETCRED_THRESHOLD_PROPERTY));
    private static Long proInterviewerMinStreetCred =
            Long.valueOf(Play.configuration.getProperty(PROINTERVIEWER_STREET_CRED_PROPERTY));
    private static Integer proInterviewerMinCategories =
            Integer.valueOf(Play.configuration.getProperty(PROINTERVIEWER_MIN_CATEGORIES_PROPERTY));
    private static Integer proInterviewerLeeway =
            Integer.valueOf(Play.configuration.getProperty(PROINTERVIEWER_LEEWAY_PROPERTY));
    private static Integer reviewerLeeway = Integer.valueOf(Play.configuration.getProperty(REVIEWER_LEEWAY_PROPERTY));

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if(evt.getSource() instanceof User){
            if (evt.getPropertyName().equals(STREET_CRED_WATCH_VARIABLE) ||
                    evt.getPropertyName().equals(HR_COMPLIANT_WATCH_VARIABLE)) {
                User user = (User)evt.getSource();

                if (isValidReviewer(user)) {
                    setEligibleReviewCategories(user, getEligibleReviewCategories(user, minCategoryQuestions));
                } else if (!user.reviewCategories.isEmpty() &&
                    user.streetCred <= reviewerMinStreetCred - reviewerLeeway) {

                    user.reviewCategories.clear();
                    user.proInterviewerCategories.clear();
                    List<Category> reviewCategoryOverrides = user.getReviewerOverrideCategories(true);
                    List<Category> proInterviewerCategoryOverrides = user.getProInterviewerOverrideCategories(true);

                    // Only send mail if no overrides are set, else add the overrides
                    if (reviewCategoryOverrides.isEmpty() && proInterviewerCategoryOverrides.isEmpty()) {
                        Mails.revokeReviewer(user);
                    } else {
                        user.reviewCategories.addAll(reviewCategoryOverrides);
                        user.proInterviewerCategories.addAll(proInterviewerCategoryOverrides);
                    }
                } else if (user.streetCred >= nearStreetCredThreshold && user.hrCompliant == false) {
                    notifyNearingReviewer(user);
                }

                if (isValidproInterviewer(user)) {
                    setEligibleProInterviewerCategories(user, user.reviewCategories);
                } else if (!user.proInterviewerCategories.isEmpty()) {
                    if (user.streetCred <= proInterviewerMinStreetCred - proInterviewerLeeway) {
                        user.proInterviewerCategories.clear();
                    }
                }
            }
        } else if (evt.getSource() instanceof UserMetrics) {
            if (evt.getPropertyName().equals(NUM_ACCEPTED_QUESTIONS_WATCH_VARIABLE)) {
                UserMetrics userMetrics = (UserMetrics)evt.getSource();

                if (isValidReviewer(userMetrics.user)) {
                    setEligibleReviewCategories(userMetrics.user,
                            getEligibleReviewCategories(userMetrics.user, minCategoryQuestions));
                } else if (!getEligibleReviewCategories(userMetrics.user, nearQuestionThreshold).isEmpty() &&
                            !userMetrics.user.hrCompliant) {
                    notifyNearingReviewer(userMetrics.user);
                }

                if (isValidproInterviewer(userMetrics.user)) {
                    setEligibleProInterviewerCategories(userMetrics.user, userMetrics.user.reviewCategories);
                }
            }
        }

        // some Notification handling -
        // remove nondismissable HR notification if user is now HR compliant
        if (evt.getSource() instanceof User && evt.getPropertyName().equals(HR_COMPLIANT_WATCH_VARIABLE)) {
            if (!((Boolean) evt.getOldValue()) && ((Boolean) evt.getNewValue())) {
                User user = (User) evt.getSource();
                Iterator<Notification> it = user.notifications.iterator();
                while(it.hasNext()){
                    Notification noti = it.next();
                    if(noti instanceof NearingReviewerNotification){
                        noti.delete();
                        it.remove();
                    }
                }
            }
        }
    }

    public static void rebuildClassificationLists(User user) {
        setEligibleReviewCategories(user, getEligibleReviewCategories(user, minCategoryQuestions));
        setEligibleProInterviewerCategories(user, user.reviewCategories);
    }

    private static boolean isValidReviewer(User user) {
        return (reviewerMinStreetCred.compareTo(user.streetCred) <= 0 &&
                user.hrCompliant &&
                !getEligibleReviewCategories(user, minCategoryQuestions).isEmpty());
    }

    private static boolean isValidproInterviewer(User user) {
        return (user.reviewCategories.size() >= proInterviewerMinCategories &&
                user.streetCred >= proInterviewerMinStreetCred &&
                user.hrCompliant);
    }

    private static List<Category> getEligibleReviewCategories(User user, long minimum) {
        GenericModel.JPAQuery q = Question.find("select distinct q.category from Question as q " +
                "inner join q.category category inner join q.user user " +
                "where user.id = :user_id and q.status = 'ACCEPTED' and active = TRUE " +
                "group by category having count(category) >= :category_min");
        q.setParameter("user_id", user.id);
        q.setParameter("category_min", minimum);
        return q.fetch();
    }

    private static void setEligibleReviewCategories(User user, List<Category> eligibleReviewCategories) {
        /*keep a record of the current review categories for the user, note that this should be a deep copy of the list
         and not a reference to the list itself since this will need to be preserved after the review categories
         are updated*/
        final List<Category> currentReviewCategories = new ArrayList<Category>(user.reviewCategories);
        final boolean isValidReviewer = isValidReviewer(user);

        Set<Category> categoriesToAdd = new HashSet<Category>();
        for (Category category : eligibleReviewCategories) {
            if (isValidReviewer && CategoryOverride.isReviewerAllowed(user, category) == null) {
                categoriesToAdd.add(category);
            }
        }

        categoriesToAdd.addAll(user.getReviewerOverrideCategories(true));
        user.reviewCategories.clear();
        user.reviewCategories.addAll(new ArrayList<Category>(categoriesToAdd));
        
        /*
         * Temporary hack to realize property change event. Once the pending enhancer updates get merged in
         * we should be able to just do user.reviewCategories = new ArrayList<Category>(categoriesToAdd);
         */
        user.propertyUpdated(User.class, "reviewCategories", currentReviewCategories, user.reviewCategories);
        
        //This is just to save the reviewCategories
        user.save();
    }

    private static void setEligibleProInterviewerCategories(User user, List<Category> eligibleReviewCategories) {
        Set<Category> categoriesToAdd = new HashSet<Category>();
        for (Category category : eligibleReviewCategories) {
            if (isValidproInterviewer(user) && CategoryOverride.isProInterviewerAllowed(user, category) == null) {
                categoriesToAdd.add(category);
            }
        }
        categoriesToAdd.addAll(user.getProInterviewerOverrideCategories(true));
        user.proInterviewerCategories.clear();
        user.proInterviewerCategories.addAll(new ArrayList<Category>(categoriesToAdd));
    }

    private void notifyNearingReviewer(User user) {
        boolean notiDisplayed = false;

        for (Notification noti : user.notifications) {
            if (noti instanceof NearingReviewerNotification) {
                notiDisplayed = true;
                break;
            }
        }

        if (!notiDisplayed) {
            NearingReviewerNotification nearingReviewerNotification = new NearingReviewerNotification(user);
            nearingReviewerNotification.save();
            user.notifications.add(nearingReviewerNotification);
            Mails.nearingReviewer(user, minCategoryQuestions, reviewerMinStreetCred);
        }
    }
}
