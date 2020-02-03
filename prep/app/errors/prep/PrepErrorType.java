package errors.prep;

import play.i18n.Messages;

/**
 * Types of errors
 */
public enum PrepErrorType {
	AUTHENTICATION_ERROR(),
	NO_SUCH_USER_ERROR(),
	ACCESS_ERROR(),
    DATA_VALIDATION_ERROR(),
    INVALID_INTERVIEW,
    STRIPE_ERROR;

    /**
     * Key to a message that can be displayed to the user for this ErrorType.  Not all PrepErrorTypes will have a default
     * message and the value can always be overridden in PrepError if desired.
     */
	private final String defaultMessage;
	
	private PrepErrorType(String messageKey){
		this.defaultMessage = Messages.get(messageKey);
	}

    private PrepErrorType(){
        this.defaultMessage = null;
    }

	public String getDefaultMessage() {
		return defaultMessage;
	}
}
