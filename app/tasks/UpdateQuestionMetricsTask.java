/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tasks;

import models.CronTrigger;
import models.Question;
import play.Logger;
import scheduler.Task;
import scheduler.TaskArgs;
import scheduler.TaskExecutionContext;
import scheduler.TaskScheduler;
import utils.BatchProcessingIterator;
import utils.LoggingUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class UpdateQuestionMetricsTask extends Task {

    public UpdateQuestionMetricsTask(TaskExecutionContext context) {
        super(context);
    }
    
    @Override
    public void doTask(){
        Logger.info("Running UpdateQuestionMetricsTask");
        Date start = new Date();
        super.doTask();
    	Object arg = getArg("questionIds");
    	if(arg != null){
    		 List questionIds = (List)arg;
    		 for(Object id : questionIds){
    			 try{
    				 Question question = Question.findById(id);
    			 	if(question != null){
    			 		Question.initMetrics(question);
    			 	}
    			 }catch(javax.persistence.NoResultException nre){
    				 Logger.error("Unable to find question[%s]. Metrics will not be updated.", id);
    			 }
    		 }
    	}else{
    		List<Question> questions = Question.findAll();
    		final int originalCount = questions.size();

    		//Could do an isolated save for each question but transactions aren't cheap and was
    		//taking quite a while so...
    		BatchProcessingIterator<Question, TaskArgs> it =
    				new BatchProcessingIterator<Question, TaskArgs>(questions, 25){

				@Override
				public Iterator<TaskArgs> processNextBatch(List<Question> nextBatch) {
					List<Long> ids = new ArrayList<Long>();

					for(Question q : nextBatch){
	    				ids.add(q.id);
	    			}
					List<TaskArgs> argsList = new ArrayList<TaskArgs>();
	    			TaskArgs args = new TaskArgs();
	    			args.add("questionIds", ids);
	    			argsList.add(args);
	    			return argsList.iterator();
				}
    		};

    		int count = 0;
    		while(it.hasNext()){
    			TaskArgs args = it.next();
    			Object o = args.getArg("questionIds");
    	    	if(o != null){
    	    		 count += ((List)o).size();
    	    	}
    			TaskScheduler.schedule(UpdateQuestionMetricsTask.class, args, CronTrigger.getASAPTrigger());
    		}
    		if(count != originalCount){
    			Logger.error("Updated %s of the question metrics but should have updated %s", count, originalCount);
    		} else{
    			Logger.info("Successfully updated all question metrics");
    		}
    	}

        Date end = new Date();
        LoggingUtil.logElapsedTime("UpdateQuestionMetricsTask.doTask", start, end);
    }
}
