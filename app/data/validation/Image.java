package data.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import net.sf.oval.configuration.annotation.Constraint;

/**
 * Image validation.
 * Message key: validation.required
 * $1: field name
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Constraint(checkWith = ImageCheck.class)
public @interface Image {
    
    boolean required() default ImageCheck.REQUIRED;
    
    String alias() default ImageCheck.ALIAS;
    
    boolean typeCheckOnly() default ImageCheck.TYPE_CHECK_ONLY;
}