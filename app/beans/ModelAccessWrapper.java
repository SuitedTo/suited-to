package beans;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import models.ModelBase;
import models.annotations.Access;
import controllers.Security;
import enums.AccessScope;
import exceptions.NoWritePrivilegesException;
import exceptions.NoReadPrivilegesException;
import beans.BeanWrapper;

public class ModelAccessWrapper extends BeanWrapper{

	public ModelAccessWrapper(ModelBase instance) {
		super(instance);
	}
	
	public ModelAccessWrapper(ModelBase instance, String... fields) {
		super(instance, fields);
	}
	
	public Object getFieldValue(String name) throws NoReadPrivilegesException{
		checkReadAccess(name);
		return super.get(name);
	}
	
	public void setFieldValue(String name, Object value) throws NoWritePrivilegesException{
		checkWriteAccess(name);
		super.set(name, value);
	}
	
	public void checkWriteAccess(String fieldName) throws NoWritePrivilegesException{
		if(!((ModelBase)instance).canAccess(controllers.Security.connectedUser(),
	        	getFieldMutabilityPermission(fieldName))){
	        		throw new NoWritePrivilegesException();
	        	}
	}
	
    public void checkReadAccess(String fieldName) throws NoReadPrivilegesException{
    	try {
			if(!((ModelBase)instance).canAccess(controllers.Security.connectedUser(),
				getFieldVisibilityPermission(fieldName))){
					throw new NoReadPrivilegesException();
				}
		} catch (NoSuchFieldException e) {
			throw new NoReadPrivilegesException();
		}
    }
	
	AccessScope getFieldVisibilityPermission(String fieldName) 
    		throws NoSuchFieldException {
    	return getAccessAnnotation(fieldName).visibility();
    }

    AccessScope getFieldMutabilityPermission(String fieldName) {
    	return getAccessAnnotation(fieldName).mutability();
    }
	
    private Access getAccessAnnotation(String fieldName) {
    	Field field;

    	try {
    		field = ((ModelBase)instance).getEntityClass().getField(fieldName);
    	}
    	catch (NoSuchFieldException nsfe) {
    		throw new RuntimeException(nsfe);
    	}
    	
    	Access result = null;
    	if(Security.connectedUser() != null){
    		result = field.getAnnotation(Access.class);
    	}

    	if (result == null) {
    		return new Access(){

				@Override
				public Class<? extends Annotation> annotationType() {
					return Access.class;
				}

				@Override
				public AccessScope visibility() {
					return AccessScope.PUBLIC;
				}

				@Override
				public AccessScope mutability() {
					return AccessScope.PUBLIC;
				}
    			
    		};
    	}

    	return result;
    }

}
