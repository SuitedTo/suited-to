package beans;


import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

import org.apache.commons.beanutils.expression.DefaultResolver;
import org.apache.commons.beanutils.expression.Resolver;
import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.data.binding.Binder;
import play.data.binding.RootParamNode;
import play.exceptions.UnexpectedException;
import play.utils.Utils;


/**
 * This is a heavily modified version of Play's BeanWrapper
 * 
 * @author joel
 *
 */
public class BeanWrapper {

    final static int notwritableField = Modifier.FINAL | Modifier.NATIVE | Modifier.STATIC;
    final static int notaccessibleMethod = Modifier.NATIVE | Modifier.STATIC;

    private Resolver resolver = new DefaultResolver();
    protected Object instance;
    protected Class<?> beanClass;

    /** 
     * a cache for our properties and setters
     */
    private Map<String, BeanProperty> wrappers = new HashMap<String, BeanProperty>();

    public BeanWrapper(Class<?> forClass) {
        if (Logger.isTraceEnabled()) {
            Logger.trace("Bean wrapper for class %s", forClass.getName());
        }

        this.beanClass = forClass;
        try {
			this.instance = newBeanInstance();
		} catch (Exception e) {
			throw new IllegalArgumentException("Unable to create instance of " + forClass.getName());
		}

        registerAccessMethods(forClass);
        registerFields(forClass);
       
    }
    
    public BeanWrapper(Object instance) {
    	if(instance == null){
    		throw new IllegalArgumentException("Unable to wrap null");
    	}
    	this.beanClass = instance.getClass();
    	this.instance = instance;

        registerAccessMethods(beanClass);
        registerFields(beanClass);
       
    }
    
    public BeanWrapper(Object instance, String... fields) {
    	if(instance == null){
    		throw new IllegalArgumentException("Unable to wrap null");
    	}
    	this.beanClass = instance.getClass();
    	this.instance = instance;
    	
        registerFields(beanClass, fields);
       
    }

    public Collection<BeanProperty> getWrappers() {
        return wrappers.values();
    }
    
    public BeanProperty getProperty(String name){
    	for (BeanProperty prop : wrappers.values()) {
            if (name.equals(prop.name)) {
            	return prop;
            }
    	}
    	return null;
    }
    
    public Object get(String name) {
    	
        return get(name, instance);
    }
    
    public Object get(String name, Object instance) {
    	
        BeanProperty prop = getProperty(name);
        if(prop != null){  
            return prop.getValue(instance);
        }
        
        Object nestedObj = getNestedObject(name, instance);
        if(nestedObj != null){
        	return nestedObj;
        }
        
        return null;
    }
    
    private Object getNestedObject(String name, Object bean) {
     
             if (bean == null) {
                 throw new IllegalArgumentException("No bean specified");
             }
             if (name == null) {
                 throw new IllegalArgumentException("No name specified for bean class '" +
                         bean.getClass() + "'");
             }
             
             Object nestedBean = bean;
             boolean nested = false;
     
             // Resolve nested references
             while (resolver.hasNested(name)) {
                 String next = resolver.next(name);
                 
                 nested = true;
                 nestedBean = new BeanWrapper(nestedBean).get(next);
                     
                 if (nestedBean == null) {
                     return null;
                 }
                 name = resolver.remove(name);
             }
             
             return nested?new BeanWrapper(nestedBean).get(name):null;
     
         }
    
    public void set(String name, Object value) {
    	
        set(name, instance, value);
    }

    public void set(String name, Object instance, Object value) {
    	BeanProperty prop = getProperty(name);
        if(prop != null){  
        	prop.setValue(instance, value);
            return;
        }
        String message = String.format("Can't find property with name '%s' on class %s", name, instance.getClass().getName());
        Logger.warn(message);
        throw new UnexpectedException(message);

    }
    
    private boolean isSetter(Method method) {
        return (method.getName().startsWith("set") && method.getName().length() > 3 && method.getParameterTypes().length == 1 && (method.getModifiers() & notaccessibleMethod) == 0);
    }

    protected Object newBeanInstance() throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Constructor constructor = beanClass.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }

    private void registerFields(Class<?> clazz) {
        // recursive stop condition
        if (clazz == Object.class) {
            return;
        }
        Field[] fields = clazz.getFields();
        for (Field field : fields) {
            if (wrappers.containsKey(field.getName())) {
                continue;
            }
            if ((field.getModifiers() & notwritableField) != 0) {
                continue;
            }
            BeanProperty w = new BeanProperty(field);
            wrappers.put(field.getName(), w);
        }
        registerFields(clazz.getSuperclass());
    }

    private void registerAccessMethods(Class<?> clazz) {
        if (clazz == Object.class) {
            return;
            // deep walk (superclass first)
        }
        registerAccessMethods(clazz.getSuperclass());

        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
        	
        	if (isSetter(method)) {
        		String propertyname = method.getName()
        				.substring(3, 4).toLowerCase() + method.getName().substring(4);

        		String getterName = "get" + StringUtils.capitalize(propertyname);
        		
        		try{
        			Method getter = clazz.getMethod(getterName);
        			BeanProperty wrapper = new BeanProperty(propertyname, getter, method);
        			wrappers.put(propertyname, wrapper);
        		}catch(Exception e){
        			//no matching getter found - just skip it
        		}
        	}
        	
        }
    }
    
    private void registerFields(Class<?> clazz, String...fields) {
        if (clazz == Object.class) {
            return;
            // deep walk (superclass first)
        }
        registerAccessMethods(clazz.getSuperclass());
        
        for(String fieldStr : fields){
        	try {
				Field field = clazz.getField(fieldStr);
				String suffix = StringUtils.capitalize(fieldStr);
				String setterName = "set" + suffix;
				String getterName = "get" + suffix;
				Method setter = clazz.getDeclaredMethod(setterName, new Class[]{field.getType()});
				Method getter = clazz.getDeclaredMethod(getterName);
				
				BeanProperty wrapper = new BeanProperty(fieldStr, getter, setter);
    			wrappers.put(fieldStr, wrapper);
    			
			} catch (SecurityException e) {
				Logger.error("Unable to create property wrapper %s", e);
			} catch (NoSuchFieldException e) {
				Logger.error("Unable to create property wrapper %s", e);
			} catch (NoSuchMethodException e) {
				Logger.error("Unable to create property wrapper %s", e);
			}
        }
    }

    public Object bind(String name, Type type, Map<String, String[]> params, String prefix, Annotation[] annotations) throws Exception {
        Object instance = newBeanInstance();
        return bind(name, type, params, prefix, instance, annotations);
    }

    public Object bind(String name, Type type, Map<String, String[]> params, String prefix, Object instance, Annotation[] annotations) throws Exception {
        RootParamNode paramNode = RootParamNode.convert( params);
        // when looking at the old code in BeanBinder and Binder.bindInternal, I
        // think it is correct to use 'name+prefix'
        Binder.bindBean( paramNode.getChild(name+prefix), instance, annotations);
        return instance;
    }
}

