package models;

import com.amazonaws.services.s3.model.AmazonS3Exception;
import controllers.Categories;
import controllers.Security;
import db.jpa.S3Blob;
import enums.*;
import exceptions.LoginException;
import exceptions.NoWritePrivilegesException;
import models.annotations.Access;
import models.annotations.PropertyChangeListeners;
import models.deadbolt.Role;
import models.deadbolt.RoleHolder;
import models.events.channels.userBadge.BadgeProgressEvent;
import models.events.channels.userBadge.BadgeProgressEventBuilder;
import models.events.subscribers.userBadge.UserBadgeManager;
import models.filter.userBadge.ByMultiplier;
import models.filter.userBadge.ByUser;
import models.filter.userBadge.UserBadgeFilter;
import models.query.QueryBase;
import models.query.QueryFilterListBinder;
import models.query.QueryResult;
import models.query.userBadge.UserBadgeQuery;
import newsfeed.listener.NewReviewerNewsFeedListener;
import newsfeed.listener.ProInterviewerNewsFeedListener;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.mindrot.jbcrypt.BCrypt;
import play.Logger;
import play.Play;
import play.data.binding.As;
import play.data.binding.NoBinding;
import play.data.binding.TypeBinder;
import play.data.validation.*;
import play.db.jpa.JPA;
import play.db.jpa.JPABase;
import play.libs.Crypto;
import trigger.assessor.UserClassificationListener;
import utils.ExistingValueShouldBePreservedException;

import javax.persistence.*;
import javax.persistence.criteria.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.*;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.CascadeType.REMOVE;

/** 
 * A User of the application.
 */
@Entity
@Table(name = "app_user") //use non-default table name because "User" is reserved in some RDBMS
@PropertyChangeListeners({UserMetrics.Synchronizer.class, NewReviewerNewsFeedListener.class,
                          UserClassificationListener.class, ProInterviewerNewsFeedListener.class})
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class User extends ModelBase implements RoleHolder, AccountHolder, Restrictable {

    public static final String DEFAULT_TIMEZONE = "America/New_York";

    /**
     * Length of the invitationKey.
     */
    private static final int INVITATION_KEY_LENGTH = 24;
    
    /**
     * User's email address. Used for login purposes as well.
     */
    @Email
    @CheckWith(UniqueEmailCheck.class)
    @Column(unique = true)
    @Access(
            visibility = AccessScope.ALL_USERS,
            mutability = AccessScope.USER_AND_COMPANY_ADMIN_AND_APP_ADMIN)
    @As(binder = TrimBinder.class)
    public String email;

    public String timeZone;
    
    
    /**
     * Whether this user's email address will be displayed
     * as part of the public profile
     */
    @Access(
            visibility = AccessScope.USER_AND_APP_ADMIN,
            mutability = AccessScope.USER_AND_APP_ADMIN)
    @As(binder = CheckboxToBooleanBinder.class)
    public boolean publicEmail;


    /**
     * Whether this user will receive email notifications
     * on question status change 
     */
    @Access(
            visibility = AccessScope.USER_AND_APP_ADMIN,
            mutability = AccessScope.USER_AND_APP_ADMIN)
    @As(binder=CheckboxToBooleanBinder.class)
    public boolean questionStatusEmails;

    /**
     * whether this user will receive emails when
     * he/she forgets to stop an interview they have conducted, defaults to true
     */
    @Access(
            visibility = AccessScope.USER_AND_APP_ADMIN,
            mutability = AccessScope.USER_AND_APP_ADMIN)
    @As(binder=CheckboxToBooleanBinder.class)
    public boolean interviewStopReminder = true;

    /**
     * whether this user will receive emails when
     * feedback is requested of them, defaults to true
     */
    @Access(
            visibility = AccessScope.USER_AND_APP_ADMIN,
            mutability = AccessScope.USER_AND_APP_ADMIN)
    @As(binder=CheckboxToBooleanBinder.class)
    public boolean feedbackRequestedReminder = true;


    /**
     * Whether this user will receive confirmation
     * emails after supplying candidate feedback via
     * reply
     */
     @Access(
            visibility = AccessScope.USER_AND_APP_ADMIN,
            mutability = AccessScope.USER_AND_APP_ADMIN)
     @As(binder=CheckboxToBooleanBinder.class)
     public boolean feedbackReplyEmails = true;

    /**
     * User's profile picture thumbnail.  Uses S3Blob type to store a | delimited string containing necessary information to
     * look up the picture in Amazon S3. See http://www.playframework.org/modules/s3blobs
     */
    @Access(
            visibility = AccessScope.ALL_USERS,
            mutability = AccessScope.USER_AND_APP_ADMIN)
    @NoBinding
    public S3Blob thumbnail;

    /**
     * User's profile picture.  Uses S3Blob type to store a | delimited string containing necessary information to
     * look up the picture in Amazon S3. See http://www.playframework.org/modules/s3blobs
     */
    @Access(
            visibility = AccessScope.ALL_USERS,
            mutability = AccessScope.USER_AND_APP_ADMIN)
    @NoBinding
    public S3Blob picture;

    /**
     * Used to identify the user in public settings.  This is not required to be populated and in cases where this is
     * null, the fullName field will be used to display instead.
     */
    @CheckWith(DisplayNameCheck.class)
    @Column(unique = true)
    @Access(
            visibility = AccessScope.PUBLIC,
            mutability = AccessScope.USER_AND_APP_ADMIN)
    @As(binder=TrimBinder.class)
    public String displayName;

    /**
     * User's password.
     */
    @Access(
            visibility = AccessScope.USER_AND_APP_ADMIN,
            mutability = AccessScope.USER_AND_APP_ADMIN)
    @As(binder=BlankIndicatesNoChangeStringBinder.class)
    public String password;

    /**
     * A temporary password generated by the system as part of the reset password functionality.  Use this instead
     * of modifying the password directly so that people can't randomly reset other people's passwords.  A user can
     * be authenticated with the password or tempPassword if the tempPassword is used before the passwordExpiration date
     */
    public String tempPassword;

    /**
     * Date when the User's temporary password is no longer valid.
     */
    public Date passwordExpiration;

    /**
     * The user's full name.  No actual validation, but should typically be an actual person's name.
     */
    @CheckWith(FullNameCheck.class)
    @Access(
            visibility = AccessScope.COMPANY_AND_APP_ADMIN,
            mutability = AccessScope.USER_AND_COMPANY_ADMIN_AND_APP_ADMIN)
    @As(binder=TrimBinder.class)
    public String fullName;

    /**
     * The status of the User.  Defaults to PENDING.
     */
    @Access(
            visibility = AccessScope.ALL_USERS,
            mutability = AccessScope.COMPANY_AND_APP_ADMIN)
    public UserStatus status = UserStatus.PENDING;

    /**
     * Used to "salt" the user's password, such that even if two users have the same password string the hashed value
     * that is stored will still be unique.
     */
    public String salt = BCrypt.gensalt();

    /**
     * The company that this user belongs to. May be null for application admin users and "loners".
     */
    @ManyToOne(optional = true)
    @JoinColumn(name = "company_id", nullable = true, updatable = true)
    @CheckWith(CompanyPresentIfRequiredCheck.class)
    @Access(
            visibility = AccessScope.ALL_USERS,
            mutability = AccessScope.COMPANY_ADMIN_AND_APP_ADMIN)
    @As(binder=CompanyBinder.class)
    public Company company;

    /**
     * List of roles that the User has.  Roles are used in the authorization logic throughout the application.
     */
    @ElementCollection
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @Access(
            visibility = AccessScope.ALL_USERS,
            mutability = AccessScope.COMPANY_ADMIN_AND_APP_ADMIN)
    @As(value={","}, binder=RoleBinder.class)
    @Fetch(FetchMode.JOIN)
    public List<RoleValue> roles = new ArrayList<RoleValue>();

    /**
     * This user can review questions that contain one of these categories
     */
    @ManyToMany
    @JoinTable(name = "app_user_category",
            joinColumns = @JoinColumn(name = "app_user_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id"))
    @CheckWith(CategoriesCheck.class)
    @Access(
            visibility = AccessScope.ALL_USERS,
            mutability = AccessScope.APP_ADMIN)
    @As(value={","}, binder=CategoryBinder.class)
    public List<Category> reviewCategories = new ArrayList<Category>();

    @ManyToMany
    @JoinTable(name = "app_user_pro_interviewer_category",
            joinColumns = @JoinColumn(name = "app_user_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id"))
    @Access(
            visibility = AccessScope.ALL_USERS,
            mutability = AccessScope.APP_ADMIN
    )
    @As(value={","}, binder=CategoryBinder.class)
    public List<Category> proInterviewerCategories = new ArrayList<Category>();

    @Access(
            visibility = AccessScope.APP_ADMIN,
            mutability = AccessScope.APP_ADMIN)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<CategoryOverride> categoryOverrides = new ArrayList<CategoryOverride>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<ProInterviewerRequest> proInterviewerRequests = new ArrayList<ProInterviewerRequest>();

    /**
     * Indicates that this User has extra reviewer priviledges. Super reviewer has additional question review
     * capabilities including access to review questions from all categories as well as ability to review their own
     * questions.
     */
    @Access(
            visibility = AccessScope.APP_ADMIN,
            mutability = AccessScope.APP_ADMIN)
    @As(binder=CheckboxToBooleanBinder.class)
    public boolean superReviewer;

    /**
     * A unique key used to identify this user's invitation to join the application.
     */
    public String invitationKey;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
	public List<SocialIdentity> socialIdentities = new ArrayList<SocialIdentity>();


    /**
     * Timestamp of when the user last logged in to the application
     */
    @Temporal(TemporalType.TIMESTAMP)
    public Date lastLogin;

    /**
     * The User's "street cred" in the system based off of questions and categories they've created and/or reviewed.
     * Defaults to 0
     */
    public Long streetCred = 0L;
    
    
    /**
     * Has completed hr compliance training
     */
    public boolean hrCompliant;

    @CheckWith(UniqueOpenIDCheck.class)
    public String googleOpenIdUrl;
    public String googleOpenIdEmail;

    /**
     * Indicates that the user has at least started the system introduction
     */
    public boolean hasViewedIntroduction;


    /***********************************************************************
     * Privacy settings                                                    *
     ***********************************************************************/
    
    //Do not access these directly, dammit!  Use the isXXX() methods to 
    //retrieve so that the privacyLockdown option can do it's thing.
    public boolean picturePublic = true;
    public boolean badgesPublic = true;
    public boolean contributingCategoriesPublic = true;
    // User's roles (reviewer, pro interviewer, etc.)
    public boolean statusLevelPublic = true;
    public boolean submittedQuestionsPublic = true;
    public boolean reviewedQuestionsPublic = true;
    public boolean canConductInterviews = true;
    
    //When this is set to true, all privacy options should be read as "false"
    //We maintain them with the original values so that if the privacy lockdown
    //box gets unchecked, we can restore them to how they were.
    public boolean privacyLockdown = false;

    /***********************************************************************************
     * Relationships to Child Entities                                                 *
     ***********************************************************************************/

    @OneToMany(mappedBy = "feedbackSource", cascade = REMOVE)
    public List<Feedback> feedback = new ArrayList<Feedback>();

    @OneToMany(mappedBy = "user", cascade = REMOVE)
    public List<Interview> createdInterviews = new ArrayList<Interview>();

    @OneToMany(mappedBy = "interviewer", cascade = REMOVE)
    public List<ActiveInterview> activeInterviewsAsInterviewer = new ArrayList<ActiveInterview>();

    @OneToMany(mappedBy = "initiatingUser", cascade = REMOVE)
    public List<ActiveInterviewEvent> activeInterviewEvents = new ArrayList<ActiveInterviewEvent>();

    @OneToMany(mappedBy = "user", cascade = REMOVE)
    public List<UserBadge> badges = new ArrayList<UserBadge>();

    @OneToMany(mappedBy = "user", cascade = REMOVE)
    public List<QuestionWorkflow> questionWorkflows = new ArrayList<QuestionWorkflow>();

    @OneToMany(mappedBy = "user", cascade = REMOVE, orphanRemoval = true)
    public List<Notification> notifications = new ArrayList<Notification>();

    @OneToMany(mappedBy = "creator", cascade = REMOVE)
    public List<CandidateFile> candidateFiles = new ArrayList<CandidateFile>();

    @OneToOne(mappedBy = "user", cascade = ALL)
    public UserMetrics metrics;

    @OneToMany(mappedBy = "user", cascade = REMOVE)
    public List<Question> questionsCreated = new ArrayList<Question>();

    @OneToMany(mappedBy = "user", cascade = REMOVE)
    public List<QuestionMetadata> metadata = new ArrayList<QuestionMetadata>();

    @OneToMany(mappedBy = "creator", cascade = REMOVE)
    public List<Category> categoriesCreated = new ArrayList<Category>();

    @OneToMany(mappedBy = "user", cascade = REMOVE)
    public List<OAuthAccessor> oAuthAccessors = new ArrayList<OAuthAccessor>();

    /**
     * Stories assigned to this user. Need this field so that deletes are completely cascaded but we don't want to access
     * this property outside of the class itself
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private List<Story> stories = new ArrayList<Story>();

    /**
     * Events related to this user. Need this field so that deletes are completely cascaded but we don't want to access
     * this property outside of the class itself
     */
    @OneToMany(mappedBy = "relatedUser", cascade = CascadeType.REMOVE)
    private List<Event> relatedEvents = new ArrayList<Event>();

    /***********************************************************************
     * Custom Validations                                                   *
     ***********************************************************************/

    private static class UniqueEmailCheck extends Check {

        @Override
        public boolean isSatisfied(Object validatedObject, Object value) {
            boolean validEmail = true;
            //validate that email is unique to the system
            String email = (String) value;
            if (StringUtils.isNotEmpty(email)) {
                User user = (User) validatedObject;
                //get all possible matches
                List<User> users = User.find("byEmailIlike", email.toLowerCase()).fetch();

                //if there's more than one we've got a problem
                if (users.size() > 1) {
                    validEmail = false;
                }

                //if there is one make sure its the current user we're editing
                if (users.size() == 1 && !users.get(0).equals(user)) {
                    validEmail = false;
                }
            }

            setMessage("email.notUnique");
            return validEmail;
        }
    }

    private static class DisplayNameCheck extends Check {

        @Override
        public boolean isSatisfied(Object validatedObject, Object value) {
            boolean validUsername = true;

            User user = (User) validatedObject;
            String username = (String) value;

            //validate that displayName is unique to the system
            if (StringUtils.isNotEmpty(username)) {

                //get all possible matches
                List<User> users = User.find("trim(lower(displayName)) like ?",
                        username.toLowerCase().trim()).fetch();

                //if there's more than one we've got a problem
                if (users.size() > 1 || (users.size() == 1 && !users.get(0).equals(user))) {
                    setMessage("username.notUnique");
                    validUsername = false;
                }
            } else if(user.displayNameRequired()) {
                setMessage("validation.displayName.required");
                validUsername = false;
            }

            return validUsername;
        }

    }

    private static class FullNameCheck extends Check {
        @Override
        public boolean isSatisfied(Object validatedObject, Object value) {
            User user = (User) validatedObject;
            if(user.fullNameRequired() && StringUtils.isEmpty((String)value)){
                setMessage("validation.fullName.required");
                return false;
            }
            return true;
        }
    }

    private static class UniqueOpenIDCheck extends Check {

        @Override
        public boolean isSatisfied(Object validatedObject, Object value) {
            boolean validOpenID = true;
            //validate that email is unique to the system
            String openID = (String) value;
            if (StringUtils.isNotEmpty(openID)) {
                User user = (User) validatedObject;
                //get all possible matches
                List<User> users = User.find("byGoogleOpenIdUrl", openID).fetch();

                //if there's more than one we've got a problem
                if (users.size() > 1) {
                    validOpenID = false;
                }

                //if there is one make sure its the current user we're editing
                if (users.size() == 1 && !users.get(0).equals(user)) {
                    validOpenID = false;
                }
            }
            setMessage("openID.notUnique");
            return validOpenID;
        }

    }

    private static class CompanyPresentIfRequiredCheck extends Check {
        @Override
        public boolean isSatisfied(Object validatedObject, Object value) {
            Company company = (Company) value;
            User user = (User) validatedObject;
            //if the user is a company admin it must be associated with a company
            setMessage("company.notPresentForCompanyAdmin");
            return !user.hasRole(RoleValue.COMPANY_ADMIN) || company != null;
        }
    }

    public static class CategoriesCheck extends Check {
        @Override
        public boolean isSatisfied(Object validatedObject, Object value) {
            boolean valid = true;
            if (!((List<Category>)value).isEmpty()) {
                for (Category category : (List<Category>)value) {
                    if (category == null) {
                        valid = false;
                        setMessage("categories.nonExistantCategory");
                    }
                }
            }

            return valid;
        }
    }




    /***********************************************************************
     * Constructors                                                        *
     ***********************************************************************/

    /**
     * Default constructor.
     *
     * DON'T USE THIS CONSTRUCTOR. IT EXISTS SOLELY FOR LOADING OF DATA FROM
     * initial-data.yml
     * USE User(Company) INSTEAD. USING THE NO-ARGS CONSTRUCTOR WILL CAUSE 
     * ACCESS ISSUES.
     */
    public User() {
    	metrics = new UserMetrics(this);

        //This is really just to facilitate situations where we load users from
        //a test file.  Our client should always set a timezone on a user.
        timeZone = DEFAULT_TIMEZONE;
    }

    public User(Company company) {
        this.company = company;

        metrics = new UserMetrics(this);
        timeZone = DEFAULT_TIMEZONE;
    }

    /**
     * Typical constructor used to create a minimally populated user.
     *
     * @param email    User's email.
     * @param password Plain-text User's password. Will be hashed before storing.
     * @param fullName User's fullName, may be null.
     * @param company  User's company, may be null.
     */
    public User(final String email, final String password, final String fullName, final Company company) {
        this();
        this.email = email;
        setPassword(password);
        this.fullName = fullName;
        this.company = company;
    }

    /**
     * Creates a user without a Company.
     *
     * @param email    User's email.
     * @param password Plain-text User's password. Will be hashed before storing.
     * @param fullName User's fullName, may be null.
     */
    public User(final String email, final String password, final String fullName) {
        this(email, password, fullName, null);
    }

    /***********************************************************************************
     * Public Utility Methods                                                          *
     ***********************************************************************************/

    /**
     * Indicates that a displayName is required for this user.  A display name is required if the user has created at
     * least one publicly visible question
     * @return boolean whether the displayName should be required
     */
    public boolean displayNameRequired(){
        if(questionsCreated != null){  //shouldn't really ever happen, but just in case
            for (Question question : questionsCreated) {
                if(question.status != null && question.status.isPubliclyVisible()){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Indicates that a fullName is required for this user.  A fullName is required if the user is part of a company with
     * more than one associated user
     * @return boolean whether the fullName should be required
     */
    public boolean fullNameRequired(){
        return company != null && company.users.size() > 1;
    }

    /**
     * <p>Returns <code>true</code> <strong>iff</strong> this user is a reviewer
     * for at least one category (or a super reviewer).</p>
     * 
     * @return <code>true</code> <strong>iff</strong> this user is a reviewer.
     */
    public boolean isReviewer() {
        return superReviewer || 
                (reviewCategories != null && !reviewCategories.isEmpty());
    }

    @Override
    public AccountType getAccountType() {
        if(company != null){
            return company.getAccountType();
        }else{
            if(hasRole(RoleValue.APP_ADMIN)){
                return AccountType.UNLIMITED;
            }
        }

        return AccountType.INDIVIDUAL;
    }

    /**
     * Gets a list of AccountTypes that this user would be eligible to upgrade into
     * @return List of AccountTypes
     */
    public List<AccountType> getAccountUpgradeOptions() {
        List<AccountType> upgradeOptions = new ArrayList<AccountType>();

        if((hasRole(RoleValue.CONTRIBUTOR) || roles.isEmpty()) && company == null){
            upgradeOptions = Arrays.asList(AccountType.STANDARD, AccountType.ENTERPRISE);
        } else if(hasRole(RoleValue.COMPANY_ADMIN) && company.accountType.equals(AccountType.STANDARD)){
            upgradeOptions = Arrays.asList(AccountType.ENTERPRISE);
        }
        return upgradeOptions;
    }

    
    /*~***********************************************************************
     * Privacy Getters.  Note that none of these can match the variable name *
     * exactly because Play! likes to "help".                                *
     *************************************************************************/
    
    public boolean isProfilePicturePublic() {
        return !privacyLockdown && picturePublic;
    }
    
    public boolean isBadgeCollectionPublic() {
        return !privacyLockdown && badgesPublic;
    }
    
    public boolean isCategorySetPublic() {
        return !privacyLockdown && contributingCategoriesPublic;
    }
    
    public boolean isUserStatusLevelPublic() {
        return !privacyLockdown && statusLevelPublic;
    }
    
    public boolean isSubmittedQuestionListPublic() {
        return !privacyLockdown && submittedQuestionsPublic;
    }
    
    public boolean isReviewdQuestionListPublic() {
        return !privacyLockdown && reviewedQuestionsPublic;
    }
    
    public boolean isConductingInterviews() {
        return !privacyLockdown && canConductInterviews;
    }
    
    /*~*********************
     * End privacy getters *
     ***********************/

    /**
     * <p>Returns <code>true</code> <strong>iff</strong> this user is a pro interviewer
     * for at least one category.</p>
     *
     * @return <code>true</code> <strong>iff</strong> this user is a pro interviewer.
     */
    public boolean isProInterviewer() {
        return (proInterviewerCategories != null && !proInterviewerCategories.isEmpty());
    }

    /**
     * <p>Returns <code>true</code> <strong>iff</strong> this user should be visible outside of his company.  Community
     * membership is determined by the existence of a non null displayName
     * @return <code>true</code> <strong>iff</strong> this user is a member of the community
     */
    public boolean isCommunityMember(){
        return displayName != null;
    }

    /**
     * Overwrite the provided setter method in order to convert strings to null values for this one particular field.
     * We don't want to allow an empty string to be stored because there is a unique constraint on the column in the
     * database and null != null but '' == ''
     *
     * @param value
     * @throws NoWritePrivilegesException
     */
    @As(binder=TrimBinder.class)
    public void setDisplayName(String value) throws NoWritePrivilegesException
    {
        checkWriteAccess("displayName");
        value = StringUtils.isEmpty(value)?null:value;
        Object oldValue = this.displayName;
        Object newValue = value;
        this.displayName = value;
        
        propertyUpdated (models.User.class, "displayName", oldValue, newValue);
    }
    
    /**
     * Sets the user's password to a hash of the plain text password argument and clears the password expiration.
     *
     * @param password Plain text password value.
     */
    public final void setPassword(final String password) {
        this.password = hashPassword(password);
        //setting a new actual password clears out any temporary password data
        this.tempPassword = null;
        this.passwordExpiration = null;
    }

    /**
     * Sets a temporary password which is set to expire on the current date.
     *
     * @param password plain text password value.
     */
    public void setTemporaryPassword(final String password) {
        this.tempPassword = hashPassword(password);
        Date expirationDate = new Date();
        expirationDate = DateUtils.addDays(expirationDate, 1); //temporary password expires in 1 day
        this.passwordExpiration = expirationDate;
    }

    public boolean passwordExpired() {
        if (passwordExpiration != null) {
            return passwordExpiration.before(new Date());
        }
        return false;
    }
    
    public static void initMetrics() {
    	List<User> users = User.findAll();
    	for(User user : users){
    		if(user.metrics == null){
    			user.metrics = new UserMetrics(user);
    		}
    		
    		user.metrics.update();
    		
    		user.isolatedSave();
    	}
    }

    public static User findByUsername(String username) {
    	JPAQuery query = find("byEmailLike", username.toLowerCase());
    	query.query.setHint("org.hibernate.cacheable", true);
        return query.first();
    }

    public static User findByFullName(String name) {
        JPAQuery query = find("byFullNameLike", name.toLowerCase());
        query.query.setHint("org.hibernate.cacheable", true);
        return query.first();
    }


    /**
     * Allows the execution of all the logic of User.connect without requiring a password.  This is useful when using
     * OpenID for the actual authentication piece but we still want to execute all the additional checks outside of just
     * the login/password such as company account status.
     * @param email
     * @return User
     * @deprecated use connect(externalId, provider) instead
     */
    public static User connect(String email){
        LoginException standardException =
                new LoginException("We didn't recognize the username or password you entered. Please try again.");

        //one or both credentials missing
        if (StringUtils.isEmpty(email)){
            throw new LoginException("Please enter an email address and password.");
        }

        //No matching user found
        User user = findByUsername(email);
        if(user == null){
            throw standardException;
        }

        throwExceptionsIfNecessary(user);

        return user;
    }

    /**
     * Checks if a user matching the given credentials should be allowed to access the application.
     * @param email Email address for log in
     * @param password The password
     * @return The user matching the given credentials or null if no match is found or the user's status does not allow
     * login
     */
    public static User connect(String email, String password) {
        LoginException standardException =
                new LoginException("We didn't recognize the username or password you entered. Please try again.");

        //one or both credentials missing
        if (StringUtils.isEmpty(email) || StringUtils.isEmpty(password)){
            throw new LoginException("Please enter an email address and password.");
        }

        //No matching user found
        User user = findByUsername(email);
        if(user == null){
            throw standardException;
        }

        //check password
        boolean validPassword = false;
        if (user.password != null && user.password.equals(user.hashPassword(password))) {
            validPassword = true;
        }

        //allow the user to login with a temporary, system generated password if one exists and is not expired
        if (user.tempPassword != null && user.tempPassword.equals(user.hashPassword(password)) && !user.passwordExpired()) {
            validPassword = true;
        }

        if(!validPassword){
            throw standardException;
        }

        throwExceptionsIfNecessary(user);

        return user;
    }

    /**
     * Runs some additional checks on the User to ensure that his account status is in good standing and he should be
     * allowed to log in.  In cases where the user should not be able to log in, throws a reason-specific exception
     * @param user The user to run checks on
     */
    private static void throwExceptionsIfNecessary(User user){
        Company company = user.company;

        if(company != null && CompanyAccountStatus.DISABLED.equals(company.status)){
            throw new LoginException("Your account has been disabled, please contact your company administrator.");
        }

        //user is not allowed to log in
        if (user.status == null || !user.status.isAllowedToLogin){
            throw new LoginException("Your account has been disabled, please contact your company administrator.");
        }
    }

    /**
     * Looks up a user by string value first checking by email, then by fullName, and finally by displayName.  If a user
     * cannot be located with the given data returns null.
     *
     * @param userData String to use for the lookup
     * @return a User matching the given userData or null if none found
     */
    public static User lookup(String userData) {
        User user = null;
        user = User.find("byEmail", userData).first();
        if (user == null) {
            user = User.find("byFullName", userData).first();
        }
        if (user == null) {
            user = User.find("byDisplayName", userData).first();
        }

        return user;
    }

    public String hashPassword(String password) {
        return Crypto.passwordHash(BCrypt.hashpw(password, salt),
                Crypto.HashType.SHA512);
    }

    /**
     * A count of the Public questions that have been ACCPETED
     *
     * @return count of the questions
     */
    public Long getAcceptedPublicQuestionCount() {
        return Question.count("user = ?1 and status = ?2 and flaggedAsInappropriate = false",
                this, QuestionStatus.ACCEPTED);
    }

    /** TODO: Refactor this and getTopReviewCategoryNames
     * to be more DRY.
     *
     * Returns a list containing the names of a
     * user's top categories calculated by
     *
     * 1) Count of questions where user is the question
     * submitter and question is accepted (group by category)
     * 2) Count of questions where user is a reviewer (question
     * can be in any status, group by category)
     *
     * and ordered in descending relevance
     *
     * @max maximum number of
     *
     * @return
     */
    public List<String> getTopCategoryNames(@Required Integer max) {
        final class topCategoryResult {

            private Long qCount;
            private String name;

            public topCategoryResult (Long q, String n) {
                qCount = q;
                name = n;
            }

            public long getQCount() {
                return qCount;
            }

            public String getName() {
                return name;
            }
        }

        List<HashMap<String, Object>> topCategories=
        QuestionWorkflow.find("select new map( count(distinct q) as qCount, category.name as name) " +
                "from QuestionWorkflow as qw " +
                "inner join qw.question as q " +
                "inner join q.category as category " +
                // User was a reviewer
                "where (qw.user = :user and q.user <> :user) or " +
                // Accepted questions created by user
                "(q.user = :user and q.status = :status) " +
                "group by category " +
                "order by count(distinct q) desc")
                .setParameter("user", this)
                .setParameter("status", QuestionStatus.ACCEPTED)
                .fetch(max);

        /**
         * TODO: All of this comparator stuff (hypothetically)
         * could be fixed by properly ordering the query.
         */
        List<topCategoryResult> sortedResults = new ArrayList<topCategoryResult>();
        for(HashMap hm : topCategories) {
            Long qCount =  (Long) hm.get("qCount");
            String name = (String) hm.get("name");
            sortedResults.add(new topCategoryResult(qCount, name));
        }

        Comparator c = new Comparator<topCategoryResult>() {

            @Override
            public int compare(topCategoryResult r1, topCategoryResult r2) {
                Boolean r1Smaller = r1.qCount < r2.qCount;
                Boolean r1Larger = r1.qCount > r2.qCount;
                return r1Smaller ? -1 : r1Larger ? 1 : 0;
            }
        };

        Collections.sort(sortedResults, Collections.reverseOrder(c));

        List<String> stringListResult = new ArrayList<String>();
        for(topCategoryResult tcr: sortedResults) {
            stringListResult.add(tcr.getName());
        }

        return stringListResult;
    }

    /**
     * Returns a list of the categories a user has reviewed quesions
     * for, in descending order of number of questions reviewed in
     * that category.
     *
     * and ordered in descending relevance
     *
     * @max maximum number of
     *
     * @return
     */
    public List<String> getTopReviewCategoryNames(@Required Integer max) {
        final class topCategoryResult {

            private Long qCount;
            private String name;

            public topCategoryResult (Long q, String n) {
                qCount = q;
                name = n;
            }

            public long getQCount() {
                return qCount;
            }

            public String getName() {
                return name;
            }
        }

        List<HashMap<String, Object>> topCategories=
                QuestionWorkflow.find("select new map( count(distinct q) as qCount, category.name as name) " +
                        "from QuestionWorkflow as qw " +
                        "inner join qw.question as q " +
                        "inner join q.category as category " +
                        // User was a reviewer
                        "where (qw.user = :user and q.user <> :user " +
                        "and q.status = :status) " +
                        "group by category " +
                        "order by count(distinct q) desc")
                        .setParameter("user", this)
                        .setParameter("status", QuestionStatus.ACCEPTED)
                        .fetch(max);

        /**
         * TODO: All of this comparator stuff (hypothetically)
         * could be fixed by properly ordering the query.
         */
        List<topCategoryResult> sortedResults = new ArrayList<topCategoryResult>();
        for(HashMap hm : topCategories) {
            Long qCount =  (Long) hm.get("qCount");
            String name = (String) hm.get("name");
            sortedResults.add(new topCategoryResult(qCount, name));
        }

        Comparator c = new Comparator<topCategoryResult>() {

            @Override
            public int compare(topCategoryResult r1, topCategoryResult r2) {
                Boolean r1Smaller = r1.qCount < r2.qCount;
                Boolean r1Larger = r1.qCount > r2.qCount;
                return r1Smaller ? -1 : r1Larger ? 1 : 0;
            }
        };

        Collections.sort(sortedResults, Collections.reverseOrder(c));

        List<String> stringListResult = new ArrayList<String>();
        for(topCategoryResult tcr: sortedResults) {
            stringListResult.add(tcr.getName());
        }

        return stringListResult;
    }

    /**
     * The Public questions that have been ACCEPTED
     *
     * @param page
     * @param runLength
     *
     * @return the questions
     */
    public List<Question> getAcceptedPublicQuestions(@Required Integer page, @Required Integer runLength) {

        return Question.find("select q " +
                        "from Question q where " +
                        "q.user = :user and " +
                        "q.status = :acceptedStatus and " +
                        "q.flaggedAsInappropriate = false " +
                        "order by q.standardScore desc")
                        .setParameter("user", this)
                        .setParameter("acceptedStatus", QuestionStatus.ACCEPTED)
                        .fetch(page, runLength);
    }

    /**
     * A count of the questions that at one point have been submitted as public question candidates (QuestionStatus.OUT_FOR_REVIEW)
     *
     * @return count of the questions
     */
    public Long getPublicQuestionSubmissionCount() {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Question> c = countQuery.from(Question.class);
        countQuery.select(cb.count(c));

        Predicate criteria = cb.conjunction();
        criteria = cb.and(criteria, cb.equal(c.get("user"), this));

        Subquery<QuestionWorkflow> subquery = countQuery.subquery(QuestionWorkflow.class);
        Root<QuestionWorkflow> subRootEntity = subquery.from(QuestionWorkflow.class);
        subquery.select(subRootEntity);

        Predicate correlatePredicate = cb.conjunction();
        correlatePredicate = cb.and(correlatePredicate, cb.equal(subRootEntity.get("question"), c));
        correlatePredicate = cb.and(correlatePredicate, cb.equal(subRootEntity.get("status"), QuestionStatus.OUT_FOR_REVIEW));
        subquery.where(correlatePredicate);
        criteria = cb.and(criteria, cb.exists(subquery));

        countQuery.where(criteria);

        return JPA.em().createQuery(countQuery).getSingleResult();
    }

    /**
     * The user's displayName if they have one, otherwise the fullName
     *
     * @return the value to display for this user in all public settings
     */
    public String getPublicDisplayName() {
        return displayName != null ? displayName : fullName;
    }

    /**
     * Get a display name that is suitable for the user himself.  Just like the public getPublicDisplayName but also
     * includes email address if there is no public display name value
     * @return
     */
    public String getPrivateDisplayName() {
        String publicName = getPublicDisplayName();
        return publicName != null ? publicName : email;
    }

    public List<Category> getReviewerOverrideCategories(boolean allowed) {
        List<Category> resultOverrideCategories = new ArrayList<Category>();
        for (CategoryOverride categoryOverride : categoryOverrides) {
            if (categoryOverride.reviewerAllowed != null && categoryOverride.reviewerAllowed == allowed) {
                resultOverrideCategories.add(categoryOverride.category);
            }
        }
        return resultOverrideCategories;
    }

    /**
     *
     * @param allowed fetch categories based on whether the user is allowed or disallowed
     * @return a list of allowed or disallowed categories
     */
    public List<Category> getProInterviewerOverrideCategories(boolean allowed) {
        List<Category> resultOverrideCategories = new ArrayList<Category>();
        for (CategoryOverride categoryOverride : categoryOverrides) {
            if (categoryOverride.proInterviewerAllowed != null && categoryOverride.proInterviewerAllowed == allowed) {
                resultOverrideCategories.add(categoryOverride.category);
            }
        }
        return resultOverrideCategories;
    }

    @Override
    public List<? extends Role> getRoles() {
        return roles;
    }

    public List<String> getRoleNames() {
        List<String> result = new ArrayList<String>();
        for (RoleValue roleValue : roles) {
            result.add(roleValue.getRoleName());
        }
        return result;
    }

    public boolean hasRole(RoleValue role) {
        return roles.contains(role);
    }

    /**
     * Determines if the User has at least one of the given roles
     * @param roleValues RoleValues to check
     * @return whether the user has at least one of the given roles
     */
    public boolean hasOneOf(RoleValue... roleValues){
        for (RoleValue roleValue : roleValues) {
            if(roles.contains(roleValue)){
                return true;
            }
        }

        return false;
    }

    /**
     * a user has access to another user if he is an APP_ADMIN or a COMPANY_ADMIN for the same company as this user
     *
     * @param user
     * @return
     */
    @Override
    public boolean hasAccess(User user) {
        if (user.hasRole(RoleValue.APP_ADMIN)) {
            return true;
        } else if (user.hasRole(RoleValue.COMPANY_ADMIN)) {
            if (user.company != null && user.company.equals(this.company)) {
                return true;
            }
        }
        return (user.id.equals(this.id));
    }

    /**
     * The list of roles that this user can assign to other users during the user creation process
     *
     * @return a List of RoleValues - potentially empty but should never be null
     */
    public List<RoleValue> getRolesAvailableToAssign() {
        List<RoleValue> values = new ArrayList<RoleValue>();

        if (this.hasRole(RoleValue.APP_ADMIN)) {
            values.add(RoleValue.APP_ADMIN);
        }

        //company admins can assign everything except for app admin
        if (this.hasRole(RoleValue.APP_ADMIN) || this.hasRole(RoleValue.COMPANY_ADMIN)) {
            values.add(RoleValue.COMPANY_ADMIN);
            values.add(RoleValue.QUESTION_ENTRY);
        }

        return values;
    }
    
    /**
     * Get a list of badges that have not yet been earned sorted with closest to
     * earned (highest progress value) at the front of the list.
     * 
     * @param limit
     * @return List of badges
     */
    public List<UserBadge> getNearlyEarnedBadges(Integer limit){
    	List<UserBadgeFilter> filters = new ArrayList<UserBadgeFilter>();
    	ByUser byUser = new ByUser();
    	byUser.include(new Long(id).toString());
    	ByMultiplier byMultiplier = new ByMultiplier();
    	byMultiplier.include("0");
    	filters.add(byUser);
    	filters.add(byMultiplier);
    	
    	QueryBase query = 
    			new QueryFilterListBinder<UserBadge, UserBadgeFilter>(
    					UserBadgeQuery.getDefaultQuery(),
    					filters);
    	query.init(3, "desc", 0, limit);
    	
    	QueryResult result = query.executeQuery();
    	return result.getList();
    }
    
    private QueryBase buildEarnedBadgesQuery(){
    	List<UserBadgeFilter> filters = new ArrayList<UserBadgeFilter>();
    	ByUser byUser = new ByUser();
    	byUser.include(new Long(id).toString());
    	ByMultiplier byMultiplier = new ByMultiplier();
    	byMultiplier.exclude("0");
    	filters.add(byUser);
    	filters.add(byMultiplier);

    	return
    			new QueryFilterListBinder<UserBadge, UserBadgeFilter>(
    					UserBadgeQuery.getDefaultQuery(),
    					filters);
    }

    /**
     * Get the number of badges that have been earned sorted with most recently
     * earned at the front of the list.
     *
     * @return The number of earned badges
     */
    public Integer getEarnedBadgesCount(){
    	QueryBase query = buildEarnedBadgesQuery();

    	return (int) query.executeCountQuery();
    }

    /**
     * Get a list of badges that have been earned sorted with most recently
     * earned at the front of the list.
     *
     * @param limit
     * @return List of badges
     */
    public List<UserBadge> getEarnedBadges(Integer limit){
    	QueryBase query = buildEarnedBadgesQuery();

    	query.init(4, "desc", 0, limit);

    	return query.executeQuery().getList();
    }

    /**
     * Sync user badges with the db state
     */
    public void updateBadges(){
    	UserBadgeManager manager = new UserBadgeManager();
    	BadgeProgressEventBuilder transformer = new BadgeProgressEventBuilder();
    	List<BadgeProgressEvent> events = new ArrayList<BadgeProgressEvent>();
    	
    	events.addAll(transformer.assessEntity(this));

    	events.addAll(transformer.assessEntity(metrics));

    	if(questionsCreated != null){
    		for(Question question : questionsCreated){
    			events.addAll(transformer.assessEntity(question));
    		}
    	}

    	if(categoriesCreated != null){
    		for(Category category : categoriesCreated){
    			events.addAll(transformer.assessEntity(category));
    		}
    	}
    	
    	for(BadgeProgressEvent event : events){
    		manager.updateBadges(event);
    	}

    	List<UserBadge> record = manager.getRecord();
    	Iterator<UserBadge> it = badges.iterator();
    	while(it.hasNext()){
    		UserBadge badge = it.next();
    		if(!record.contains(badge)){
    			it.remove();
    			badge.delete();
    		}
    	}
    }

    /**
     * Create the badge if it doesn't already exist
     *
     * @param badgeName
     */
    public void addBadge(String badgeName, int multiplier, int progress, List<Long> associatedEntityIds){
    	UserBadge existing = null;
    	if((associatedEntityIds == null) || associatedEntityIds.isEmpty()){
    		existing = UserBadge.findFirst("byName", badgeName);
    		associatedEntityIds = null;
    	}else{
    		existing = UserBadge.findFirst("byNameAndSubjectIds",
    				badgeName,
    				StringUtils.join(associatedEntityIds, ','));
    	}
    	if(existing == null){
    		UserBadge badge = new UserBadge(badgeName, multiplier, progress, this, associatedEntityIds).save();
			badges.add(badge);
    	}
    }

    /**
     * Create a new badge or update the existing one
     *
     * @param badgeName
     */
    public UserBadge addOrUpdateBadge(String badgeName, int multiplier, int progress, List<Long> associatedEntityIds){
    	UserBadge existing = null;
    	if((associatedEntityIds == null) || associatedEntityIds.isEmpty()){
    		associatedEntityIds = null;
    		existing = UserBadge.findFirst("byNameAndUser", badgeName, this);
    	}else{
    		existing = UserBadge.findFirst("byNameAndSubjectIdsAndUser",
    				badgeName,
    				StringUtils.join(associatedEntityIds, ','),
    				this);
    	}
    	if(existing != null){
    		if(multiplier > existing.multiplier){
    			existing.earned = new Date();
    		}
    		if(multiplier == 0){
    			existing.earned = null;
    		}
    		existing.multiplier = multiplier;
    		existing.progress = progress;
    		return existing;
    	}else{
    		UserBadge badge = new UserBadge(badgeName, multiplier, progress, this, associatedEntityIds);
			badges.add(badge);
			return badge;
    	}
    }

    /**
     * Remove the specified badge and save the user.
     *
     * @param badgeName
     */
    public void removeBadge(String badgeName){
    	Iterator<UserBadge> it = badges.iterator();
    	while(it.hasNext()){
    		UserBadge badge = it.next();
    		if(badge.name.equals(badgeName)){
    			badge.delete();
    			it.remove();
    			break;
    		}
    	}
    }

    public boolean hasReviewCapability() {
        return superReviewer || reviewCategories.size() > 0;
    }

    public boolean hasPicture(){

        try {
            return picture != null;
        } catch (IllegalArgumentException e) {
            Logger.info("This exception is expected if the User's picture is not defined", e);
            return false;
        } catch (AmazonS3Exception ase) {
            return !(ase.getMessage().contains("Not Found"));
        }
    }

    public boolean hasThumbnail(){

        try {
            return thumbnail != null;
        } catch (IllegalArgumentException e) {
            Logger.info("This exception is expected if the User's picture is not defined", e);
            return false;
        } catch (AmazonS3Exception ase) {
            return !(ase.getMessage().contains("Not Found"));
        }
    }

    @Override
    public boolean isDeletable() {
        //user is deletable if it is not associated with any questions or interviews
        Long interviewCount = Interview.count("byUser", this);
        Long questionCount = Question.count("byUser", this);
        Long candidateCount = Candidate.count("byCreator", this);
        return interviewCount + questionCount + candidateCount == 0;
    }

    public static String generateInvitationKey() {
        String value = RandomStringUtils.randomAlphanumeric(INVITATION_KEY_LENGTH);
        if (User.count("byInvitationKey", value) > 0) {
            generateInvitationKey();
        }
        return value;
    }

    public static FieldDisplayMode getFieldDisplayMode(String fieldName,
    		User displayUser, User connectedUser)
    				throws NoSuchFieldException {

    	Field field = User.class.getField(fieldName);
    	Access access = field.getAnnotation(Access.class);

    	if (access == null) {
    		throw new RuntimeException("Cannot display user field.  Field \"" +
    				fieldName + "\" does not have @Access annotation.");
    	}

    	boolean mutate = displayUser.canAccess(connectedUser,access.mutability());
    	boolean view = displayUser.canAccess(connectedUser,access.visibility());

    	FieldDisplayMode result = FieldDisplayMode.HIDE;

    	if (view) {
    		result = FieldDisplayMode.VIEW;
    	}

    	if (mutate) {
    		result = FieldDisplayMode.EDIT;
    	}

    	return result;
    }

    public class CompanyBinder implements TypeBinder<Company> {

        public Object bind(String name, Annotation[] annotations, String value,
                    Class actualClass, Type genericType) throws Exception {

            Company company = null;
            if(StringUtils.isNotEmpty(value)){
                company = Company.find("byName", value.trim()).first();

                if (company == null) {
                    Validation.addError("company", "user.company.notFound");
                    Validation.keep();
                }
            }

            return company;
        }

    }

    public class BlankIndicatesNoChangeStringBinder
            implements TypeBinder<String> {

        public Object bind(String name, Annotation[] annotations, String value,
                    Class actualClass, Type genericType) throws Exception {

            if (StringUtils.isEmpty(value)) {
                throw new ExistingValueShouldBePreservedException();
            }

            return value;
        }
    }

    public class RoleBinder implements TypeBinder<List<RoleValue>> {

        public Object bind(String name, Annotation[] annotations, String value,
                Class actualClass, Type genericType) throws Exception {

            List<RoleValue> allowedRoles = null;
            User user = Security.connectedUser();

            //If we're loading fixtures in dev mode there won't be a connected user
        	//so we just allow all
            boolean allowAll = (user == null) && (play.Play.mode.equals(play.Play.Mode.DEV));

            allowedRoles = (user == null)?new ArrayList<RoleValue>():user.getRolesAvailableToAssign();

            if(StringUtils.isNotEmpty(value)){
                RoleValue intendedRole =
                        RoleValue.valueOf(RoleValue.class, value);

                if (allowAll || allowedRoles.contains(intendedRole)) {
                    return intendedRole;
                }
                else {
                    Validation.addError("roles",
                            "You cannot set that role on a user.");
                    Validation.keep();
                }
            }

            return null;
        }
    }

    public static class CategoryBinder
    	implements TypeBinder<Category> {

    	public Object bind(String name, Annotation[] annotations, String value,
                Class actualClass, Type genericType) throws Exception {
    		Category category = Categories.categoryFromName(value, false);
    		if(category == null){
    			throw new Exception();
    		}
    		return category;
    	}
    }

    public static class CheckboxToBooleanBinder implements TypeBinder<Boolean> {

        public Object bind(String name, Annotation[] annotations, String value,
                    Class actualClass, Type genericType) throws Exception {

            return (value != null && ("on".equals(value.toLowerCase()) || "true".equals(value.toLowerCase())));
        }
    }

    public static class TrimBinder implements TypeBinder<String> {

        public Object bind(String name, Annotation[] annotations, String value,
                    Class actualClass, Type genericType) throws Exception {
            return value.trim();
        }
    }



    /**
     * Deletes any profile picture associated with the user from the S3 storage then uses the superclass delete() to
     * take it from there.
     * @param <T> User being deleted
     * @return The User that was deleted
     */
    @Override
    public <T extends JPABase> T delete() {
        for (Category category : categoriesCreated) {
            for (User user : category.getReviewers()) {
                user.reviewCategories.remove(category);
                user.proInterviewerCategories.remove(category);
                user.save();
            }
        }

        if(hasPicture()){
            this.picture.delete();
        }
        if(hasThumbnail()){
            this.thumbnail.delete();
        }
        return super.delete();
    }



    public void updateStreetCred(){
        double reviewerPointsMultiplier = new Double(Play.configuration.getProperty("streetCred.reviewerPointsMultiplier"));

        //base points are the sum (+/-) of all questions created
        List<Question> questionsCreated = Question.find("byUserAndActiveAndStatusAndFlaggedAsInappropriate",
                this, true, QuestionStatus.ACCEPTED, false).fetch();
        double streetCred = 0;
        for (Question question : questionsCreated) {
            Integer questionScore = question.standardScore;
            if(questionScore != null){ //really this should never be null
                streetCred += questionScore;
            }
        }

        //any questions in which this user has participated in the review give some percentage of points
        List<Question> reviewedQuestions = QuestionWorkflow.find("select distinct question from QuestionWorkflow qw where qw.status IN (?1) AND qw.user = ?2",
                Arrays.asList(QuestionStatus.ACCEPTED, QuestionStatus.RETURNED_TO_SUBMITTER), this).fetch();
        for (Question question : reviewedQuestions) {
            if(!questionsCreated.contains(question) && question.active && !question.flaggedAsInappropriate){
                streetCred += reviewerPointsMultiplier * question.standardScore;
            }
        }

        //deduct for inappropriate questions
        long inappropriateCount = Question.count("byUserAndFlaggedAsInappropriate", this, true);
        streetCred -= new Double(Play.configuration.getProperty("streetCred.inappropriateQuestionDeduction")) * inappropriateCount;

        //category based points
        Long userCategoryCount = Category.count("byCreatorAndStatus", this, Category.CategoryStatus.PUBLIC);
        streetCred += userCategoryCount * new Double(Play.configuration.getProperty("streetCred.publicCategoryPoints"));

        this.streetCred = Math.round(streetCred);
    }

    public int getNumberOfQuestionsReviewed(){
    	return metrics.numberOfQuestionsReviewed;
    }

    public int getNumberOfCandidatesCreated(){
    	return (int) Candidate.count("byCreator",
                this);
    }

    public int getNumberOfInterviews(){
    	return (int) ActiveInterview.count("from ActiveInterview where user = (?1) or interviewer = (?1)",
                this);
    }

	@Override
	public long getResourceUsage(AccountResource resource) {
		if(company != null){
			return company.getResourceUsage(resource);
		}
		switch(resource){
		case PRIVATE_QUESTIONS:
		case INTERVIEWS:
		case CANDIDATES:
		case ADMINISTRATORS:
			return 0L;
		case USERS:
			return 1L;
		default:
			throw new IllegalArgumentException("Unknown account resource");
	}
	}
	
	public void checkWriteAccess(String fieldName) throws NoWritePrivilegesException{
		/*
    	 * Disable write access checks during entity initialization of User object.
    	 */
    	if(email == null){
    		return;
    	}
    	super.checkWriteAccess(fieldName);
    }

	@Override
	public boolean canAccess(User accessingUser, AccessScope scope) {

		if(scope == null){
			return false;
		}

		switch(scope){

		case PUBLIC:
			return true;
		case ALL_USERS:
			return (accessingUser != null);
		case COMPANY_AND_APP_ADMIN:
			return accessingUser != null &&
			(accessingUser.hasRole(enums.RoleValue.APP_ADMIN) ||
					(ObjectUtils.equals(accessingUser.company,
							company)));
		case USER_AND_COMPANY_ADMIN_AND_APP_ADMIN:
			return ObjectUtils.equals(accessingUser, this) ||
					(accessingUser != null && (
							(accessingUser.hasRole(enums.RoleValue.COMPANY_ADMIN) &&
									ObjectUtils.equals(accessingUser.company, 
											company)) ||
											(accessingUser.hasRole(enums.RoleValue.APP_ADMIN))));

		case COMPANY_ADMIN_AND_APP_ADMIN:
			return (accessingUser != null && (
					(accessingUser.hasRole(enums.RoleValue.COMPANY_ADMIN) &&
							ObjectUtils.equals(accessingUser.company, 
									company)) ||
									(accessingUser.hasRole(enums.RoleValue.APP_ADMIN))));			
		case USER_AND_APP_ADMIN:
			return ObjectUtils.equals(accessingUser, this) ||
					(accessingUser != null &&
					accessingUser.hasRole(enums.RoleValue.APP_ADMIN));
		case APP_ADMIN:
			return accessingUser != null && 
			accessingUser.hasRole(enums.RoleValue.APP_ADMIN);
		default: return false;
		}
	}

    /**
     * Gets a temporarily signed and accessible URL for the picture in S3 The url will expire after 30 seconds so this
     * method should only be used if the url will be accessed within that timeframe.  A typical usage would be to use
     * the return value of this method as the src attribute of an HTML img tag
     * @return Temporarily signed URL
     */
    public String getPublicPictureUrl(){
    	if(!hasPicture()){
    		return null;
    	}
    	java.net.URL url = picture.getPublicUrl();
    	return "//" + url.getHost() + url.getPath();
    }

    /**
     * Gets a temporarily signed and accessible URL for the picture in S3 The url will expire after 30 seconds so this
     * method should only be used if the url will be accessed within that timeframe.  A typical usage would be to use
     * the return value of this method as the src attribute of an HTML img tag
     * @return Temporarily signed URL
     */
    public String getPublicPictureThumbnailUrl(){
        if(!hasThumbnail()){
            return null;
        }
        java.net.URL url = thumbnail.getPublicUrl();
        return "//" + url.getHost() + url.getPath();
    }

    public Boolean hasActiveProInterviewRequest() {
        Boolean foundActive = false;
        if (!proInterviewerRequests.isEmpty()) {
            for (ProInterviewerRequest proInterviewerRequest : proInterviewerRequests) {
                if (proInterviewerRequest.status == ProInterviewerRequestStatus.SUBMITTED) {
                    foundActive = true;
                }
            }
        }
        return foundActive;
    }
}
