package scheduler;

import java.util.Calendar;
import java.util.List;


public class TaskExecutionContext{
	private final TaskStateListener stateListener;
	private final TaskScheduler scheduler;
	private final TriggerDetail triggerDetail;
	private final List<Calendar> executionTimes;
	
	public TaskExecutionContext(TaskScheduler scheduler,
			TaskStateListener progressListener,
			TriggerDetail triggerDetail,
			List<Calendar> executionTimes) {
		this.scheduler = scheduler;
		this.triggerDetail = triggerDetail;
		this.stateListener = progressListener;
		this.executionTimes = executionTimes;
	}

	public TriggerDetail getTriggerDetail() {
		return triggerDetail;
	}

	public TaskScheduler getScheduler() {
		return scheduler;
	}

	public final TaskStateListener getStateListener() {
		return stateListener;
	}

	public final List<Calendar> getExecutionTimes() {
		return executionTimes;
	}
}
