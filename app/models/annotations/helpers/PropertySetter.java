
package models.annotations.helpers;

/**
 * <p>A strategy for setting a property on an object.</p>
 */
public interface PropertySetter {
    public void set(Object dest, String property, Object value) 
            throws InvalidValueException;
}
