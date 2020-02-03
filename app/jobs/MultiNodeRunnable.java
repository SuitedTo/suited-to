package jobs;

import java.io.IOException;

import play.Logger;
import play.cache.Cache;
import play.jobs.Job;



/**
 * Wrapper to ensure that only one server instance runs the
 * job.
 * 
 * 
 * @author joel
 *
 */
public class MultiNodeRunnable implements Runnable{
	
	protected final String jobKey;
	protected final Runnable job;
	
	public MultiNodeRunnable(String jobKey, Runnable job){
		this.jobKey = jobKey;
		this.job = job;
	}

	public void run() {
		//I think this is OK as long as the key uniquely identifies every
		//individual execution of the wrapped runnable
		if(Cache.safeAdd(jobKey, "-1","10mn")){
			try {
				job.run();
				Logger.debug("Ran job %s", jobKey);
			} catch (Exception e) {
				Logger.error("Multi node job failed [%s]", jobKey);
			}
		}
	}
}