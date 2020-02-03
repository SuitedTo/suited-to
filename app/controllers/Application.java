package controllers;

import cache.KeyBuilder;
import controllers.deadbolt.Deadbolt;
import controllers.deadbolt.Restrict;
import enums.AccountType;
import enums.ActiveInterviewState;
import enums.QuestionStatus;
import enums.RoleValue;
import models.*;
import notifiers.*;
import notifiers.Mails;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import play.CorePlugin;
import play.Logger;
import play.cache.Cache;
import play.data.validation.Error;
import play.db.jpa.JPA;
import play.jobs.Every;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import play.mvc.With;
import utils.StripeUtil;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

@With(Deadbolt.class)
public class Application extends ControllerBase {

    /*************************************************
     * Static Fields                                 *
     *************************************************/

	private static String INSTANCE_ID = "?";
    private static final int MAX_AWAITING_COMPLETION_QUESTIONS = 5;
    public static final String COMPANY_UPGRADE_COMPLETE_PARAM = "companyUpgradeComplete";
    
    static{
    	try {
			INSTANCE_ID = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
		}
    }


    /*************************************************
     * Actions                                       *
     *************************************************/

    /**
     * After initial registration allows the user a first opportunity to decide whether to use the system as a company
     * or a contributor
     */
    public static void chooseUsageType(){
        render();
    }

    /**
     * Saves the user's selection for application usage type uses Role value as the selection value.  Role value may be
     * null if a selection was not made
     * @param roleValue
     */
    public static void saveUsageType(RoleValue roleValue){

        User user = Security.connectedUser();

        if(roleValue != null){
            user = Security.connectedUser();
            user.roles.clear();
            user.roles.add(roleValue);

            if(RoleValue.COMPANY_ADMIN.equals(roleValue)){
                Company company = new Company();
                company.accountType = AccountType.STANDARD;
                company.trialExpiration = company.getDefaultTrialExpirationDate();
                company.contactEmail = user.email;

                await(StripeUtil.createNewPaymentSystemCustomerAsPromise(company, null, null));
                
                user = user.merge();

                company.save();
                user.company = company; //company needs to be assigned to user after the .merge() calls so the instances match up

                notifiers.Mails.adminWelcome(user);

            }
            else {
                notifiers.Mails.individualWelcome(user);
            }
            user.save();
            flash.put(COMPANY_UPGRADE_COMPLETE_PARAM, flash.get(COMPANY_UPGRADE_COMPLETE_PARAM));
        }
        else {
            /**
             * "I don't know right now" users have a null roleValue, but
             * still receive the contributor welcome email.
             */
            notifiers.Mails.individualWelcome(user);
        }

        home();
    }

    public static void upgradeAccount(){
        User user = Security.connectedUser();
        List<AccountType> upgradeOptions = user.getAccountUpgradeOptions();
        render(upgradeOptions);
    }

    public static void processUpgrade(AccountType accountType){
        User user = Security.connectedUser();
        //make sure the user is actually allowed to perform the upgrade
        if (!user.getAccountUpgradeOptions().contains(accountType)){
            unauthorized();
        }

        Company company = user.company; //may be null

        /*upgrading from individual to a Standard company account. This is essentially like choosing company with the
        initial usage type selection unless the user had gone into a company account and then downgraded to an individual
        account*/
        if(AccountType.STANDARD.equals(accountType)){
            flash.put(COMPANY_UPGRADE_COMPLETE_PARAM, "true");
            if(company != null){
                user.roles.clear();
                user.roles.add(RoleValue.COMPANY_ADMIN);
                user.save();
                home();
            } else {
                saveUsageType(RoleValue.COMPANY_ADMIN); //will create a new company for the user and render the home page
            }
        } else if(AccountType.ENTERPRISE.equals(accountType)){
            CompanyManagement.manage(null, CompanyManagement.Mode.UPGRADE, accountType);
        }


    }

    /**
     * Builds necessary data and renders the home(dashboard) page for connected users or renders the index page in the
     * absence of an authenticated user
     */
    public static void home() {
        if(Security.isConnected()) {
            /*it's possible to hit a case especially in dev mode where we're recreating the database all the time where
            * there is a user id present in the session but it doesn't match up with an actual user record.  in that
            * case force the logout action which will clear out the session data and display the index page allowing
            * the user to log in.*/
            if(Security.connectedUser() == null){
                try {
                    Security.logout();
                } catch (Throwable throwable) {
                   throw new RuntimeException(throwable);
                }
            }


            //table data for upcoming interviews
            List upcomingInterviewsWithoutStatus = getUpcomingInterviews();
            List <ActiveInterview> upcomingInterviews = new ArrayList<ActiveInterview>();

            Iterator<ActiveInterview> iterator = upcomingInterviewsWithoutStatus.iterator();

            while(iterator.hasNext()) {
                ActiveInterview ai = iterator.next();
                if(ai.getStatus().equals(ActiveInterviewState.NOT_STARTED)){
                    upcomingInterviews.add(ai);
                }
            }

            User user = Security.connectedUser();

            List<Object> awaitingCompletionQuestions = null;

            if (user.superReviewer) {
                awaitingCompletionQuestions = Question.find("status",
                        QuestionStatus.AWAITING_COMPLETION).fetch(MAX_AWAITING_COMPLETION_QUESTIONS);
            } else if (!user.reviewCategories.isEmpty()) {
                awaitingCompletionQuestions = new ArrayList<Object>();
                int questionsToAdd = MAX_AWAITING_COMPLETION_QUESTIONS;

                for (Category category : user.reviewCategories) {
                    if (questionsToAdd > 0) {
                        awaitingCompletionQuestions.addAll(Question.find("byCategoryAndStatus",
                                category, QuestionStatus.AWAITING_COMPLETION).fetch(questionsToAdd));
                        questionsToAdd = MAX_AWAITING_COMPLETION_QUESTIONS - awaitingCompletionQuestions.size();
                    }
                }
            }

            EntityManager em = JPA.em();
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<User> cq = cb.createQuery(User.class);
            Root<User> userRoot = cq.from(User.class);
            Path<Long> streetCred = userRoot.get("streetCred");
            Path<String> displayName = userRoot.get("displayName");
            // Nearest user with street cred higher than this user's
            TypedQuery<User> higher = em.createQuery(cq.where(cb.and(
                    cb.greaterThan(streetCred, user.streetCred), cb.isNotNull(displayName)))
                    .orderBy(cb.asc(streetCred))).setMaxResults(1);
            // Nearest user with street cred lower than this user's
            TypedQuery<User> lower = em.createQuery(cq.where(cb.and(
                    cb.lessThan(streetCred, user.streetCred), cb.isNotNull(displayName)))
                    .orderBy(cb.desc(streetCred))).setMaxResults(1);

            List<User> highestList;
            List<User> lowestList;
            User nextHighest;
            User nextLowest;

            highestList = higher.getResultList();
            if(highestList.size() > 0) {
                nextHighest = highestList.get(0);
            }
            else {
                nextHighest = null;
            }

            lowestList = lower.getResultList();
            if(lowestList.size() > 0) {
                nextLowest = lowestList.get(0);
            }
            else {
                nextLowest = null;
            }

            Integer badgesCount = Security.connectedUser().getEarnedBadgesCount();
            Boolean companyUpgradeComplete = Boolean.valueOf(flash.get(COMPANY_UPGRADE_COMPLETE_PARAM));
            render(upcomingInterviews, awaitingCompletionQuestions, nextHighest, nextLowest, badgesCount, companyUpgradeComplete);
        } else {
            Community.index();
        }
    }

    @Restrict(RoleValue.APP_ADMIN_STRING)
    public static void testJob(String name)
            throws ClassNotFoundException, InstantiationException,
            IllegalAccessException,
            NoSuchMethodException,
            IllegalArgumentException,
            InvocationTargetException {

        Class jobClass = Class.forName(
                "jobs." + name, false, Application.class.getClassLoader());

        Object job;
        job = jobClass.newInstance();

        Logger.warn("Running \"" + job.getClass() + "\"...");

        if (job instanceof Job) {
            ((Job) job).now();
        }
    }


    /*************************************************
     * AJAX Actions                                  *
     *************************************************/

    /**
     * Depending on the action specified by intendedAction, checks for additional data that the user is required to have
     * before executing that particular action.  If the user needs additional data then those additional field names are
     * returned in the response.
     * @param intendedAction The action that the user it trying to perform
     */
    public static void checkUserData(String intendedAction){
        Map<String, CheckUserDataResult> responseMap = new HashMap<String, CheckUserDataResult>();
        CheckUserDataResult result = new CheckUserDataResult();

        List<String> fieldList = new ArrayList<String>();
        User user = Security.connectedUser();

        /*note: if the question is going to be submitted as a private question then the intendedAction will be "privateQuestion"
        in which case we don't want to require any additional fields*/
        if("question".equals(intendedAction)){
            if(StringUtils.isEmpty(user.displayName)){
                fieldList.add("displayName");
            }
        } else if("invitation".equals(intendedAction)){
            if(StringUtils.isEmpty(user.fullName)){
                fieldList.add("fullName");
            }

            Company company = user.company;
            if(company != null && StringUtils.isEmpty(company.name)){
                fieldList.add("companyName");
            }
        }
        result.action = intendedAction;
        result.fields = fieldList;
        responseMap.put("data", result);

        renderJSON(responseMap);
    }

    private static class CheckUserDataResult {
        private List fields;
        private String action;
    }

    public static void saveUserData(String displayName, String companyName, String fullName){
        final User connectedUser = Security.connectedUser();
        boolean companyUpdated = false;
        boolean userUpdated = false;


        if(StringUtils.isNotEmpty(displayName)){
            connectedUser.displayName = displayName;
            userUpdated = true;
        }

        if(StringUtils.isNotEmpty(fullName)){
            connectedUser.fullName = fullName;
            userUpdated = true;
        }

        final Company company = connectedUser.company;
        if(StringUtils.isNotEmpty(companyName) && company != null){
            company.name = companyName;
            companyUpdated = true;
        }

        validation.valid(connectedUser);
        if(validation.hasErrors()){
            Map<String, Object> result = new HashMap<String, Object>();
            List<Error> errors = validation.errors();
            List<String> messages = new ArrayList<String>();
            for (Error error : errors) {
                //strip out the generic error message that the framework adds when validating model objects
                if(!"Validation failed".equals(error.message())){
                    messages.add(error.message());
                }
            }
            result.put("errors", messages);
            renderJSON(result);
        }

        /*We're persisting data in Jobs due to concerns about deadlocks occurring due to updating the User and company
        * at the same time wrapping the job calls in await() will suspend the web request while the job executes while
        * ensuring that the execution is complete before sending the response back to the client*/
        if(userUpdated){
            await(new Job() {
                @Override
                public void doJob() throws Exception {
                    User user = connectedUser.merge();
                    user.save();
                }
            }.now());

        }

        if(companyUpdated){
            await(new Job() {
                @Override
                public void doJob() throws Exception {
                    Company attachedCompany = company.merge();
                    attachedCompany.save();
                }
            }.now());
        }
        renderJSON(buildSuccessResponseMap());
    }

    /*************************************************
     * Helper/Utility Methods                        *
     *************************************************/

    /**
     * Retrieves a list of ActiveInterviews for the company associates with the logged in user that are scheduled
     * within 7 days of the current date.
     * todo: move this into User class since it could potentially be used by more than just the controller
     * @return List of ActiveInterviews
     */
    private static List<ActiveInterview> getUpcomingInterviews() {
        EntityManager em = JPA.em();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<ActiveInterview> cq = cb.createQuery(ActiveInterview.class);
        Root<ActiveInterview> ci = cq.from(ActiveInterview.class);

        //get the type of user logged into SuitedTo
        final User connectedUser = Security.connectedUser();

        //restrict by company
        Path company = ci.get("company");
        Predicate criteria = cb.conjunction();
        criteria = cb.and(criteria, cb.equal(company, connectedUser.company));

        if(!connectedUser.hasRole(RoleValue.COMPANY_ADMIN)){
            Path interviewer = ci.get("interviewer");
            criteria = cb.and(criteria, cb.equal(interviewer, connectedUser));
        }
        
        criteria = cb.and(criteria, cb.isTrue(ci.<Boolean>get("active")));

        Predicate criteria2 = cb.conjunction();

        /*
         * check if the interview date is today of 7 days in the future. If true,
         * post to dashboard. If no date exists, we still will post this as an
         * upcoming interview
         */
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DATE, 7);
        Date sevenDaysFromNow = calendar.getTime();
        Path interviewDate = ci.<Date>get("anticipatedDate");

        criteria2 = cb.and(criteria2, cb.between(interviewDate,
                DateUtils.truncate(new Date(), Calendar.DATE), sevenDaysFromNow ));

        criteria2 = cb.or(criteria2, cb.isNull(interviewDate));

        criteria = cb.and(criteria, criteria2);
        cq.where(criteria);

        //orderBy
        cq.orderBy(cb.asc(interviewDate));

        TypedQuery<ActiveInterview> query = em.createQuery(cq);

        return query.getResultList();

    }


    public static String getInstanceId(){
    	return Application.INSTANCE_ID;
    }
    
    @Every("2min")
    public class UpdateStatus extends play.jobs.Job{
    	
    	public void doJob(){
        	String status = CorePlugin.computeApplicationStatus(false).replaceAll("\n", "<br>");        	
    		Cache.set(KeyBuilder.buildInstanceKey(Application.INSTANCE_ID, Application.class.getName() + "status"),
    			status);
    		
    	}
    }
}