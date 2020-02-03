package utils;

import java.lang.annotation.Annotation;

import javax.persistence.Entity;
import javax.persistence.Query;

import models.ModelBase;
import play.Logger;
import play.Play;
import play.db.jpa.JPA;
import play.utils.Java;

public class TimingUtil {

	private TimingUtil(){}
	
	/**
	 * Only for use from within a job or task.
	 * 
	 * Wait for an entity to show up in the db.
	 * 
	 * @param clazz
	 * @param id
	 * @return The entity or null if it never showed up
	 */
	public static <T extends ModelBase> T wait(Class<? extends ModelBase> clazz, Long id){
		String entityName = clazz.getSimpleName();
		Annotation entityAnnot = clazz.getAnnotation(Entity.class);
	 	if(entityAnnot != null){
	 		String name = ((Entity)entityAnnot).name();
		 	 if(!StringUtils.isEmpty(name)){
		 		 entityName = name;
		 	 }
		 	 
		}
		int attemptNum = 1;
		Query query = JPA.em().createQuery("SELECT e FROM " + entityName + " e where id=:id");
    	query.setParameter("id", id);
		while(attemptNum <= 3){
			Object result = null;
			try{
				result = query.getSingleResult();
			}catch (javax.persistence.NoResultException e){
				
			}
			if(result != null){
				return (T) result;
			}else{
				try {
					Thread.currentThread().sleep(1000 * attemptNum);
				} catch (InterruptedException e) {
				}
				++attemptNum;
				continue;
			}
			
		}
		return null;
	}
}
