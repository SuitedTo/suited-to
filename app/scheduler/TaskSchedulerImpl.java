package scheduler;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledFuture;

public interface TaskSchedulerImpl {

	public long getTaskCount();
	public BlockingQueue<Runnable> getQueue();
	public int getActiveCount();
	public <V> ScheduledFuture<V> schedule(Callable<V> task, long delay);
}
