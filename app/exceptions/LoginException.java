package exceptions;

import errors.prep.PrepErrorType;
import play.utils.FastRuntimeException;

public class LoginException extends FastRuntimeException {
	
	private PrepErrorType errorType;

    public LoginException(String s) {
        super(s);
    }
    
    public LoginException(PrepErrorType errorType, String s) {
        this(s);
        this.errorType = errorType;
    }

	public PrepErrorType getErrorType() {
		return errorType;
	}
}
