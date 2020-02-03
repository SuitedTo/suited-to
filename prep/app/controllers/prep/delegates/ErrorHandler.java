package controllers.prep.delegates;

import errors.prep.PrepError;
import errors.prep.PrepErrorResult;
import errors.prep.PrepErrorType;
import play.data.validation.*;
import play.data.validation.Error;
import play.i18n.Messages;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for dealing with PrepErrors within the delegates package
 */
public class ErrorHandler {

    private static final String errorPrefix = "prep";

    /**
     * The most basic way to emit errors. Will search in messages file for a message with a key of prep.{actualKeyFromValidationError}
     * Uses the ThreadLocal Validation.current() instance to find errors that should be emitted
     */
    static void emitErrorResult(){
        emitErrorResult(null);
    }

    /**
     * Like the no-args overloaded method but allows the caller to specify a "context" which will be appended to the message key
     * example: prep.password.registration if "registration" is the given context.
     * @param context String to be appended to message key
     */
    static void emitErrorResult(String context){
        List<PrepError> errorList = new ArrayList<PrepError>();
        for (Error error : Validation.errors()) {
            StringBuilder messageKeyBuilder = new StringBuilder(errorPrefix);
            messageKeyBuilder.append(error.getKey());
            if(context != null){
                messageKeyBuilder.append(".");
                messageKeyBuilder.append(context);
            }

            String prepSpecificMessageKey = messageKeyBuilder.toString();

            String message = Messages.get(prepSpecificMessageKey);

            if(prepSpecificMessageKey.equals(message)){  //we didn't find a matching prep-specific message
                message = error.message();
            }

            //strip out the annoying generic model validation message that Play adds in there
            if(!"Validation failed".equals(message)){
                errorList.add(new PrepError(PrepErrorType.DATA_VALIDATION_ERROR, error.getKey(), message));
            }
        }
        throw new PrepErrorResult(errorList.toArray(new PrepError[errorList.size()]));
    }

    /**
     * Allows the caller to emit a customized message.  The message key should exactly match a value in messages file.
     * Additional variables may also be added to the message
     * @param messageKey
     * @param variables
     */
    static void emitCustomErrorResult(String messageKey, String... variables){
        emitCustomErrorResult(PrepErrorType.DATA_VALIDATION_ERROR, messageKey, variables);
    }

    public static void emitCustomErrorResult(PrepErrorType type, String messageKey){
        emitCustomErrorResult(type, messageKey, "");
    }

    static void emitCustomErrorResult(PrepErrorType type, String messageKey, String... variables){
        Error error = new Error("", messageKey, variables);
        throw new PrepErrorResult(new PrepError(type, error));
    }
}
