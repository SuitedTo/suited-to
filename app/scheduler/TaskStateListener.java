package scheduler;

public interface TaskStateListener {
	
	public void scheduled(TriggerDetail triggerDetail);

	/**
	 * The task associated with the given trigger has been executed.
	 * 
	 * @param success If the task ran successfully
	 * @param triggerDetail
	 */
	public void finished(TriggerDetail triggerDetail, boolean success);
}
