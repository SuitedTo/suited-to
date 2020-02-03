package scheduler;

import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import play.Logger;
import play.exceptions.UnexpectedException;
import play.libs.Time.CronExpression;


public class DefaultTaskScheduler extends ScheduledThreadPoolExecutor implements TaskSchedulerImpl{


	public DefaultTaskScheduler(int corePoolSize, ThreadFactory threadFactory,
			RejectedExecutionHandler handler) {
		super(corePoolSize, threadFactory, handler);
	}

	@Override
	public <V> ScheduledFuture<V> schedule(Callable<V> task, long delay){
        try {
            
            return schedule(task,
            		delay,
            		TimeUnit.MILLISECONDS);
        } catch (Exception ex) {
            throw new UnexpectedException(ex);
        }
    }

	

}
