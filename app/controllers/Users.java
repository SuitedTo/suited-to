package controllers;

import com.google.gson.*;
import controllers.deadbolt.Deadbolt;
import controllers.deadbolt.Restrict;
import controllers.deadbolt.RestrictedResource;
import controllers.deadbolt.Restrictions;
import data.binding.CustomBinding;
import data.binding.types.ImageBinder;
import data.validation.Image;
import data.validation.PasswordCheck;
import db.jpa.S3Blob;
import enums.AccountResource;
import enums.ImageSize;
import enums.RoleValue;
import enums.UserStatus;
import models.Category;
import models.CategoryOverride;
import models.Company;
import models.User;
import models.query.user.AccessibleUsersByPartialEmailOrPartialName;
import notifiers.Mails;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.Play;
import play.data.binding.As;
import play.data.binding.ParamNode;
import play.data.binding.RootParamNode;
import play.data.validation.Error;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.db.jpa.Blob;
import play.db.jpa.JPA;
import play.i18n.Messages;
import play.mvc.With;
import trigger.assessor.UserClassificationListener;
import utils.EscapeUtils;
import utils.ImageUtil;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import java.io.FileInputStream;
import java.lang.reflect.Type;
import java.util.*;

@With(Deadbolt.class)
public class Users extends ControllerBase {

    private static final JsonSerializer USERNAME_SERIALIZER = 
            new UsernameSerializer();
    private static final JsonSerializer EMAIL_SERIALIZER = 
            new EmailSerializer();

    private static final JsonSerializer AUTOCOMPLETE_SERIALIZER =
            new AutoCompleteSerializer();


    private static final Gson USER_AS_USERNAME_RENDERER;
    private static final Gson USER_AS_EMAIL_RENDERER;
    private static final Gson USER_AUTOCOMPLETE_RENDERER;

    static {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(User.class, USERNAME_SERIALIZER);
        
        USER_AS_USERNAME_RENDERER = builder.create();

        builder = new GsonBuilder();
        builder.registerTypeAdapter(User.class, EMAIL_SERIALIZER);
        
        USER_AS_EMAIL_RENDERER = builder.create();

        builder = new GsonBuilder();
        builder.registerTypeAdapter(User.class, AUTOCOMPLETE_SERIALIZER);

        USER_AUTOCOMPLETE_RENDERER = builder.create();
    }

    @RestrictedResource(name = {"models.User"}, staticFallback = true)
    public static void preferences(Long id) {
        
        User user = null;
        if (id == null) {
        	user = Security.connectedUser();
        } else {
        	user = User.findById(id);
        	if(user == null){
        		throw new RuntimeException("No such user.");
        	}
        }
        
        if (!user.id.equals(Security.connectedUser().id)) {
            //For the moment, you can only change preferences on yourself.
            //Eventually, we might like company or SuitedTo admins to be
            //able to change your preference as well
            throw new RuntimeException("User is not the logged in user.");
        }
        
        render(user);
    }
    
    @RestrictedResource(name = {"models.User"}, staticFallback = true)
    public static void updatePreferences(@Required Long id, 
                boolean privacyProfilePicture, boolean privacyBadges,
                boolean privacyCategoryContributions, boolean privacyRoles,
                boolean privacyQuestionSubmissions, 
                boolean privacyQuestionsReviewed, boolean privacyKillSwitch) {
        
        User user = User.findById(id);
        
        //################
        //# Privacy      #
        //################
        
        user.privacyLockdown = privacyKillSwitch;
        
        //If lockdown is on, we ignore the other privacy options, as their value
        //on the form does not represent their true value (they've been cleared 
        //because the kill switch is on.)
        if (!privacyKillSwitch) {
            user.picturePublic = privacyProfilePicture;
            user.badgesPublic = privacyBadges;
            user.contributingCategoriesPublic = privacyCategoryContributions;
            user.statusLevelPublic = privacyRoles;
            user.submittedQuestionsPublic = privacyQuestionSubmissions;
            user.reviewedQuestionsPublic = privacyQuestionsReviewed;
        }
        
        
        user.save();
        Application.home();
    }
    
    /**
     * Displays the form for editing an existing user, creation of new users should use the invitation method
     * @param id
     */
    @RestrictedResource(name = {"models.User"}, staticFallback = true)
    public static void show(@Required Long id, String title){
        final User connectedUser = Security.connectedUser();
        if (id.equals(connectedUser.id)) {
            renderArgs.put("currentSection", null);
        }
        
        if(title != null){
        	renderArgs.put("title", title);
        }else{
        	renderArgs.put("title", "User");
        }
        
        if(validation.hasErrors()){
            list();
        }
        User user = null;
        if(id != null){
            user = User.findById(id);
        }
        List<RoleValue> roleOptions = connectedUser.getRolesAvailableToAssign();
        String existingCategories = user != null ? Categories.getCommaSeparatedCategoryNames(user.reviewCategories) : null;
        String proInterviewerCategories = user != null ? Categories.getCommaSeparatedCategoryNames(user.proInterviewerCategories) : null;
        String reviewerAllowedOverrideCategories = user != null ? Categories.getCommaSeparatedCategoryNames(user.getReviewerOverrideCategories(true)) : null;
        String reviewerDisallowedOverrideCategories = user != null ? Categories.getCommaSeparatedCategoryNames(user.getReviewerOverrideCategories(false)) : null;
        String proInterviewerAllowedOverrideCategories = user != null ? Categories.getCommaSeparatedCategoryNames(user.getProInterviewerOverrideCategories(true)) : null;
        String proInterviewerDisallowedOverrideCategories = user != null ? Categories.getCommaSeparatedCategoryNames(user.getProInterviewerOverrideCategories(false)) : null;
        render(connectedUser, user, roleOptions, existingCategories, proInterviewerCategories,
                reviewerAllowedOverrideCategories, reviewerDisallowedOverrideCategories,
                proInterviewerAllowedOverrideCategories, proInterviewerDisallowedOverrideCategories);
    }


    /**
     * Displays the user invitation form
     */
    @Restrictions({@Restrict(RoleValue.APP_ADMIN_STRING), @Restrict(RoleValue.COMPANY_ADMIN_STRING)})
    public static void invitation(){
        User connectedUser = Security.connectedUser();
        List<RoleValue> roleOptions = connectedUser.getRolesAvailableToAssign();
        render(roleOptions);
    }

    
    @Restrictions({@Restrict(RoleValue.APP_ADMIN_STRING), @Restrict(RoleValue.COMPANY_ADMIN_STRING)})
    public static void massInvite(String emails, String companyName){
        render(emails, companyName);
    }

    /**
     * Sends a user invitation to emails specified in the input textarea.
     * @param emails String of emails separated by newline.
     * @param companyName Name of the company to create invitations for.
     */

    @Restrictions({@Restrict(RoleValue.APP_ADMIN_STRING), @Restrict(RoleValue.COMPANY_ADMIN_STRING)})
    public static void sendMassInvitation(@Required final String emails, final String companyName) {
        User connectedUser = Security.connectedUser();
        Company company = null;
        if(StringUtils.isNotEmpty(companyName)){
            company = Company.find("byName", companyName).first();
            validation.required(company).message("user.company.notFound");
        } else if(connectedUser.company != null){
            company = connectedUser.company;
        }

        List<String> userErrors = new ArrayList<String>();
        String lines[] = emails.split("[,;\\r?\\n]");
        
        List<String> emailList = new LinkedList<String>();
        for (String line : lines) {
            if (!StringUtils.isEmpty(line)) {
                emailList.add(line);
            }
        }
        
        if ((company.accountType.getMax(AccountResource.USERS) != null) &&
                (emailList.size() > company.accountType.getMax(AccountResource.USERS) - 
                company.getUsers().size())) {
            
            validationError(Messages.get("limits.users.cannot_create"));
            Validation.keep();
            massInvite(emails, companyName);
        }
        
        int userCount = 0;
        for (String line : emailList) {
            if(StringUtils.isEmpty(line)){
                continue;
            }
            User user = new User(connectedUser.company);
            user.company = company;
            /** These fields have to be set after the user's company
             to satisfy User.canAccess(). If the user's company is null,
             ObjectUtils.equals(accessingUser.company, company) returns
             false, and write access is denyed.
             TODO: Discuss how User.canAccess() can be improved
             to deal with access during user creation.
             **/
            user.roles.add(RoleValue.QUESTION_ENTRY);
            user.email = line.trim();
            user.fullName = user.email.substring(0,user.email.indexOf('@'));
            user.invitationKey = User.generateInvitationKey();
            Validation.clear();
            Validation.ValidationResult result = validation.valid(user);
            if(result.ok){
                try {
                    Mails.invitation(connectedUser, user, request.host, null);
                    connectedUser.metrics.incrementNumberOfUsersInvited();
                    connectedUser.metrics.save();
                    user.save();
                    userCount++;
                } catch (Exception e) {
                    StringBuilder errorBuilder = new StringBuilder("Failed to send email to ");
                    errorBuilder.append(user.email);
                    errorBuilder.append(". The user was not created. Please try again.");

                    userErrors.add(user.email);
                }
            } else {
                StringBuilder errorBuilder = new StringBuilder("Invitation could not be sent to user: ");
                errorBuilder.append(user.email);
                errorBuilder.append(" for the following reasons: ");
                for (Error error : validation.errors()) {
                    if(!error.getKey().equals("user")){
                        errorBuilder.append(error.message());
                    }
                }
                userErrors.add(errorBuilder.toString());
            }
        }


        render("@list", userCount, userErrors);
    }


    @Restrictions({@Restrict(RoleValue.APP_ADMIN_STRING), @Restrict(RoleValue.COMPANY_ADMIN_STRING)})
    @RestrictedResource(name = {"models.User"}, staticFallback = true)
    public static void resendInvitation(Long id){
        User user = User.findById(id);
        if(UserStatus.INVITATION_WITHDRAWN.equals(user.status)){
            user.status = UserStatus.PENDING;
            user.save();
        }

        Mails.invitation(Security.connectedUser(), user, request.host, null);
        String confirmationEmail = user.email;
        render("@list", confirmationEmail);
    }

    public static void invite(String fullName, String email, 
            @Required String userRole, String invitationMessage, 
            String companyName) {
        
        User connectedUser = Security.connectedUser();
        User user = new User(connectedUser.company);

        if (!enums.AccountLimitedAction.CREATE_USER.canPerform(
                        connectedUser)) {

            validationError(Messages.get("limits.users.cannot_create"));
        }
        
        if(StringUtils.isNotEmpty(userRole)){
            RoleValue role = RoleValue.valueOf(RoleValue.class, userRole);
            
            if (connectedUser.getRolesAvailableToAssign().contains(role)) {
                user.roles.add(role);
            }
            else {
                throw new RuntimeException("Can't set the role: " + userRole);
            }
        }
        
        Company c;
        if (StringUtils.isEmpty(companyName)) {
            c = connectedUser.company;
        }
        else {
            if (!connectedUser.hasRole(RoleValue.APP_ADMIN)) {
                throw new RuntimeException("Non app admins can't select a " +
                        "company.");
            }
            
            c = Company.find("byName", companyName).first();
            validation.required(c).message("user.company.notFound");
        }
        user.company = c;

        /** These fields have to be set after the user's company
        to satisfy User.canAccess(). If the user's company is null,
         ObjectUtils.equals(accessingUser.company, company) returns
         false, and write access is denyed.
         TODO: Discuss how User.canAccess() can be improved
         to deal with access during user creation.
        **/
        user.fullName = fullName;
        user.email = email;

        validation.valid(user);
        if (Validation.hasErrors()) {
            List<RoleValue> roleOptions = 
                    connectedUser.getRolesAvailableToAssign();
            String entryForm = "@invitation"; 
            renderTemplate(entryForm, user, connectedUser, roleOptions, invitationMessage);
        }
        
        String invitationKey = User.generateInvitationKey();
        user.invitationKey = invitationKey;
        Mails.invitation(connectedUser, user, request.host, invitationMessage);
        
        connectedUser.metrics.incrementNumberOfUsersInvited();
        connectedUser.metrics.save();
        
        user.save();
        
        String confirmationEmail = email;
        render("@list", confirmationEmail);
    }
    
    @RestrictedResource(name = {"models.User"}, staticFallback = true)
    public static void save(Long id,
                            String invitationMessage,
                            String includedFields,
                            String password,
                            String confirmPassword,
                            String email) {

        User connectedUser = Security.connectedUser();

        User user;
        if(id != null){
            user = User.findById(id);
        } else {
            user = new User(connectedUser.company);
        }        


        if(StringUtils.isNotEmpty(password) || 
                StringUtils.isNotEmpty(confirmPassword)){

            validation.equals(password, confirmPassword)
                    .message("password.confirmation.doNotMatch")
                    .key("password");
            
            if (password != null) {
                validation.range(password.length(), PasswordCheck.MIN, 
                        PasswordCheck.MAX).message(
                            Messages.get("validation.password", "", 
                                "Password", PasswordCheck.MIN, 
                                PasswordCheck.MAX));
            }
        }
        
        RootParamNode root = params.getRootParamNode();
        ParamNode userNode = new ParamNode("user");
        root.addChild(userNode);


        /*yep, this is ugly, but we need an actual arrayList instead of the AbstractList implementation that the Java
        gods decided to bless us with so that we can remove the password element from the list if necessary*/
        Set<String> includedFieldsSet = new HashSet<String>(Arrays.asList(includedFields.split(" ")));

        /*if nothing was submitted for the password then remove "password" from includedFields.  This allows the user
        to leave the password fields on the page empty if they want to update other information but not change their
        password*/
        if(StringUtils.isEmpty(password)){
            includedFieldsSet.remove("password");
        }

        String stringValue;
        Map<String, String[]> allParams = params.all();
        for (String field : includedFieldsSet) {
            if (!StringUtils.isEmpty(field)) {
                stringValue = firstOrNothing(allParams.get(field));
                
                if(stringValue != null){
                	stringValue = stringValue.trim();
                }
                
                if(field.equals("status")){
                	boolean active = stringValue != null && 
                            "on".equalsIgnoreCase(stringValue.toLowerCase());
                    
                	stringValue = active ? UserStatus.ACTIVE.name() :
                		UserStatus.DEACTIVATED.name();
                }
                
                if(field.equals("roles")){
                	List<RoleValue> allowedRoles = 
                            connectedUser.getRolesAvailableToAssign();
                    
                    List<String> result = new LinkedList<String>();
                    
                    if(StringUtils.isNotEmpty(stringValue)){
                        RoleValue intendedRole = 
                                RoleValue.valueOf(RoleValue.class, stringValue);

                        if (user.hasRole(RoleValue.COMPANY_ADMIN) &&
                                user.company.getAdministrators().size() == 1 &&
                                !intendedRole.equals(RoleValue.COMPANY_ADMIN)) {
                            //We're trying to change the company's only admin
                            //away from admin--not allowed!
                            Validation.addError("roles", 
                                    "You cannot remove a company's last admin.");
                            Validation.keep();
                        }
                        else if (allowedRoles.contains(intendedRole)) {
                            result.add(intendedRole.name());
                        }
                        else {
                            Validation.addError("roles", 
                                    "You cannot set that role on a user.");
                            Validation.keep();
                        }
                    }
                    
                    stringValue =  StringUtils.join(result, ',');
                }
                
                if(field.equals("categoryOverrides")) {
                    // don't add these, because we need to manual add it to the user rather than having a binder do it
                    // the tag is generated by userProperty.html, and needed for now to include autocomplete JS magic
                    // and permission logic
                    continue;
                } else if (field.equals("company") && stringValue.trim().isEmpty()) {
                    // with our new registration, company name can be null here which throws off the binder
                    // we should ignore it unless it has been set
                    continue;
                }
                

                ParamNode node = new ParamNode(field);
                node.setValue(new String[]{stringValue}, field);
                userNode.addChild(node);
            }
        }

        CustomBinding.bindBean(root, "user", user);

        validation.valid(user);
        if (Validation.hasErrors()) {
            List<RoleValue> roleOptions = 
                    connectedUser.getRolesAvailableToAssign();
            String entryForm = "@show"; 
            renderTemplate(entryForm, user, connectedUser, roleOptions);
        }

        //build the lists of categories that are explicitly allowed or disallowed from the form
        if (Security.connectedUser().getRoles().contains(RoleValue.APP_ADMIN)) {
            List<Category> reviewerAllowedOverrideCategories =
                    Categories.categoriesFromStandardRequestParam(allParams.get("reviewerAllowedOverrideCategories")[0], false);
            List<Category> reviewerDisallowedOverrideCategories =
                    Categories.categoriesFromStandardRequestParam(allParams.get("reviewerDisallowedOverrideCategories")[0], false);
            List<Category> proInterviewerAllowedOverrideCategories =
                    Categories.categoriesFromStandardRequestParam(allParams.get("proInterviewerAllowedOverrideCategories")[0], false);
            List<Category> proInterviewerDisallowedOverrideCategories =
                    Categories.categoriesFromStandardRequestParam(allParams.get("proInterviewerDisallowedOverrideCategories")[0], false);

            Set<Category> categoriesToAdd = new HashSet<Category>();

            Map<Category, Boolean> reviewerOverrides = new HashMap<Category, Boolean>();
            for (Category category : reviewerAllowedOverrideCategories) {
                categoriesToAdd.add(category);
                reviewerOverrides.put(category, true);
            }
            for (Category category : reviewerDisallowedOverrideCategories) {
                categoriesToAdd.add(category);
                reviewerOverrides.put(category, false);
            }

            Map<Category, Boolean> proInterviewerOverrides = new HashMap<Category, Boolean>();
            for (Category category : proInterviewerAllowedOverrideCategories) {
                categoriesToAdd.add(category);
                proInterviewerOverrides.put(category, true);
            }
            for (Category category : proInterviewerDisallowedOverrideCategories) {
                categoriesToAdd.add(category);
                proInterviewerOverrides.put(category, false);
            }

            List<CategoryOverride> newCategoryOverrides = new ArrayList<CategoryOverride>();
            for (Category category : categoriesToAdd) {
                newCategoryOverrides.add(new CategoryOverride(user, category, reviewerOverrides.get(category), proInterviewerOverrides.get(category)));
            }

            user.categoryOverrides.clear();
            user.categoryOverrides.addAll(newCategoryOverrides);

            UserClassificationListener.rebuildClassificationLists(user);
        }

        //then save the user which cascades saves to the category overrides
        user.save();

        //If the user updated himself, preserve his session
        if (connectedUser == user) {
            session.put("username", user.email);
            session.put("prettyusername", user.email);
        }


        if (connectedUser == user) {
            Application.home();
        }
        else {
            render("@list");
        }
    }
    
    /**
     * <p>Takes a partial user name and renders a JSON object containing either
     * a field called "users" that maps to a list of full user names resembling
     * the given name and belonging to the company of the currently 
     * logged-in user, or a field called "error" that contains a human-readable
     * error message.</p>
     */
    @Restrictions({@Restrict(RoleValue.APP_ADMIN_STRING), @Restrict(RoleValue.COMPANY_ADMIN_STRING)})
    public static void getUsersForUserCompanyByPartialName(
                @Required String partialName) {

        Map<String, Object> result = new HashMap<String, Object>();
        
        try {
            List<User> users = 
                    doGetUsersForUserCompanyByPartialName(partialName);
            result.put("users", users);
        }
        catch (RuntimeException re) {
            result.put("error", re.getMessage());
        }
        
        renderJSON(USER_AS_USERNAME_RENDERER.toJson(result));
    }


    @Restrictions({@Restrict(RoleValue.APP_ADMIN_STRING), @Restrict(RoleValue.COMPANY_ADMIN_STRING)})
    public static void getUsersForUserCompanyByPartialEmailOrPartialName(@Required String q) {

        String[] searchTerms = q.split(", ");

        for (int i = 0; i < searchTerms.length; i++) {
            searchTerms[i] = EscapeUtils.safeSQLLikeString(searchTerms[i]);
        }

        if (searchTerms.length == 1) {
            q = searchTerms[0];
        }
        q = searchTerms.length > 1 ? searchTerms[searchTerms.length -1] : q;

        AccessibleUsersByPartialEmailOrPartialName query = new AccessibleUsersByPartialEmailOrPartialName(q);
        CriteriaQuery cq = query.finish();
        TypedQuery tq = JPA.em().createQuery(cq);
        tq.setFirstResult(0);
        tq.setMaxResults(Integer.valueOf(Play.configuration.getProperty("default.query.max")));
        tq.setHint("org.hibernate.cacheable", true);

        List<User> users = tq.getResultList();

        renderJSON(USER_AUTOCOMPLETE_RENDERER.toJson(users));
    }

    /**
    * <p>Takes a partial user email and renders a JSON object containing either
    * a field called "emails" that maps to a list of full user emails
    * resembling the given email and belonging to the company of the currently
    * logged-in user, or a field called "error" that contains a human-readable
    * error message.</p>
    */
    @Restrictions({@Restrict(RoleValue.APP_ADMIN_STRING), @Restrict(RoleValue.COMPANY_ADMIN_STRING)})
    public static void getUsersForUserCompanyByPartialEmail(
                @Required String partialName) {
        
        Map<String, Object> result = new HashMap<String, Object>();
        
        try {
            List<User> email = 
                    doGetUsersForUserCompanyByPartialName(partialName);
            result.put("emails", email);
        }
        catch (RuntimeException re) {
            result.put("error", re.getMessage());
        }
        
        renderJSON(USER_AS_EMAIL_RENDERER.toJson(result));
    }





    private static List<User> doGetUsersForUserCompanyByPartialEmail(
                String partialEmail) {
        
        Company company = getUserCompany();
        
        List<User> results = User.find("byCompanyAndEmailIlike", company, 
                "%" + partialEmail + "%").fetch();
        
        return results;
    }
    
    private static List<User> doGetUsersForUserCompanyByPartialName(
                String partialName) {
        
        Company company = getUserCompany();
        
        List<User> results = User.find("byCompanyAndFullnameIlike", company, 
                "%" + partialName + "%").fetch();
        
        return results;
    }

    /**
    * Upload a profile picture for the connected user
    * todo: ideally this would be taking a File argument instead of blob, but I haven't modified the binder yet
    * @param picture The picture.
    */
    @RestrictedResource(name = {"models.User"}, staticFallback = true)
    public static void uploadPicture(
                Long id,
                @Required
                @As(binder=ImageBinder.class)
                @Image(alias="Profile picture",typeCheckOnly=true)
                Blob picture) {

        if(validation.hasErrors()){
            flash.error("Error uploading picture.");
            show(id, null);
        }

        Blob thumbnail = new Blob();
        thumbnail.set(ImageUtil.scaleAndCrop(picture.get(), picture.type(), ImageSize.THUMBNAIL), picture.type());
        picture.set(ImageUtil.scale(picture.get(), picture.type(), ImageSize.PROFILE), picture.type());

        try {
            S3Blob s3ProfilePicBlob = new S3Blob(true);
            S3Blob s3ThumbnailBlob = new S3Blob(true);
            await(s3ProfilePicBlob.setAsJob(new FileInputStream(picture.getFile()), picture.type()).now());
            await(s3ThumbnailBlob.setAsJob(new FileInputStream(thumbnail.getFile()), thumbnail.type()).now());

            User user = User.findById(id);
            user.picture = s3ProfilePicBlob;
            user.thumbnail = s3ThumbnailBlob;
            user.save();

        } catch (Exception e) {
            flash.error("picture.upload.error");
            Logger.error("Unable to upload picture. %s", e.getMessage());
        }

        User user = Security.connectedUser();
        show(id, null);
    }

    /**
    * Delete the profile picture for the given user.  Removes the picture from 
    * S3 bucket and then sets the reference on the User object to null.
    */
    @RestrictedResource(name = {"models.User"}, staticFallback = true)
    public static void deletePicture(Long id) {
        User user = User.findById(id);
        if(user.hasPicture()){
            user.picture.delete();
        }
        user.picture = null;

        user.save();

        show(id, null);
    }
	
    public static void getPicture(Long id){
        User user = User.findById(id);
        notFoundIfNull(user);

        if(user.hasPicture()){
            S3Blob picture = user.picture;
            response.setContentTypeIfNotSet(picture.type());
            renderBinary(picture.get());
        }
    }
    
    @Restrictions({@Restrict(RoleValue.APP_ADMIN_STRING), @Restrict(RoleValue.COMPANY_ADMIN_STRING)})
    public static void list(){
        render();
    }

    public static void cancelEdit(){
        list();
    }

    
    @Restrictions({@Restrict(RoleValue.APP_ADMIN_STRING), @Restrict(RoleValue.COMPANY_ADMIN_STRING)})
    @RestrictedResource(name = {"models.User"}, staticFallback = true)
    public static void delete(Long id){
        User user = User.findById(id);
        user.delete();
        list();
    }

    /**
     * Indicates that the current user has seen the system introduction tutorial. Intended to be called via AJAX will
     * render a success message as JSON data
     */
    public static void markIntroductionAsViewed(){
        User user = Security.connectedUser();
        user.hasViewedIntroduction = true;
        user.save();
        renderJSON(buildSuccessResponseMap());
    }


    private static String firstOrNothing(String[] array) {
        String result;
        
        if (array == null) {
            result = null;
        }
        else {
            result = array[0];
        }
        
        return result;
    }
    
    private static class UsernameSerializer implements JsonSerializer<User> {

        public JsonElement serialize(User t, Type type, 
                    JsonSerializationContext jsc) {
            return new JsonPrimitive(t.fullName);
        }
    }
    
    private static class EmailSerializer implements JsonSerializer<User> {

        public JsonElement serialize(User t, Type type, 
                    JsonSerializationContext jsc) {
            return new JsonPrimitive(t.email);
        }
    }

    private static class AutoCompleteSerializer implements JsonSerializer<User> {

        public JsonElement serialize(User t, Type type,
                    JsonSerializationContext jsc) {
            JsonObject result = new JsonObject();
            result.add("label", new JsonPrimitive(t.fullName));
            return result;
        }
    }
}
