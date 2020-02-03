package models;

import java.util.concurrent.ExecutionException;

import play.jobs.Job;


public abstract class InlineJob {
	private Job job;

	public InlineJob(){
		this.job = new Job(){
			public void doJob(){
				doStuff();
			}
		};
	}
	protected abstract void doStuff();
	
	public final void invokeAndWait(){
		try {
			job.now().get();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
