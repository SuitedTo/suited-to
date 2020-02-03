/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package models.annotations.helpers;

import java.lang.reflect.Field;
import models.User;

/**
 * <p>A property setter that simply assumes the property has a correspondingly
 * named field, setting it appropriately.</p>
 */
public class FieldSetter implements PropertySetter {

    public void set(Object dest, String property, Object value) 
            throws InvalidValueException {
        
        try {
            Field field = User.class.getField(property);
            field.set(dest, value);
        }
        catch (NoSuchFieldException isfe) {
            throw new RuntimeException(isfe);
        }
        catch (IllegalAccessException iae) {
            throw new RuntimeException(iae);
        }

    }
}
