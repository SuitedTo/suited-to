package controllers;

import com.stripe.model.Card;
import com.stripe.model.Coupon;

import com.stripe.model.Customer;
import data.validation.PasswordCheck;
import enums.*;
import jobs.HandleAccountChangesJob;
import models.Company;
import models.User;
import models.embeddable.PhoneNumber;
import notifiers.Mails;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.data.binding.As;
import play.data.validation.Phone;
import play.data.validation.Valid;
import play.db.jpa.JPA;
import play.i18n.Messages;
import play.libs.OpenID;
import play.mvc.Before;
import utils.StripeUtil;

import java.util.*;

/**
 * Handles all aspects of Company Registration and Management
 */
public class CompanyManagement extends ControllerBase {

    /*************************************************
     * Enums and Static Fields                       *
     *************************************************/

    public enum Mode {
        UPGRADE, STANDARD, ADMIN, TRIAL_EXPIRED, PAYMENT
    }

    public enum FormField {
        COMPANY_NAME, ACCOUNT_TYPE, PAYMENT, SETTINGS
    }

    private static final Map<Mode, List<FormField>> hiddenFieldMap;
    private static final Map<Mode, List<FormField>> requiredFieldMap;

    static {
        hiddenFieldMap = new HashMap<Mode, List<FormField>>();
        hiddenFieldMap.put(Mode.UPGRADE, Arrays.asList(FormField.ACCOUNT_TYPE, FormField.SETTINGS));
        hiddenFieldMap.put(Mode.PAYMENT, Arrays.asList(FormField.ACCOUNT_TYPE, FormField.SETTINGS));
        hiddenFieldMap.put(Mode.TRIAL_EXPIRED, Arrays.asList(FormField.ACCOUNT_TYPE, FormField.SETTINGS));
        requiredFieldMap = new HashMap<Mode, List<FormField>>();
    }

    /*************************************************
     * Access Checks                                 *
     *************************************************/

    @Before(unless={"save", "manage"})
    private static void standardIdCheck(){
        Long id = Long.valueOf(request.params.get("id"));
        Company company = Company.findById(id);
        if(company.hasAccess(Security.connectedUser())){
            return;
        }
        forbidden();
    }

    /**
     * Performs custom access checks.  This is necessary because this controller needs to provide limited access to
     * un-authenticated users in the case where they are making updates to the account due to payment or other issues.
     */
    @Before(only={"save","manage"})
    private static void accessCheck(){
        String idString = request.params.get("id");
        if(idString == null){
            idString = request.params.get("company.id");
        }

        User user = Security.connectedUser();

        //if no id just make sure we have an authenticated user
        if(idString == null && user != null){
            return;
        }

        //lookup the company
        Long id = Long.valueOf(idString);
        Company company = id != null ? Company.<Company>findById(id) : null;
        notFoundIfNull(company);


        /*In the case where a user enters the correct login credentials but is not able to access the system due to a
        problem with the payment on their account they will have the opportunity to access this account management
        functionality if they are a company admin.  The Security controller will add the "temporaryUserName" to the
        session*/
        if(user == null) {
            String temporaryUserName = session.get("temporaryUserName");
            user = User.findByUsername(temporaryUserName);
            //set the user in the routeArgs so the Action methods have access to it
            routeArgs.put("temporaryUser", user);
        }

        //make sure the user has company admin role and has access to the company he's trying to manage
        if(user == null || !company.hasAdminAccess(user)){
            forbidden();
        }
    }



    /*************************************************
     * Actions                                       *
     *************************************************/

    /**
     * Main entry point for Company management including standard administration and upgrading accounts. Security is
     * controlled by the accessCheck method that is configured to before invocation of this method.
     * @param id primary key of the Company being managed (may be null)
     * @param mode Mode that we're operating in
     * @param accountType If upgrading the accountType will be the new accountType that the user is upgrading into
     */
    public static void manage(Long id, Mode mode, AccountType accountType){
        mode = mode != null ? mode : Mode.STANDARD; //default to standard mode
        Company company = determineAppropriateCompany(id);
        if(accountType != null) {
            company.accountType = accountType;
        }
        prepareForm();

        User connectedUser = Security.connectedUser();
        render(company, mode, connectedUser);
    }


    /**
     * Performs validations and saves the Company.  This is invoked any time a company is being saved from the UI
     * including self registration, Application Admin management, and Company Admin management and therefore needs to
     * account for those various scenarios.
     */
    public static void save(@Valid Company company, String stripeToken, String couponCode, @Phone String phoneNumber,
                            Mode mode, Boolean usePaymentInfoOnFile, @As("MM/dd/yyyy")Date trialExpiration) throws Throwable {

        User connectedUser = Security.connectedUser();
        notFoundIfNull(company);

        boolean editMode = company.hasBeenSaved();

        //detatch the company from the hibernate session so that we can run a db query to get the existing account type
        //it will need to be merged back in before saving
        AccountType existingAccountType = null;
        if (editMode) {
            JPA.em().detach(company);
            existingAccountType = existingAccountType(company.id);

            // If The user is not already free, they can't change to free!
            if(existingAccountType != AccountType.FREE &&
                    company.accountType == AccountType.FREE) {
                badRequest();
            }
        }

        //kindof a hack since we're not even allowing the user to specify the phone number type, but this is quicker
        //than modifying the data model under a time crunch
        if(company.contactPhone == null){
            company.contactPhone = new PhoneNumber();
            company.contactPhone.phoneNumberType = PhoneType.WORK;
        }
        if(company.contactPhone.phoneNumberType == null){
            company.contactPhone.phoneNumberType = PhoneType.WORK;
        }
        company.contactPhone.phoneNumberValue = phoneNumber;

        //trial expiration date, if it exists must be at least one day in the future or equal to the currently saved value
        if(trialExpiration != null){
            boolean validExpiration = trialExpiration.equals(company.trialExpiration) || trialExpiration.after(new Date());
            validation.isTrue(validExpiration);
            if(validExpiration){
                company.trialExpiration = trialExpiration;
            }
        }

        //company admin is changing the account type = delete the trial period
        if(connectedUser != null && connectedUser.hasRole(RoleValue.COMPANY_ADMIN) && !existingAccountType.equals(company.accountType)){
            company.trialExpiration = null;
        }

        //do the initial validation which will handle all the checks on the Company and some basic User validation.
        //we need to do this 2-step validation so that we have a saved Company present on the User.  If downstream
        //validation fails the transaction will be rolled back.
        if (validation.hasErrors()) {
            prepareForm();
            render("@manage", company, mode, connectedUser);
        }

        if (editMode) {
            company = JPA.em().merge(company);
        }

        /*if we're editing an existing company then we may need to do some recalculations on the company data
        there is a chance that the connectedUser would be null here if we're accessing this Action in order to correct
        payment errors as a company admin.  In that case there won't be an actual connectedUser but we can grab the
        temporaryUser from the routeArgs which is set in the accessCheck method*/
        User temporaryUser = (User) routeArgs.get("temporaryUser");
        User userToRunJobsAs = connectedUser != null ? connectedUser : temporaryUser;

        if(editMode){
            try {
                new HandleAccountChangesJob(userToRunJobsAs, company, existingAccountType).doJob();
            } catch (Exception e) {
                Logger.error("Unable to execute HandleAccountChangesJob", e);
            }
        }

        /*
        * Update the Stripe Payment System
        * */
        boolean stripeUpdatedSuccessfully = await(StripeUtil.updatePaymentSystemDataAsPromise(company, stripeToken, couponCode));
        if(!stripeUpdatedSuccessfully){
            JPA.setRollbackOnly();
            if(connectedUser != null && connectedUser.hasRole(RoleValue.APP_ADMIN)){
                validation.addError("company", "Could not update the payment system. Check the note on company configuration requirements");
            } else {
                validation.addError("company", "Unable to process payment information");
            }
            prepareForm();
            render("@manage", company, mode, connectedUser);
        }

        //at this point Stripe has been updated so check additional conditions
        Customer stripeCustomer = await(StripeUtil.getCustomerAsPromise(company));
        Card activeCard = StripeUtil.getDefaultCard(stripeCustomer);

        boolean shouldUsePaymentInfoOnFileButNoneExists = usePaymentInfoOnFile &&  activeCard == null;

        //if connected user is null that means it is a company user in trying to correct payment info
        boolean companyUser = connectedUser == null || connectedUser.hasRole(RoleValue.COMPANY_ADMIN);
        boolean paymentRequired = !StripeUtil.is100PercentDiscount(stripeCustomer) && company.isTrialExpired();
        boolean accountInGoodStanding = StripeUtil.isSubscriptionInGoodStanding(stripeCustomer);


        if(shouldUsePaymentInfoOnFileButNoneExists || (companyUser && paymentRequired && !accountInGoodStanding)) {
            JPA.setRollbackOnly();
            validation.addError("company", "Unable to process payment information");
            prepareForm();
            render("@manage", company, mode, connectedUser);
        }

        if(company.hasBeenSaved()){
        	company = company.merge();
        }
        company.save();


        /*if we're in "Correcting Payment Info" mode and the user is now allowed to log in because he has provided updated
        payment information then initialize the user information in the session.  Note that checkAuthentication will
        eventually check the Company associated with the username provided for things like trialExpired or delinquent
        which are values that could be updated as part of this Action.  Since all validation is complete at this point
        and we're not saving any additional data it is safe to go ahead and commit the current transaction so that the
        updated Company fields will be available when querying for the User.*/
        if(temporaryUser != null){
            //need the transaction to commit so that when we look the company back up it has the new data
            JPA.em().getTransaction().commit();
            boolean isUserNowAllowedToLogin = Security.checkAuthentication(temporaryUser.email);
            if(isUserNowAllowedToLogin){
                session.put("username", temporaryUser.email);
                session.put("prettyusername", temporaryUser.email);
            } else {
                flash.keep();
            }
        }

        Application.home();
    }

    /**
     * Deactivates the company
     * @param id primary key of the company to deactivate
     * @throws Throwable
     */
    public static void deactivate(Long id) throws Throwable{
        Company company = Company.findById(id);
        company.status = CompanyAccountStatus.DISABLED;
        company.deactivationDate = new Date();
        company.save();

        if(Security.connectedUser().hasRole(RoleValue.APP_ADMIN)){
            Companies.list();
        }
        else {
            Security.logout();
        }
    }


    /*************************************************
     * Helpers                                       *
     *************************************************/

    private static Company determineAppropriateCompany(Long id){
        Company company = null;
        if(id != null){
            company = Company.findById(id);
        } else {
            company = Security.connectedUser().company;
        }

        if(company == null){
            company = new Company();
        }

        return company;
    }

    public static boolean fieldRequired(FormField field, Mode mode, Company company){
        List<FormField> requiredFieldsForMode = requiredFieldMap.get(mode);
        return requiredFieldsForMode != null && requiredFieldsForMode.contains(field);
    }

    public static boolean fieldHidden(FormField field, Mode mode, Company company){
        List<FormField> hiddenFieldsForMode = hiddenFieldMap.get(mode);
        return hiddenFieldsForMode != null && hiddenFieldsForMode.contains(field);
    }

    public static String getLink(Company c) {
        java.util.Map<String, Object> params =
                new java.util.HashMap<String, Object>();

        if (c != null) {
            params.put("id", c.id);
        }

        return play.mvc.Router.reverse("CompanyManagement.manage", params).url;
    }

    public static String getLink() {
        Company c = null;

        if (Security.isConnected()) {
            c = Security.connectedUser().company;
        }

        return getLink(c);
    }

    private static void prepareForm(){
        renderArgs.put("showNav", Security.isConnected());
        List<Coupon> coupons = await(StripeUtil.fastGetAvailableCouponsAsPromise());
        renderArgs.put("availableCoupons", coupons);
        renderArgs.put("stripeToken", params.get("stripeToken"));

        Company company = null;
        String idParam = request.params.get("id");
        if (idParam != null) {
            company = Company.findById(Long.valueOf(idParam));
        }
        Card card = await(StripeUtil.getCardDataForPageRedisplayAsPromise(request.params.get("stripeToken"), company));
        renderArgs.put("card", card);
        if(params.get("couponCode") != null){
            renderArgs.put("couponCode", params.get("couponCode"));
        } else {
            String couponCode = await(StripeUtil.getExistingCouponCodeAsPromise(company));
            renderArgs.put("couponCode", couponCode);
        }

        //arguments added below this check should only be added if we're editing an existing company
        if(company == null){
            return;
        }
        renderArgs.put("currentUserCount", User.count("company = ? and status <> ?", company, UserStatus.DEACTIVATED));
        //interviewcount is used for messaging when changing account types as well as in statistics
        renderArgs.put("interviewCount", company.getActiveInterviewCount());

        //build the statistics but only if the logged in user is an app admin since they are the only ones who will
        //see the stats and the queries are pretty expensive
        final User user = Security.connectedUser();
        if(user != null && user.hasRole(RoleValue.APP_ADMIN)) {
            renderArgs.put("totalQuestionCount", company.getTotalQuestionCount());
            renderArgs.put("publicQuestionCount", company.getPublicQuestionCount());
            renderArgs.put("privateQuestionCount", company.getPrivateQuestions().size());
            renderArgs.put("candidateCount", company.getCandidates().size());
            renderArgs.put("userCount", company.getActiveOrPendingUsers().size());
            renderArgs.put("adminUserCount", company.getAdministrators().size());
            renderArgs.put("reviewerCount", company.getReviewerCount());
            renderArgs.put("contributingUserCount", company.getContributingUserCount());
        }
    }

    /**
     * Looks up the Company from the database and returns the AccountType from that saved record.  This assumes that
     * The company that matches the given id has been detatched from the Hibernate Session.
     * @param id Id of the Company to find AccountType for
     * @return The AccountType of the Company as it exists in the database
     */
    private static AccountType existingAccountType(Long id){
        if(id == null){
            return null;
        }
        Company existingCompany = Company.findById(id);
        AccountType existingAccountType = existingCompany.accountType;
        JPA.em().detach(existingCompany);
        return existingAccountType;
    }

}