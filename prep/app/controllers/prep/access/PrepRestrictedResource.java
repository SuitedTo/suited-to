package controllers.prep.access;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created with IntelliJ IDEA for sparc-interview
 * User: phutchinson
 * Date: 3/15/13
 * Time: 5:08 PM
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface PrepRestrictedResource {

    String resourceClassName();
}
