package jobs;

import java.util.List;

import models.ModelBase;
import models.User;
import play.jobs.Job;
import play.utils.Java;

public class UpdateUserBadges extends Job{

	@Override
    public void doJob() {
		List<User> users = User.findAll();
		for(User user : users){
			//TODO: Batch update as in updatequestionmetrics task
			new UpdateOne(user.id).call();
		}
	}
	
	public static class UpdateOne extends RobustJob{
		private final long id;
		
		public UpdateOne(long id){
			this.id = id;
		}
		
		@Override
	    public void tryJob() {
			User user = User.findById(id);
			if(user != null){
				user.updateBadges();
				user.save();
			}
		}
	}
	
}
