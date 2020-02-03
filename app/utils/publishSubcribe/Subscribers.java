package utils.publishSubcribe;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target(value={java.lang.annotation.ElementType.TYPE})
@Retention(value=java.lang.annotation.RetentionPolicy.RUNTIME)
public @interface Subscribers {
	public abstract Class<?>[] value();
}
