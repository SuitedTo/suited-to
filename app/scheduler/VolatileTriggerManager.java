package scheduler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import play.Logger;


public class VolatileTriggerManager implements TriggerDetailManager{

	private AtomicBoolean die = new AtomicBoolean(false);
	private List<TriggerDetail> scheduled = new ArrayList<TriggerDetail>();
	private final ConcurrentLinkedQueue<TriggerDetail> taskDetailStore = new ConcurrentLinkedQueue<TriggerDetail>();
	
	
	@Override
	public void start(TriggerDetailConsumer consumer) {
		new TriggerDetailProvider(consumer).start();
	}
	
	@Override
	public boolean offer(TriggerDetail triggerDetail) {
		return taskDetailStore.offer(triggerDetail);
	}
	
	private class TriggerDetailProvider extends Thread{
		private final TriggerDetailConsumer consumer;
		
		public TriggerDetailProvider(TriggerDetailConsumer consumer){
			this.consumer = consumer;
		}
		
		public void run(){

			while(!die.get()){
				Object[] td = taskDetailStore.toArray();
				for (int i = 0; i < td.length; ++i){
					if(td != null){
						consumer.acceptTask((TriggerDetail)td[i]);
					}
				}

				try {
					Thread.currentThread().sleep(1000L);
				} catch (InterruptedException e) {}
			}
		}
	}
	
	@Override
	public void scheduled(TriggerDetail triggerDetail) {
		synchronized(scheduled){
			taskDetailStore.remove(triggerDetail);
			scheduled.add(triggerDetail);
		}
	}

	@Override
	public void finished(TriggerDetail triggerDetail, boolean success) {
		synchronized(scheduled){
			scheduled.remove(triggerDetail);
		
			if(triggerDetail.getTrigger().reschedule){
				offer(triggerDetail);
			}
		}
		
	}

	@Override
	public void clearSchedule() {		
	}
	
	/**
	 * @param triggerKey
	 */
	public TriggerDetail get(String triggerKey) {
		Iterator<TriggerDetail> tds = scheduled.iterator();
		while(tds.hasNext()){
			TriggerDetail td = tds.next();
			if(td.getTriggerKey().endsWith(triggerKey)){
				return td;
			}
		}
		return null;
	}

	@Override
	public void die() {
		this.die.set(true);
	}
	
}
