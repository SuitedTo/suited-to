package utils;

import java.util.concurrent.Callable;


import play.Logger;
import play.jobs.Job;
import play.mvc.Http.Request;
import play.mvc.Scope.Session;

/**
 * Various job related helper methods.
 *
 * @author joel
 *
 */
public final class JobUtil {

	private JobUtil(){}

	/**
	 * Wrap the given runnable within a Job that will run
	 * within the current thread context.
	 *
	 * @param runnable
	 * @return A Job with no result
	 */
	public static Job asJobInContext(final Runnable runnable){
		final Session session = Session.current();
		return new Job(){
			public void doJob(){

				Session.current = new ThreadLocal<Session>();
				Session.current.set(session);
				runnable.run();
			}
		};
	}
	
	/**
	 * Wrap the given runnable within a Job that will run
	 * within the current thread context.
	 *
	 * @param callable
	 * @return A Job with result
	 */
	public static <T> Job<T> asJobInContext(final Callable<T> callable){
		final Session session = Session.current();
		return new Job<T>(){
			public T doJobWithResult(){

				Session.current = new ThreadLocal<Session>();
				Session.current.set(session);
				try {
					return callable.call();
				} catch (Exception e) {
					Logger.error("Unable to create job %s", e.getMessage());
					return null;
				}
			}
		};
	}
}