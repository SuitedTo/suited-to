package errors.prep;

import play.i18n.Messages;
import play.mvc.Http;

public enum PrepHttpError {
    AUTHORIZATION_ERROR("prep.error.authorization", Http.StatusCode.UNAUTHORIZED),
    SSL_ERROR("prep.error.ssl", Http.StatusCode.BAD_REQUEST);

    private final String message;
    private final int statusCode;


    private PrepHttpError(String messageKey, int statusCode) {
        this.message = Messages.get(messageKey);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getMessage() {
        return message;
    }
}
