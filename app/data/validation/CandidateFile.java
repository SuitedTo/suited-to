package data.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import net.sf.oval.configuration.annotation.Constraint;

/**
 * Candidate file validation.
 * Message key: validation.required
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Constraint(checkWith = CandidateFileCheck.class)
public @interface CandidateFile {
    
    boolean required() default CandidateFileCheck.REQUIRED;
    
    String alias() default CandidateFileCheck.ALIAS;

    int size() default CandidateFileCheck.MAX_SIZE;
}