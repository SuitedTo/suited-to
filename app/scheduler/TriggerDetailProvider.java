package scheduler;

public interface TriggerDetailProvider {

	
	/**
	 * Start your loop. For each stored trigger detail THAT IS NOT ALREADY SCHEDULED,
	 * call consumer.acceptTask().
	 * @param sink
	 */
	public void start(TriggerDetailConsumer consumer);
}
