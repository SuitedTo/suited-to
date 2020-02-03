package jobs;

import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceException;

import org.hibernate.OptimisticLockException;
import org.hibernate.StaleStateException;

import play.Logger;
import play.Invoker.Suspend;
import play.db.jpa.JPA;
import play.jobs.Job;

public class RobustJob<T> extends Job<T>{

	public final T doJobWithResult(){
		try{
			return tryJobWithResult();
		} catch(PersistenceException pe){
			final Throwable cause = pe.getCause();
		    if (cause instanceof OptimisticLockException || cause instanceof StaleStateException) {
		      final EntityTransaction tx = JPA.em().getTransaction();
		      if (tx.isActive()) {
		        tx.setRollbackOnly();
		      }
		      Logger.error("Caught %s in task %s. Retrying transaction.", cause.getClass().getName(), getClass().getName());
		      //I haven't tested this all the way through yet but it should work
		      throw new Suspend((int)(Math.random() * 500));
		    }
		    throw pe;
		}
	}
	
	public final void doJob(){}
	
	public void tryJob(){
		
	}
	
	public T tryJobWithResult(){
		tryJob();
		return null;
	}
}
