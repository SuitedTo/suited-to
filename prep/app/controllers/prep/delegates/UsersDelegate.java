package controllers.prep.delegates;

import data.validation.PasswordCheck;
import dto.prep.PrepAccountUpdateDTO;
import dto.prep.PrepCredentialsDTO;
import dto.prep.PrepRegistrationDTO;
import dto.prep.PrepUserDTO;
import errors.prep.PrepError;
import errors.prep.PrepErrorResult;
import errors.prep.PrepErrorType;
import exceptions.LoginException;
import models.prep.PrepUser;
import models.prep.PrepUserRole;
import models.prep.PrepUser.RoleValue;

import org.apache.commons.lang.StringUtils;
import play.Play;
import play.data.validation.Email;
import play.data.validation.Validation;
import play.i18n.Messages;
import play.libs.F.Promise;
import play.mvc.Controller;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Delegate for interacting with PrepUser entities.  Note that PrepUser is completely separate from the base User
 * entities. This is to ensure complete separation between the user bases and improve clarity within the Prep and
 * SuitedTo applications
 */
public class UsersDelegate{

    /**
     * Lookup a PrepUser with the given Id and translate into a PrepUserDTO prior to returning
     * @param id primary key of the PrepUser to get
     * @return PrepUserDTO
     */
    public static Promise<PrepUserDTO> get(Long id){
        return PrepUserDTO.fromModel(PrepUser.find.byId(id));
    }

    /**
     *
     */
    public static PrepAccountUpdateDTO update(Long id, String firstName, String email, String password, String stripeId) {

        if(password != null && password.length() < 6) {
            ErrorHandler.emitCustomErrorResult("prep.error.user.passwordLength");
        }

        PrepUser prepUser = PrepUser.find.byId(id);
        if(prepUser == null) {
            ErrorHandler.emitCustomErrorResult("prep.error.user.notFound", id.toString());
        }

        boolean emailTaken = false;
        List<PrepUser> prepUsers = PrepUser.find.where().eq("email", email).findList();

        switch(prepUsers.size()) {
            case 0:
                emailTaken = false;
                break;
            case 1:
                emailTaken = !prepUsers.get(0).equals(prepUser);
                break;
            default:
                break;
        }
        if(emailTaken) {
            ErrorHandler.emitCustomErrorResult("prep.error.user.extantEmail", email);
        }

        if(firstName != null) {
            prepUser.firstName = firstName;
        }
        if(email != null) {
            prepUser.email = email;
        }
        if(password != null) {
            prepUser.setPassword(password);
        }
        if(stripeId != null) {
            prepUser.stripeId = stripeId;
        }
        prepUser.save();

        return PrepAccountUpdateDTO.fromModel(prepUser);
    }

    /**
     * Logs a user into the system by running the appropriate PrepUser.connect() implementation and updates the last
     * login date for the user upon successful login.  Note that this handles the data model aspects of the User login
     * but does not actually create an authenticated session.  Its up to the caller to use the returned PrepUserDTO to
     * actually create an authenticated session
     * @param credentials Information to use to try and connect the user
     * @return PrepUserDTO
     * @throws PrepErrorResult if there is any sort of problem logging in including not being able to locate a user with
     * the given credentials
     */
    public static Promise<PrepUserDTO> login(PrepCredentialsDTO credentials) {
        Promise<PrepUserDTO> result = null;
        if(credentials.email != null) {
            PrepUser tempUser = PrepUser.findByEmail(credentials.email);
            if(tempUser != null){
                //If the temp password of the user equals the typed password, and the expire date is after the current time:
                if(tempUser.tempExpire != null && tempUser.tempPassword != null){
                    if(tempUser.tempPassword.equals(tempUser.hashPassword(credentials.password)) && tempUser.tempExpire.after(new Date())){
                        ErrorHandler.emitCustomErrorResult("prep.error.login.tempPassword");
                    }
                }
            }
        }
        try {
            PrepUser user = null;
            if (StringUtils.isNotEmpty(credentials.externalAuthProviderId)) {
                user = PrepUser.connect(credentials.externalAuthProviderId, PrepUser.AuthProvider.valueOf(credentials.externalAuthProvider),
                		credentials.externalAuthProviderAccessToken, credentials.externalAuthProviderAccessTokenSecret, credentials.email);
            } else {
                user = PrepUser.connect(credentials.email, credentials.password);
            }

            //update the last login timestamp
            user.lastLogin = new Date();
            user.save();

            result = PrepUserDTO.fromModel(user);
        } catch (LoginException e) {
        	
        	PrepErrorType type = e.getErrorType();
            if(type != null){
            	ErrorHandler.emitCustomErrorResult(type, e.getMessage());
            }
            if(credentials.externalAuthProviderId != null){
                ErrorHandler.emitCustomErrorResult("prep.error.login.external", credentials.externalAuthProvider);
            }
            
        	ErrorHandler.emitCustomErrorResult("prep.error.login.email");
        
        } catch (PrepUser.ConflictingUserException e){
            if(e.conflictingAuthProvider != null){
                ErrorHandler.emitCustomErrorResult("prep.error.login.externalEmailConflict", e.conflictingAuthProvider.name());
            } else {
                ErrorHandler.emitCustomErrorResult("prep.error.login.externalEmailConflictNoProvider");
            }
        }
        return result;
    }

    /**
     * Creates a new PrepUser which is a completely separate thing from a models.User with the given credentials and
     * demographic information contained in the PrepRegistrationDTO.  The PrepUserDTO returned should have a populated
     * id and therefore can be passed to SecurityUtil.createAuthenticatedSession() to actually log the newly created
     * user into the system
     * @param registrationData Information from which to create a new PrepUser
     * @return PrepUserDTO
     * @throws PrepErrorResult via the ErrorHandler if there is some problem with creating the new user such as
     * duplicate email address or external provider id
     */
    public static Promise<PrepUserDTO> register(PrepRegistrationDTO registrationData) {

        PrepUser user = new PrepUser();

        //set the user's default identifier
        user.email = registrationData.email;
        user.externalAuthProviderId = registrationData.externalAuthProviderId;
        PrepUser.AuthProvider requestedExternalAuthProvider = null;
        if(StringUtils.isNotEmpty(registrationData.externalAuthProvider)){
            requestedExternalAuthProvider = PrepUser.AuthProvider.valueOf(registrationData.externalAuthProvider);
            user.externalAuthProvider = requestedExternalAuthProvider;

        }
        
        //user.status = UserStatus.ACTIVE;

        user.firstName = registrationData.firstName;

        user.profilePictureUrl = registrationData.profilePictureUrl;

        //need to do password validation here instead of delegating to the user because when we set the password on the
        //user it gets encrypted. If a user used a 3rd party (facebook, google, etc.) to register they won't have a password
        if(StringUtils.isEmpty(registrationData.externalAuthProvider)){
            Validation validation = Validation.current();
            String password = registrationData.password;

            validation.required(user.email).key("Email");
            validation.email(user.email);
            validation.required(password).key("Password");

            if(password != null){
                //a little niceness to allow single character passwords in dev mode
                double passwordMin = Play.mode.equals(Play.Mode.DEV) ? 1 : PasswordCheck.MIN;

                validation.range(password != null ? password.length() : 0, passwordMin, PasswordCheck.MAX)
                        .message(Messages.get("validation.password", "", "Password", PasswordCheck.MIN, PasswordCheck.MAX));
                validation.equals(password, registrationData.confirmPassword).message("password.confirmation.doNotMatch");
            }
            //hidden inputs are getting bound as empty strings as opposed to the null value that we want so overwrite here
            user.password = password;
        }

        boolean savedSuccessfully = !Validation.hasErrors() && user.validateAndSave();

        if(!savedSuccessfully){
            //re-run the validations so that we can know what exactly is wrong without just
            Set<String> errorKeys = Validation.current().errorsMap().keySet();
            boolean duplicateEmail = errorKeys.contains(".email");
            boolean duplicateProviderId = errorKeys.contains(".externalAuthProviderId");


            PrepUser.AuthProvider conflictingAuthProvider = null;
            if(duplicateEmail){
                PrepUser conflictingUser = PrepUser.find.where().eq("email", registrationData.email).findUnique();
                conflictingAuthProvider = conflictingUser != null ? conflictingUser.externalAuthProvider : null;
            }

            //Handle various error scenarios:
            //1. duplicate email address not associated with an external provider, just use the standard Error Handler
            if(duplicateEmail && conflictingAuthProvider == null && requestedExternalAuthProvider == null){
                ErrorHandler.emitErrorResult("registration");

            //2. duplicate external provider, needs a custom error message
            } else if(duplicateProviderId && requestedExternalAuthProvider != null){
                ErrorHandler.emitCustomErrorResult("prep.error.registration.accountAlreadyActive",
                        requestedExternalAuthProvider.name());

            //3. duplicate email address associated with different external provider, needs a custom error message
            } else if(duplicateEmail && requestedExternalAuthProvider != null && conflictingAuthProvider != null){
                ErrorHandler.emitCustomErrorResult("prep.error.registration.emailUsedByAnotherProvider",
                        requestedExternalAuthProvider.name(), conflictingAuthProvider.name());

            //4. email address for requested auth provider already assigned to a user not using an auth provider
            } else if(duplicateEmail && requestedExternalAuthProvider != null){
                ErrorHandler.emitCustomErrorResult("prep.error.registration.externalEmailConflictNoProvider",
                        requestedExternalAuthProvider.name());

            //Last resort, just use the standard error messaging
            } else {
                ErrorHandler.emitErrorResult("registration");
            }

        } else {
        	
        	//user.roles.add(PrepUser.RoleValue.STANDARD);
            new PrepUserRole(user, RoleValue.STANDARD).save();
        }

        return PrepUserDTO.fromModel(user);
    }

    /**
     * Only code in the delegates package can send custom error messages. This method
     * allows us to use Play's email validation in our controller, and use the delegate
     * to fire off our error.
     * @param email
     */
    public static void renderValidEmailError(String email) {
        ErrorHandler.emitCustomErrorResult("prep.error.user.validEmail", email);
    }
}
