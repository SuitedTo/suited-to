/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tasks;

import com.google.gson.Gson;
import enums.QuestionStatus;
import models.QuestionWorkflow;
import models.Story;
import newsfeed.metadata.QuestionCountStoryMetadata;
import play.db.jpa.JPA;
import scheduler.Schedule;
import scheduler.Task;
import scheduler.TaskExecutionContext;
import utils.CriteriaHelper;
import utils.CriteriaHelper.TableKey;

import javax.persistence.criteria.CriteriaBuilder;
import java.util.*;

@Schedule(on="0 0 6 ? * MON")//Run every monday at 6am
public class WeeklyNewsfeedStoriesTask extends Task {

    public WeeklyNewsfeedStoriesTask(TaskExecutionContext context) {
        super(context);
    }
    
    @Override
    public void doTask() {
        
        Calendar c = new GregorianCalendar();
        c.setTime(new Date());
        c.add(Calendar.DATE, -7);
        Date weekAgo = c.getTime();
        
        CriteriaBuilder builder = JPA.em().getCriteriaBuilder();
        CriteriaHelper query = new CriteriaHelper();
        TableKey questionWorkflowKey = 
                query.addSourceEntity(QuestionWorkflow.class);
        query.addCondition(builder.equal(
                query.field(questionWorkflowKey, "status"), 
                QuestionStatus.ACCEPTED));
        query.addCondition(builder.greaterThan(
                query.<Date>field(questionWorkflowKey, "created"), weekAgo));

        long questionCount = query.count();
        //only create the story if there is more than one question
        if(questionCount > 0){
            new Story(1, 1, new Gson().toJson(new QuestionCountStoryMetadata(questionCount))).save();
        }

    }
}
