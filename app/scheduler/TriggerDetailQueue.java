package scheduler;

public interface TriggerDetailQueue {

	public boolean offer(TriggerDetail triggerDetail);
	
	public TriggerDetail get(String triggerKey);
	
}
