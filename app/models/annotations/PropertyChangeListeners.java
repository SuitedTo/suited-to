package models.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * This annotation can be added to any entity class. Each Listener class
 * must implement the java.beans.PropertyChangeListener interface and must
 * have a no arg constructor.
 * 
 * @author joel
 *
 */
@Target(value={java.lang.annotation.ElementType.TYPE})
@Retention(value=java.lang.annotation.RetentionPolicy.RUNTIME)
public abstract @interface PropertyChangeListeners {
	
  public abstract Class<?>[] value();

}
