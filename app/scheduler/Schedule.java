package scheduler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Schedule {
	String on();
	String timeZone() default "NIL";
	boolean allTimeZones() default false;
	boolean repeat() default true;
}
