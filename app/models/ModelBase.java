package models;

import beans.ModelAccessWrapper;
import enums.AccessScope;
import exceptions.NoReadPrivilegesException;
import exceptions.NoWritePrivilegesException;
import notifiers.Mails;
import org.apache.commons.lang.reflect.FieldUtils;
import play.Logger;
import play.PlayPlugin;
import play.db.jpa.GenericModel;
import play.db.jpa.JPA;
import play.db.jpa.JPABase;
import play.mvc.Http.Request;
import utils.StatusUtil;

import javax.persistence.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: rphutchinson
 * Date: 1/24/12
 * Time: 1:01 PM
 * To change this template use File | Settings | File Templates.
 */
@MappedSuperclass
public abstract class ModelBase extends GenericModel implements AccessTarget,Comparable {

    @Id
    @TableGenerator( name = "idSequence", table = "ID_SEQ", pkColumnName = "name", pkColumnValue = "STANDARD_ENTITY", valueColumnName = "value", initialValue = 1, allocationSize = 1000 )
    @GeneratedValue( strategy = GenerationType.TABLE, generator = "idSequence" )
    public Long id;

	@Temporal(TemporalType.TIMESTAMP)
	public Date created;

	@Temporal(TemporalType.TIMESTAMP)
	public Date updated;
	
	private transient PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    @Transient
    public List<Event> eventsToCreateWhileSaving = new ArrayList<Event>();
    
    @Transient
    protected boolean listenersEnabled = false;
    
    @Transient
    public List<Object> eventContext = new ArrayList<Object>();
    


	@PrePersist
	protected void onCreate() {
		created = new Date();
	}

	@PreUpdate
	protected void onUpdate() {
		updated = new Date();
	}


    public void createEvents(){
        for (Iterator<Event> iterator = eventsToCreateWhileSaving.iterator(); iterator.hasNext(); ) {
            Event event = iterator.next();
            iterator.remove();

            /*verify that any property change that resulted in the creation of the event is still the same as it was
            when the event was created*/
            PropertyChangeEvent propertyChangeEvent = event.propertyChangeEvent;
            if(propertyChangeEvent != null){
                Object valueFromEvent = propertyChangeEvent.getNewValue();
                Object currentValue = null;
                try {
                    currentValue = FieldUtils.readDeclaredField(this, propertyChangeEvent.getPropertyName());
                } catch (Exception e) {
                    Logger.error("Unable to load property " + propertyChangeEvent.getPropertyName() + " from bean " + this, e);
                }
                if(!valueFromEvent.equals(currentValue)){
                    continue;
                }
            }
            event.save();
        }
    }

	public boolean isDeletable() {
		return true;
	}

	public boolean hasBeenSaved() {
		return this.id != null;
	}

	/**
	 * Default setters are enhanced to call this method. If you have defined
	 * a custom setter and want to realize a property change event then
	 * you'll need to call this method in your setter.
	 * 
	 * @param entityClass
	 * @param propertyName
	 * @param oldValue
	 * @param newValue
	 */
	public void propertyUpdated(Class entityClass, String propertyName, Object oldValue, Object newValue){
		if(((oldValue != null) && !oldValue.equals(newValue)) || ((oldValue == null) && (newValue != null))){
			notifyPropertyChangeListeners(propertyName, oldValue, newValue);
		}
	}
	
	private void notifyPropertyChangeListeners(String propertyName, Object oldValue, Object newValue){
		if(listenersEnabled){
			PropertyChangeEvent propertyChangeEvent = new PropertyChangeEvent(this, propertyName, oldValue, newValue);
			PlayPlugin.postEvent("ModelBase.objectUpdated", propertyChangeEvent);
			getPropertyChangeSupport().firePropertyChange(propertyChangeEvent);
		}
	}
	
	public void addPropertyChangeListener(PropertyChangeListener l) {
			getPropertyChangeSupport().addPropertyChangeListener(l);
	}

	public void removePropertyChangeListener(PropertyChangeListener l) {
			getPropertyChangeSupport().removePropertyChangeListener(l);
	}

	public void addPropertyChangeListener(String propertyName, PropertyChangeListener l) {
			getPropertyChangeSupport().addPropertyChangeListener(propertyName, l);
	}

	public void removePropertyChangeListener(String propertyName, PropertyChangeListener l) {
			getPropertyChangeSupport().removePropertyChangeListener(propertyName, l);
	}

    private PropertyChangeSupport getPropertyChangeSupport(){
    	if(this.propertyChangeSupport == null){
    		this.propertyChangeSupport = new PropertyChangeSupport(this);
    	}

    	return this.propertyChangeSupport;

    }
    
    public void registerListeners(){
    	try {
			getClass().getMethod("___registerListeners").invoke(this);
		} catch (Exception e) {
			Logger.error("Failed to register listeners %s", e.getMessage());
		}
    }
    
    public Object getFieldValue(String fieldName)
    		throws NoReadPrivilegesException {
        return new ModelAccessWrapper(this, fieldName).get(fieldName, this);
    }
    
    public void setFieldValue(String fieldName, Object value)
    	throws NoWritePrivilegesException {
    	new ModelAccessWrapper(this, fieldName).set(fieldName, this, value);
    }
    
    public boolean canAccess(User accessingUser, AccessScope scope) {
    	return true;
    }

    public void checkReadAccess(String fieldName) throws NoReadPrivilegesException{
    	new ModelAccessWrapper(this, fieldName).checkReadAccess(fieldName);
    }
    
    public void checkWriteAccess(String fieldName) throws NoWritePrivilegesException{
    	if(play.Play.runingInTestMode()){
			return;
		}
    	new ModelAccessWrapper(this, fieldName).checkWriteAccess(fieldName);
    }

    private <T extends JPABase> T doSave(boolean isolated){
    	T result = super.save();

    	/*create any events that were registered to be created while saving this entity. This needs to happen after
    	entity itself is saved.*/
    	createEvents();
    	
    	if(isolated){
    		JPA.em().getTransaction().commit();
    		JPA.em().getTransaction().begin();
    	}

    	return result;
    }
    
    public <T extends JPABase> T save(){
    	boolean lowPriority = false;
    	try{
//    		Still a work in progress so I've commented this out
//    		if(Request.current() == null){
//    			lowPriority = true;
//    			/*
//    	    	 * Give higher priority to transactions generated from user requests and lower priority
//    	    	 * to those generated by background tasks/jobs.
//    	    	 */
//    	    	if(play.cache.Cache.get("__lowPriorityLock-" + id) != null){
//    	    		/* This will cause tasks and robust jobs to wait for a little while and.
//    	    		 * try again. Regular jobs will fail.
//    	    		 *
//    	    		 */
//    	    		throw new PersistenceException(new org.hibernate.OptimisticLockException("Low priority task was refused"));
//    	    		
//    	    	}
//    		}
    		return doSave(false);
    	}catch(PersistenceException pe){
    		if(!play.Play.runingInTestMode() && !lowPriority){    			

    			String status =
    					"Update failed.<br>";
    				StringBuffer stack = new StringBuffer();
        			StackTraceElement[] elements = pe.getStackTrace();
        			for(StackTraceElement element : elements){
        				stack.append(element).append("<br>");
        			
        				status += pe.getMessage() + "<br>" +
        						stack + "<br>";
        			}

                try {
                    status += StatusUtil.getDBStatus().replaceAll("\n", "<br>");
                    Mails.criticalStatus(status);
                } catch (Exception e) {
                    Logger.warn("unable to build DBStatus to send status email - this is expected if this is an H2 database.");
                }


    		}
    		throw pe;
    	}
    }

    /**
     * Update, commit the transaction, and start a new transaction. Only use this
     * method if you're 100% sure that all updates kicked off by your thread can be
     * independent (would something get out of sync if one of your updates gets rolled
     * back while another goes through?).
     *
     * @return
     */
    @Deprecated
    public <T extends JPABase> T isolatedSave() {

        try {
            return doSave(true);
        } catch (PersistenceException pe) {
            if (!play.Play.runingInTestMode()) {

                String status =
                        "Update failed.<br>";
                StringBuffer stack = new StringBuffer();
                StackTraceElement[] elements = pe.getStackTrace();
                for (StackTraceElement element : elements) {
                    stack.append(element).append("<br>");

                    status += pe.getMessage() + "<br>" +
                            stack + "<br>";
                }

                try {
                    status += StatusUtil.getDBStatus().replaceAll("\n", "<br>");
                    Mails.criticalStatus(status);
                } catch (Exception e) {
                    Logger.warn("unable to build DBStatus to send status email - this is expected if this is an H2 database.");
                }


            }
            throw pe;
        }
    }

    
    /**
     * The model enhancer will add this method to all subclass implementations
     * Do not override.
     * 
     * @return
     */
    public Class<?> getEntityClass(){
    	throw new UnsupportedOperationException("Entities must implement the getEntityClass() method");
    }

    /**
     * <p>JPABase's implementation of equals is extremely inefficient, requiring
     * reflection, etc.  We override it with a lighter-weight equality.  Two
     * ModelBases are equal if and only if they are the same instance or they 
     * have the same non-null id.</p>
     * 
     * <p>Subclasses requiring stricter equality should override this method.
     * </p>
     * 
     * @param o
     * @return 
     */
    @Override
    public boolean equals(Object o) {
        boolean result = (this == o);
        
        if (!result) {
            result = (o != null) && getClass().equals(o.getClass()); 
            
            if (result) {
                ModelBase oAsModelBase = (ModelBase) o;

                //"Or" vs. "And" here is tricky.  "And" probably makes more 
                //sense, but makes implementing hashCode() difficult since 
                //hashCode() doesn't have knowledge of the other object.  So 
                //theoretically, hashCode() could give different hashes for 
                //objects that would end up being equals().  "Or" allows us to 
                //guarantee that hashCode() is consistent with equals().
                if (oAsModelBase.hasBeenSaved() || hasBeenSaved()) {
                    result = (id != null && oAsModelBase.id != null && 
                                id.equals(oAsModelBase.id));
                }
                else {
                    result = super.equals(o);
                }
            }
        }
        
        return result;
    }
    
    /**
     * See note at equals.
     * @return 
     */
    @Override
    public int hashCode() {
        int result;
        
        if (hasBeenSaved()) {
            result = id.hashCode();
        }
        else {
            result = super.hashCode();
        }
        
        return result;
    }
    
    /**
     * Grab the first entity selected by the given query. Will return null
     * if the query yields an empty result list.
     * 
     * @param query
     * @param params
     * @return The entity
     */
    public static <T extends JPABase> T findFirst(String query, Object... params) {
        throw new UnsupportedOperationException("Please annotate your JPA model with @javax.persistence.Entity annotation.");
    }

	/**
	 * Default compareTo based on id
	 *
	 * @param other Object to compare to.  Assumed to be an instance of ModelBase
	 * @return 0 if equal ids, -1 if this id is smaller than other id, 1 if this id is greater than other id.
	 */
	public int compareTo(Object other) {

		if (other == null) {
			return 0;
		}

		return id.compareTo(((ModelBase) other).id);
	}
	
	public boolean getWillBeSaved(){
		return willBeSaved;
	}

}
