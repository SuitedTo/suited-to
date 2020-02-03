package tasks;

import java.util.List;

import play.Logger;
import models.Question;
import models.User;
import scheduler.Task;
import scheduler.TaskExecutionContext;

public class UpdateUserMetricsTask extends Task {

	public UpdateUserMetricsTask(TaskExecutionContext context) {
        super(context);
    }
    
    @Override
    public void doTask(){
        super.doTask();
        Object arg = getArg("userIds");
    	if(arg != null){
    		 List userIds = (List)arg;
    		 for(Object id : userIds){
    			 try{
    				 User user = User.findById(id);
    			 	if(user != null){
    			 		user.metrics.update();
    			 	}
    			 }catch(javax.persistence.NoResultException nre){
    				 Logger.error("Unable to find user[%s]. Metrics will not be updated.", id);
    			 }
    		 }
    	}else{
    		User.initMetrics();
    	}
    }
}
