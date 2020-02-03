package models;

import enums.*;
import models.embeddable.PhoneNumber;
import models.filter.interview.ByActive;
import models.filter.interview.InterviewFilter;
import models.filter.question.ByStatus;
import models.filter.question.QuestionFilter;
import models.query.QueryBase;
import models.query.QueryFilterListBinder;
import models.query.QueryResult;
import models.query.interview.AccessibleInterviews;
import models.query.question.AccessibleQuestions;
import models.query.user.ActiveOrPendingUsers;
import models.query.user.AdminUsers;
import org.apache.commons.lang.StringUtils;
import org.hibernate.CacheMode;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;
import org.joda.time.DateTime;
import org.joda.time.Days;
import play.data.validation.Check;
import play.data.validation.CheckWith;
import play.data.validation.Email;
import play.data.validation.Required;
import play.db.jpa.JPABase;
import utils.StripeUtil;

import javax.persistence.*;
import java.util.*;

@Entity
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Company extends ModelBase implements AccountHolder, Restrictable {


    public static final int FREE_TRIAL_LENGTH_DAYS = 14;

    @CheckWith(CompanyNameCheck.class)
    public String name;

    /**
     * The Company's account type
     */
    @Required
    @Enumerated(EnumType.STRING)
    public AccountType accountType;

    /**
     * The Company's previous account type if they have made a change.
     */
    @Enumerated(EnumType.STRING)
    public AccountType previousAccountType;

    /**
     * The date/time that the company last changed their account type.  Will be null unless there has been an change to
     * account type
     */
    @Temporal(TemporalType.TIMESTAMP)
    public Date lastAccountChangeDate;
    
    @Temporal(TemporalType.TIMESTAMP)
    public Date trialExpiration;

    /**
     * Date when the company was deactivated.
     */
    public Date deactivationDate;

    /**
     * Unique identifier for this company in the third party payment system.  Initial implementation is with Stripe
     * http://www.stripe.com
     */
    public String paymentSystemKey;

    public String address;


    public String contactName;

    public PhoneNumber contactPhone;

    @Email
    public String contactEmail;



    public String contactJobTitle;

    /**
     * Indicates that the company can change their account type.  If this is true an Application admin will need to
     * apply any updates to the account.
     */
    public boolean accountTypeLock = false;

    /**
     * Indicates whether users can display everyone's feedback on a candidate or
     * just their own, (admins can always display all feedback displays)
     * true: full access
     * false: limited access
     */
    @Required
    public boolean feedbackDisplay = false;

    /**
     * enables integration with taleo on TRUE
     * functionality is limited to app_admin
     */
    @Required
    public boolean taleoIntegration = false;


    /**
     * the last four digit company credit card. Initial implementation is with Stripe
     */
    public String lastFourCardDigits;

    /**
     * The status of the company.  Can be set to DISABLED to prevent any users from the company from logging in
     */
    @Enumerated(EnumType.STRING)
    public CompanyAccountStatus status = CompanyAccountStatus.ACTIVE;


    /***********************************************************************************
     * Relationships to Child Entities                                                 *
     ***********************************************************************************/

    @OneToMany(mappedBy = "company", cascade = CascadeType.REMOVE)
    @Sort(type = SortType.NATURAL)
    public SortedSet<Job> jobs = new TreeSet<Job>();

    @OneToMany(mappedBy = "company", cascade = CascadeType.REMOVE)
    public List<User> users = new ArrayList<User>();

    @OneToMany(mappedBy = "company", cascade = CascadeType.REMOVE)
    @Sort(type = SortType.NATURAL)
    public SortedSet<Interview> interviews = new TreeSet<Interview>();

    @OneToMany(mappedBy = "company", cascade = CascadeType.REMOVE)
    public List<Candidate> candidates = new ArrayList<Candidate>();

    @OneToMany(mappedBy = "company", cascade = CascadeType.REMOVE)
    public List<QuestionNote> questionNotes = new ArrayList<QuestionNote>();
    
    @OneToMany(mappedBy = "company", cascade = CascadeType.REMOVE)
    public List<CompanyJobStatus> jobStatuses = new ArrayList<CompanyJobStatus>();

    /**
     * Stories assigned to this company. Need this field so that deletes are completely cascaded but we don't want to
     * access this property outside of the class itself.
     */
    @OneToMany(mappedBy = "company", cascade = CascadeType.REMOVE)
    private List<Story> stories = new ArrayList<Story>();


    /***********************************************************************************
     * Custom Validation Classes                                                       *
     ***********************************************************************************/

    /**
     * Custom validation for the Company's name field
     */
    public static class CompanyNameCheck extends Check {
        @Override
        public boolean isSatisfied(Object validatedObject, Object value) {
            Company company = (Company) validatedObject;

            if(company.nameRequired() && StringUtils.isEmpty((String) value)){
                setMessage("validation.companyName.required.moreThanOneUser");
                return false;
            }

            return true;
        }
    }

    /**
     * ********************************************************************************
     * Constructors                                                                    *
     * *********************************************************************************
     */

    public Company(String name) {
        this.name = name;
    }

    public Company() {}


    /***********************************************************************************
     * Utility Methods                                                                 *
     ***********************************************************************************/

    /**
     * Gets the default trial expiration date for this company.  The value may be overridden by application administrators
     * it also may be null for Enterprise accounts
     * @return
     */
    public Date getDefaultTrialExpirationDate(){
        if(AccountType.STANDARD.equals(accountType)){
            Calendar cal = Calendar.getInstance();

            //setting the free trial up for standard users
            cal.add(Calendar.DATE, Company.FREE_TRIAL_LENGTH_DAYS);
            return cal.getTime();
        }

        return null;
    }

    /**
     * Indicates that a CompanyName is required for this Company. The company name is required if there is more than one
     * user in the company
     * @return boolean whether the companyName is required
     */
    public boolean nameRequired(){
        return hasBeenSaved() && users != null && users.size() > 1;
    }

    public List<User> getUsers() {
    	JPAQuery query = User.find("byCompany", this);
    	query.query.setHint("org.hibernate.cacheable", true);
        query.query.setHint("org.hibernate.cacheMode",CacheMode.NORMAL );
        return query.fetch();
    }

    public List<User> getUsersSortedByName(){
        JPAQuery query = User.find("company = ? order by LOWER(fullName)", this);
        query.query.setHint("org.hibernate.cacheable", true);
        query.query.setHint("org.hibernate.cacheMode",CacheMode.NORMAL );
        return query.fetch();
    }

    public SortedSet<Interview> getInterviews() {
        return interviews;
    }

    public AccountType getAccountType() {
        AccountType result;
        
        if (accountType == null) {
            result = AccountType.FREE;
        }
        else {
            result = accountType;
        }
        
        return result;
    }

    /**
     * Implement the setAccountType to override default behavior.  Whenever the accountType is set this will populate
     * the previous accountType and lastAccountChangeDate fields
     * @param accountType
     */
    public void setAccountType(AccountType accountType) {
        if (accountType != null && !accountType.equals(this.accountType)) {
            previousAccountType = this.accountType;
            lastAccountChangeDate = new Date();
        }

        this.accountType = accountType;

    }

    @Override
    public boolean hasAccess(User user) {
        //user is SuitedTo admin or the user's company matches this company. Role-based access controls may impose
        //further restrictions
        return user.hasRole(RoleValue.APP_ADMIN) || this.equals(user.company);
    }

    /**
     * Determines if the given user has admin access to the given company.  Includes company admins and Application
     * admins
     * @param user User to check for access
     * @return whether the given user has admin privileges for this company
     */
    public boolean hasAdminAccess(User user){
        return (user.hasOneOf(RoleValue.COMPANY_ADMIN, RoleValue.APP_ADMIN) && hasAccess(user));
    }
    
    /**
     * <p>Attempts to return a "canonical" admin user for this company.  The
     * returned user is guaranteed to be associated with this company, active, 
     * and set as company admin, but which such user it chooses is undefined.
     * </p>
     * 
     * <p>If for any reason such a user cannot be found, throws an 
     * <code>IllegalStateException</code>.</p>
     * 
     * @return An active, admin user for this company.
     * 
     * @throws IllegalStateException If no such user exists.
     */
    public User getDefaultAdminUser() {
        //try and find an admin user matching the company contact info
        User adminUser = 
                User.find("byCompanyAndEmail", this, contactEmail).first();
        if(adminUser != null && adminUser.hasRole(RoleValue.COMPANY_ADMIN) && 
                adminUser.status.equals(UserStatus.ACTIVE)){
            
            return adminUser;
        }

        List<User> activeUsers = User.find("byCompanyAndStatus", this, 
                UserStatus.ACTIVE).fetch();
        
        for (User activeUser : activeUsers) {
            if(activeUser.hasRole(RoleValue.COMPANY_ADMIN)){
                return activeUser;
            }
        }

        throw new IllegalStateException("No active, admin user for company " +
                name + ".");
    }
    
    /**
     * <p>Returns all administrative users associates with this company, sorted
     * in descending order by name.</p>
     * 
     * @return  A <code>List</code> of administrative users.
     */
    public List<User> getAdministrators() {
        
        QueryResult<User> admins = new AdminUsers(this, 1, "asc", null, 0, 
                Integer.MAX_VALUE).executeQuery();
        
        return admins.getList();
    }
    
    public List<User> getActiveOrPendingUsers() {
        
        QueryResult<User> admins = new ActiveOrPendingUsers(this, 1, "asc", 
                null, 0, Integer.MAX_VALUE).executeQuery();
        
        return admins.getList();
    }

    /**
     * Count of active users for this company who are either super reviewers or who have review privileges for at least
     * one category
     * @return long
     */
    public Long getReviewerCount(){
        return User.count("from User u where u.status in (?1) and u.company = ?2 and (u.superReviewer = ?3 or u.reviewCategories IS NOT EMPTY)",
                UserStatus.getStatusesConsideredInUse(), this, true);
    }

    /**
     * Count of users from this company who have created at least one question that has been accepted to the public
     * question pool.
     * @return long
     */
    public Long getContributingUserCount(){

        return User.count("from User u where u.status in (?1) and u.company = ?2 AND exists(select id from Question q where q.user = u and status = ?3)",
                UserStatus.getStatusesConsideredInUse(), this, QuestionStatus.ACCEPTED);
    }
    
    /**
     * <p>Returns all private questions associated with users of this company,
     * sorted from newest to oldest.</p>
     * 
     * @return A <code>List</code> of private questions, newest to oldest.
     */
    public List<Question> getPrivateQuestions() {
        QueryBase query = new AccessibleQuestions(getDefaultAdminUser(), 5, 
                "desc", null, 0, Integer.MAX_VALUE);

        List<QuestionFilter> filters = new ArrayList<QuestionFilter>(1);
        QuestionFilter filter = new ByStatus();
        filter.include(QuestionStatus.PRIVATE.name());
        filters.add(filter);

        QueryFilterListBinder filteredQuery = 
                new QueryFilterListBinder(query, filters);
        filteredQuery.init(5, "desc", 0, Integer.MAX_VALUE);
        QueryResult result = filteredQuery.executeQuery();
        
        return result.getList();
    }


    /**
     * The number of Accepted public questions that have been contributed by users belonging to this Company
     *
     * @return long number of questions
     */
    public long getPublicQuestionCount() {
        return Question.count("select count(q.id) from Question q, User u, Company c " +
                "where q.user = u and u.company = c and u.company = ? and q.status = ? and active = ?",
                this, QuestionStatus.ACCEPTED, true);
    }

    /**
     * The total number of questions created by this company that are active in the system. This includes private and
     * public questions as well as questions that are pending review or have been returned to the submitter for
     * additional edits.
     *
     * @return long number of quetsions
     */
    public long getTotalQuestionCount() {
        return Question.count("select count(q.id) from Question q, User u, Company c " +
                "where q.user = u and u.company = c and u.company = ? and active = ?",
                this, true);
    }
    
    /**
     * <p>Returns the number of active interviews associated with users of this company</p>
     * 
     * @return Number of active interviews.
     */
    public long getActiveInterviewCount() {
        QueryBase query = new AccessibleInterviews(getDefaultAdminUser(), 2, 
                "desc", null, 0, Integer.MAX_VALUE);
        
        List<InterviewFilter> filters = new ArrayList<InterviewFilter>(1);
        InterviewFilter filter = new ByActive();
        filter.include("true");
        filters.add(filter);
        
        QueryFilterListBinder filteredQuery = 
                new QueryFilterListBinder(query, filters);
        filteredQuery.init(2, "desc", 0, Integer.MAX_VALUE);
        
        return filteredQuery.executeCountQuery();
    }
    
    /**
     * <p>Returns all active interviews associated with users of this company,
     * sorted from newest to oldest.</p>
     * 
     * @return A <code>List</code> of active interviews, newest to oldest.
     */
    public List<Interview> getActiveInterviews() {
        QueryBase query = new AccessibleInterviews(getDefaultAdminUser(), 2, 
                "desc", null, 0, Integer.MAX_VALUE);
        
        List<InterviewFilter> filters = new ArrayList<InterviewFilter>(1);
        InterviewFilter filter = new ByActive();
        filter.include("true");
        filters.add(filter);
        
        QueryFilterListBinder filteredQuery = 
                new QueryFilterListBinder(query, filters);
        filteredQuery.init(2, "desc", 0, Integer.MAX_VALUE);
        QueryResult result = filteredQuery.executeQuery();
        
        return result.getList();
    }

    public List<Candidate> getCandidates() {
        List<Candidate> candidates = Candidate.find("byCompany", this).fetch();

        return candidates;
    }



    @Override
    public <T extends JPABase> T delete() {
        /*We're required to manually delete the users and candidates for the company because they contain ElementCollection
        fields which are not deleted properly when the delete cascaded down to these model classes via the deletion of
        a parent class. After deleting all the users and candidates remove them from their respective collections here
        so that they are no longer referenced during the delete of the actual Company.*/
        for (Candidate candidate : candidates) {
            candidate.delete();
        }
        this.candidates.clear();

        for (User user : users) {
            user.delete();

        }
        this.users.clear();



        return super.delete();
    }

	@Override
	public long getResourceUsage(AccountResource resource) {
		switch(resource){
			case PRIVATE_QUESTIONS:
				return getPrivateQuestions().size();
			case INTERVIEWS:
				return getActiveInterviewCount();
			case CANDIDATES:
				return Candidate.find("byCompany", this).fetch().size();
			case ADMINISTRATORS:
				return getAdministrators().size();
			case USERS:
				return getActiveOrPendingUsers().size();
			default:
				throw new IllegalArgumentException("Unknown account resource");
		}
	}


    /**
     * <p>Returns the number of days left on a company's free trial.  This
     * value could be negative if the company's free trial has expired.</p>
     *
     * <p>If the company's not on a free trial then
     * this method throws an
     * <code>IllegalStateException</code>.
     * </p>
     *
     * TODO: This method should also take into account if the company has a
     *       credit card on file (in which case they aren't in their free
     *       trial.)
     *
     * @return The number of days remaining in the company's free trial.
     * @throws IllegalStateException If the company is not on a free trail.
     */
    public int daysRemainingInFreeTrial() {

        if (trialExpiration == null) {
            throw new IllegalStateException();
        }
        //use the date at midnight to normalize the time component of the dates
        return Days.daysBetween(new DateTime().toDateMidnight(), new DateTime(trialExpiration).toDateMidnight()).getDays();
    }

    public boolean isTrialExpired(){
        if(trialExpiration != null) {
            return new Date().after(trialExpiration);
        }

        return false;
    }






}

