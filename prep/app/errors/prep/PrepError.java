package errors.prep;

/**
 * A Server-side error in the PrepApplication.  Typically passed back to the client using PrepErrorResult
 */
public class PrepError {

    /**
     * The type of error
     */
    private final PrepErrorType type;

    /**
     * The message to be displayed to the user (as-is) no further templating is performed
     */
    private final String message;

    /**
     * optional key for the error message. Typically this will represent a field on the model class wen there is a model
     * validation that fails
     */
    private final String key;



    public PrepError(PrepErrorType type, String key, String message) {
        this.type = type;
        this.key = key;
        this.message = message;
    }

    public PrepError(PrepErrorType type) {
        this.type = type;
        this.key = null;
        this.message = type.getDefaultMessage();
    }

    public PrepError(PrepErrorType type, play.data.validation.Error playError){
        this.type = type;
        this.key = playError.getKey();
        this.message = playError.message();
    }


    public PrepErrorType getType() {
        return type;
    }

    public String getMessage(){
        return message;
    }
}
