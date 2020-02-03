package jobs;

import java.util.List;

import models.User;
import play.jobs.Job;

public class UpdateUserMetrics extends Job{

	@Override
    public void doJob() throws Exception {
		User.initMetrics();
	}
	
}
