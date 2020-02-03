package scheduler;

public interface TriggerDetailManager extends
	TriggerDetailQueue,
	TriggerDetailProvider,
	TaskStateListener{
	
	public void die();
	public void clearSchedule();
}
