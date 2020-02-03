package scheduler;


import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

import play.Invoker.Suspend;
import play.Logger;
import play.Play;
import play.cache.Cache;
import play.db.jpa.JPA;
import play.exceptions.JavaExecutionException;
import play.exceptions.PlayException;
import play.jobs.Job;

import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceException;

import models.CronTrigger;

import org.hibernate.OptimisticLockException;
import org.hibernate.StaleStateException;

class TaskWrapper<V> extends TaskInvocation implements Callable<V>{	
    
	protected final Task task;

	public TaskWrapper(Task task){
		super(task.context);
		this.task = task;
	}

	private static boolean lock(TriggerDetail triggerDetail){
		return Cache.safeAdd(triggerDetail.getTriggerKey(), "-1", "5s");
	}
	

    @Override
    public void execute() throws Exception {
    }
    
	public V call() {
		
		if(!lock(task.context.getTriggerDetail())){
			finished(false);
			return null;
		}
		Monitor monitor = null;
		try {
			boolean retry = false;
			if (init()) {
				before();
				V result = null;

				try {
					monitor = MonitorFactory.start("Task: " + task.getClass().getName());
					result = (V) task.doTaskWithResult();
					monitor.stop();
					monitor = null;
				} catch(PersistenceException pe){
					final Throwable cause = pe.getCause();
				    if (cause instanceof OptimisticLockException  || cause instanceof StaleStateException) {
				      final EntityTransaction tx = JPA.em().getTransaction();
				      if (tx.isActive()) {
				        tx.setRollbackOnly();
				      }
				      Logger.error("Caught %s in task %s. Will retry ASAP", cause.getClass().getName(), getClass().getName());
			          retry = true;
				    }
				    throw pe;
				} catch (StaleStateException sse){
					final EntityTransaction tx = JPA.em().getTransaction();
				      if (tx.isActive()) {
				        tx.setRollbackOnly();
				      }
				      Logger.error("Caught %s in task %s. Will retry ASAP", sse.getClass().getName(), getClass().getName());
			          retry = true;
			          throw sse;
				} catch (PlayException e) {
					throw e;
				} catch (Exception e) {
					StackTraceElement element = PlayException.getInterestingStrackTraceElement(e);
					if (element != null) {
						throw new JavaExecutionException(Play.classes.getApplicationClass(element.getClassName()), element.getLineNumber(), e);
					}
					throw e;
				}finally{
					after();
					if(retry){
						new Job(){
							public void doJob(){
								try {
									Thread.sleep(5000L);
								} catch (InterruptedException e) {
								}
								//Could keep a retry count in the db
								TaskScheduler.schedule(task.getClass(),
										task.context.getTriggerDetail().getTaskArgs(),
										CronTrigger.getASAPTrigger());
							}
						}.now();
						
					}
				}
				return result;
			}
        //Typically we wouldn't catch Throwable. However, this is consistent with other invocations inside the
        //Play! Framework so we're catching Throwable to stay consistent with that.
		} catch (Throwable e) {
			e.printStackTrace();
			onException(e);
		} finally {
			if(monitor != null) {
				monitor.stop();
			}
			_finally();
		}
		return null;
	}
	
	public String toString(){
		return task.getClass().getName();
	}

}
