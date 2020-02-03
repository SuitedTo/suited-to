package controllers.prep.rest;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import common.utils.prep.SecurityUtil;
import controllers.prep.access.Access;
import controllers.prep.access.PrepRestrictedResource;
import controllers.prep.auth.PublicAction;
import controllers.prep.delegates.ErrorHandler;
import controllers.prep.delegates.UsersDelegate;
import data.binding.types.prep.JsonBinder;
import dto.prep.PrepAccountUpdateDTO;
import dto.prep.PrepRegistrationDTO;
import dto.prep.PrepUserDTO;
import errors.prep.PrepErrorType;
import models.Job;
import models.User;
import models.prep.PrepUser;
import org.apache.commons.lang.StringUtils;
import org.jboss.netty.handler.codec.base64.Base64;
import play.Play;
import play.data.binding.As;
import play.data.validation.Required;
import play.libs.WS;
import play.mvc.With;
import play.mvc.results.Result;

import java.util.HashMap;
import java.util.Map;

/**
 * RESTful Service for PrepUsers
 */
@With(Access.class)
public class Users extends PrepController {

    /**
     * Creates a new PrepUser based on registration parameters
     *
     * @param body
     */
    @PublicAction
    public static void create(@As(binder = JsonBinder.class) PrepRegistrationDTO body) {
        //when no values are filled on the form the body will empty PrepRegistrationDTO that can be passed to the delegate
        if (body == null) {
            body = new PrepRegistrationDTO();
        }

        //determine if that email account has already been registered
        PrepUser user = null;

        if(body.email != null) {
            user = PrepUser.find.where().eq("email", body.email).findUnique();
        }

        if(user == null) {

            /*use the delegate to create the actual user, if there are validation errors they will be added to the ThreadLocal
            Validation.current() instance which is available locally here in the Controller via the validation variable*/
            PrepUserDTO prepUserDTO = await(UsersDelegate.register(body));

            //any validation errors would have already halted execution being thrown as PrepErrorResults
            SecurityUtil.createAuthenticatedSession(prepUserDTO);

            renderRefinedJSON(prepUserDTO.toJsonTree());

        } else {
            ErrorHandler.emitCustomErrorResult(PrepErrorType.AUTHENTICATION_ERROR, "That email has already been registered.");
        }

    }

    /**
     * Get a JSON representation of a PrepUser with the given ID
     *
     * @param id primary key of a PrepUser
     */
    @PrepRestrictedResource(resourceClassName = "models.prep.PrepUser")
    public static void get(Long id) {
        PrepUserDTO dto = await(UsersDelegate.get(id));

        renderRefinedJSON(dto.toJsonTree());
    }

    /**
     *
     */
    public static void update(@Required @As(binder = JsonBinder.class) PrepAccountUpdateDTO body) {

        /**
         * Validating the email here, as Validation is protected within the
         * Controller class, meaning that I can't use Play's validation in UserDelegate.
         */
        validation.email(body.email);
        if (validation.hasErrors()) {
            UsersDelegate.renderValidEmailError(body.email);
        }

        PrepUser user = PrepUser.find.byId(body.id);

        PrepAccountUpdateDTO dto = UsersDelegate.update(body.id, body.firstName, body.email, body.password, user.stripeId);

        renderRefinedJSON(dto.toJsonTree());
    }
}
