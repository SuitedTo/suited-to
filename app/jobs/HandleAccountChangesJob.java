package jobs;

import enums.*;
import models.Company;
import models.Interview;
import models.Question;
import models.User;
import models.query.QueryResult;
import models.query.user.AccessibleUsers;
import play.Logger;
import play.jobs.Job;

import java.util.List;

/**
 * Makes any adjustments to data related to the Company that is necessary due to account type changes such as
 * deactivating users or interviews.
 */
public class HandleAccountChangesJob extends Job {

    /**
     * The user that caused the job to be executed.  Particularly important when disabling users for the company.  We
     * don't want to disable this one.
     */
    private final User user;

    /**
     * The Company having its data adjusted
     */
    private final Company company;

    /**
     * The AccountType for the company before its current state.  Used to determine if the company is upgrading or
     * downgrading its account type.
     */
    private final AccountType existingAccountType;

    /**
     * Single Constructor for HandleAccountChangesJob
     */
    public HandleAccountChangesJob(User user, Company company, AccountType existingAccountType) {
        //if an application Admin has initiated this Job we need to go find a suitable COMPANY admin to use as the
        //default user for all the lookup actions to follow.
        if(user.hasRole(RoleValue.APP_ADMIN)){
            this.user = company.getDefaultAdminUser();
            Logger.warn("HandleAccountChanges called by a SuitedTo Admin: " + user.email +
                    " Which means we have to figure out a suitable default user for the Company.  Using " +
                    this.user.email + " as default user");
        } else {
            this.user = user;
        }

        this.company = company;
        this.existingAccountType = existingAccountType;
    }

    @Override
    public void doJob() throws Exception {
        final AccountType newAccountType = company.accountType;
        if (newAccountType.equals(existingAccountType) || isAccountTypeUpgrade(newAccountType, existingAccountType)) {
            return;
        }

        handleUserChanges();
        handleInterviewChanges();
        handlePrivateQuestionChanges();
    }

    /**
     * Checks the given AccountType arguments to determine if the Account type is being upgraded
     *
     * @param updatingTo The new AccountType
     * @param existing   The existing AccountType
     * @return whether the AccountType change (if any) represents an upgrade
     */
    private static boolean isAccountTypeUpgrade(AccountType updatingTo, AccountType existing) {
        if (updatingTo == null || existing == null) {
            throw new IllegalArgumentException("Cannot check for AccountTypeUpgrade unless both new and existing are " +
                    "specified. updatingTo: " + updatingTo + " existing: " + existing);
        }

        //nothing is changing
        if (updatingTo.equals(existing)) {
            return false;
        }

        //we know they're different from previous check and ENTERPRISE is as high as it goes
        if (updatingTo.equals(AccountType.ENTERPRISE)) {
            return true;
        }

        //the only other upgrade scenario is going to STANDARD from FREE
        if (updatingTo.equals(AccountType.STANDARD) && existing.equals(AccountType.FREE)) {
            return true;
        }

        //if we got here it means our account type is FREE or we're downgrading from ENTERPRISE to STANDARD
        return false;

    }

    /**
     * If the company has more active or invited users than the account type allows mark all users except the one that
     * initiated this job as inactive.
     */
    private void handleUserChanges() {
        //userResults should not include the initiating user
        QueryResult<User> userResults =
                new AccessibleUsers(user, null, null, null, 0, Integer.MAX_VALUE).executeQuery();
        Long maxAllowed = company.accountType.getMax(AccountResource.USERS); //this may be null for ENTERPRISE

        //if we're going to end up with more users that the account type allows deactivate.  Remember that the
        //userResults doesn't contain the current user so we need to adjust that number for the check
        if (maxAllowed != null && userResults.getTotal() + 1 > maxAllowed) {
            List<User> users = userResults.getList();
            for (User companyUser : users) {
                if(UserStatus.PENDING.equals(companyUser.status)){
                    companyUser.status = UserStatus.INVITATION_WITHDRAWN;
                } else {
                    companyUser.status = UserStatus.DEACTIVATED;
                }
                companyUser.save();
            }
        }
    }

    /**
     * If the Company has more that the max allowed interviews for their new account type, Keep the newest interviews up
     * to that maximum allowed and deactivate all the older ones.
     */
    private void handleInterviewChanges() {
        List<Interview> interviews = company.getActiveInterviews();

        for (Interview interview : interviews) {
            if (!interview.active){
                interviews.remove(interview);
            }
        }

        Long maxAllowed = company.accountType.getMax(AccountResource.INTERVIEWS);
        if (maxAllowed != null && interviews.size() > maxAllowed) {
            for (int i = 0; i < interviews.size(); i++) {
                if (i >= maxAllowed) {
                    Interview interview = interviews.get(i);
                    interview.inactivate();
                }
            }
        }
    }

    private void handlePrivateQuestionChanges() {
        List<Question> privateQuestions = company.getPrivateQuestions();

        for (Question question : privateQuestions) {
            if (!question.active) {
                privateQuestions.remove(question);
            }
        }
        
        Long maxAllowed = company.accountType.getMax(AccountResource.PRIVATE_QUESTIONS);
        if(maxAllowed != null && privateQuestions.size() > maxAllowed){
            for ( int i= 0; i < privateQuestions.size(); i++){
                if(i >= maxAllowed){
                    Question question = privateQuestions.get(i);
                    question.active = false;
                    question.updateStatus(user, 
                            QuestionStatus.WITHDRAWN,
                            "Withdrawing Private Question Due to Account " +
                            "Downgrade");
                    question.save();
                }
            }
        }
    }
}
