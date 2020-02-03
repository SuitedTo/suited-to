/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.annotations;

import data.binding.types.DummyBinder;
import enums.AccessScope;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import models.annotations.helpers.FieldSetter;
import models.annotations.helpers.PropertySetter;
import play.data.binding.TypeBinder;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Access {
    AccessScope visibility();
    AccessScope mutability();
}
