package scheduler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import jobs.MultiNodeRunnable;
import jobs.RobustJob;
import models.CronTrigger;
import models.TaskTrigger;
import models.TriggerEntry;
import play.Logger;
import play.jobs.Job;
import play.jobs.OnApplicationStart;

public class DBTriggerManager  implements TriggerDetailManager{

	private volatile List<TriggerDetail> scheduled = new ArrayList<TriggerDetail>();
	private AtomicBoolean die = new AtomicBoolean(false);

	@Override
	public void start(TriggerDetailConsumer consumer) {
		new TriggerDetailProvider(consumer).start();
	}

	@Override
	public boolean offer(final TriggerDetail triggerDetail) {
		final TriggerEntry t = new TriggerEntry();
		t.triggerKey = triggerDetail.getTriggerKey();
		t.taskClass = triggerDetail.getTaskClassName();
		t.taskArgs = triggerDetail.getTaskArgs().toXML();

		if(TriggerEntry.find("byTaskClassAndTaskArgs", t.taskClass, t.taskArgs).first() != null){
			Logger.error("Rejected duplicate task: %s %s", t.taskClass, t.taskArgs);
			return false;
		}

		final TaskTrigger tt = triggerDetail.getTrigger();
		tt.triggerEntry = t;
		new Job(){

			public void doJob(){				
				t.save();
				tt.save();
			}
		}.now();
		return true;
	}

	private class TriggerDetailProvider extends Thread{
		private final TriggerDetailConsumer consumer;

		public TriggerDetailProvider(TriggerDetailConsumer consumer){
			this.consumer = consumer;
		}

		public void run(){

			while(!die.get()){
				synchronized(scheduled){
					new Job(){

						public void doJob(){
							
							List<TriggerEntry> triggers = TriggerEntry.findAll();
							for(TriggerEntry t : triggers){
								
								CronTrigger ct = CronTrigger.find("byTriggerEntry", t).first();

								//Only supporting cron triggers right now so ct shouldn't be null
								if(ct == null){
									Logger.error("No trigger found for entry %s", t.triggerKey);
									continue;
								}

								TaskArgs taskArgs = TaskArgs.fromXML(t.taskArgs);

								TriggerDetail td = new TriggerDetail(
										t.triggerKey,
										t.taskClass,
										ct,
										taskArgs);

								if(scheduled.contains(td)){
									continue;
								}

								consumer.acceptTask(td);
							}
						}
					}.call();
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
			scheduled.add(triggerDetail);
		}
	}

	@Override
	public void finished(final TriggerDetail triggerDetail, boolean success) {

		synchronized(scheduled){
			scheduled.remove(triggerDetail);

			if(success){
				new RobustJob(){
					public void tryJob(){
						TriggerEntry t =
								TriggerEntry.find("byTriggerKeyAndTaskArgsElike",
										triggerDetail.getTriggerKey(),
										triggerDetail.getTaskArgs().toXML()).first();
						if(t != null){
							CronTrigger ct = CronTrigger.find("byTriggerEntry", t).first();				

							if(!triggerDetail.getTrigger().reschedule){	

								//Only supporting cron triggers right now so ct shouldn't be null
								if(ct != null){
									ct.delete();
								}

								t.delete();
								return;
							}

							//Only supporting cron triggers right now so ct shouldn't be null
							if(ct!= null){
								ct.previousExecution = triggerDetail.getTrigger().previousExecution;					
								ct.save();
							}
						}
					}
				}.call();
			}
		}
	}

	@Override
	public void clearSchedule() {
		new Job(){
			public void doJob(){

				new MultiNodeRunnable("DBTriggerManager.ClearSchedule",
						new Runnable(){
					public void run(){
						//TODO: update query
						List<TriggerEntry> all = TriggerEntry.findAll();
						for(TriggerEntry e : all){
							e.scheduled = false;
							e.save();
						}
					}
				}).run();
			}
		}.call();

	}

	@Override
	public TriggerDetail get(String triggerKey) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void die() {
		die.set(true);
	}



}
