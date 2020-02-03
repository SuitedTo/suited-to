package beans;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import play.Logger;
import play.data.binding.NoBinding;
import play.exceptions.UnexpectedException;


/**
 * This is a heavily modified version of Play's BeanWrapper.Property
 * 
 * @author joel
 *
 */
public class BeanProperty {

    private Annotation[] annotations;
    private Method getter;
    private Method setter;
    private Field field;
    private Class<?> type;
    private Type genericType;
    String name;
    private String[] profiles;

    BeanProperty(String propertyName, Method getterMethod, Method setterMethod) {
        name = propertyName;
        getter = getterMethod;
        setter = setterMethod;
        type = setter.getParameterTypes()[0];
        annotations = setter.getAnnotations();
        genericType = setter.getGenericParameterTypes()[0];
        setProfiles(this.annotations);
    }

    BeanProperty(Field field) {
        this.field = field;
        this.field.setAccessible(true);
        name = field.getName();
        type = field.getType();
        annotations = field.getAnnotations();
        genericType = field.getGenericType();
        setProfiles(this.annotations);
    }

    public void setProfiles(Annotation[] annotations) {
        if (annotations != null) {
            for (Annotation annotation : annotations) {
                if (annotation.annotationType().equals(NoBinding.class)) {
                    NoBinding as = ((NoBinding) annotation);
                    profiles = as.value();
                }
            }
        }
    }
    
    public Object getValue(Object instance) {
        try {
            if (getter != null) {
                if (Logger.isTraceEnabled()) {
                    Logger.trace("invoke getter %s on %s", getter, instance);
                }
                
                return getter.invoke(instance);
            } else {
                if (Logger.isTraceEnabled()) {
                    Logger.trace("field.get(%s)", instance);
                }

                return field.get(instance);
            }

        } catch (Exception ex) {
            Logger.warn(ex, "ERROR in BeanWrapper when getting property %s", name);
            throw new UnexpectedException(ex);
        }
    }

    public void setValue(Object instance, Object value) {
        try {
            if (setter != null) {
                if (Logger.isTraceEnabled()) {
                    Logger.trace("invoke setter %s on %s with value %s", setter, instance, value);
                }

                setter.invoke(instance, value);
                return;
            } else {
                if (Logger.isTraceEnabled()) {
                    Logger.trace("field.set(%s, %s)", instance, value);
                }

                field.set(instance, value);
            }

        } catch (Exception ex) {
            Logger.warn(ex, "ERROR in BeanWrapper when setting property %s value is %s (%s)", name, value, value == null ? null : value.getClass());
            throw new UnexpectedException(ex);
        }
    }
    
    /**
     * Does this property represent an indexed value (ie an array or List)?
     */
    public boolean isIndexed() {

        if (type == null) {
            return (false);
        } else if (type.isArray()) {
            return (true);
        } else if (List.class.isAssignableFrom(type)) {
            return (true);
        } else {
            return (false);
        }

    }


    /**
     * Does this property represent a mapped value (ie a Map)?
     */
    public boolean isMapped() {

        if (type == null) {
            return (false);
        } else {
            return (Map.class.isAssignableFrom(type));
        }

    }

    public String getName() {
        return name;
    }

    public Class<?> getType() {
        return type;
    }

    public Type getGenericType() {
        return genericType;
    }

    public Annotation[] getAnnotations() {
        return annotations;
    }

    @Override
    public String toString() {
        return type + "." + name;
    }


}