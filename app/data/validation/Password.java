package data.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import net.sf.oval.configuration.annotation.Constraint;

/**
 * Password validation.
 * Message key: validation.required
 * $1: field name
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Constraint(checkWith = PasswordCheck.class)
public @interface Password {

    String alias() default PasswordCheck.ALIAS;
    
    int min() default PasswordCheck.MIN;
    
    int max() default PasswordCheck.MAX;
    
    boolean required() default PasswordCheck.REQUIRED;
    
    String message() default PasswordCheck.mes;
}

