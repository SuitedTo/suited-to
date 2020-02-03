/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tasks;

import com.google.gson.Gson;
import models.Category;
import models.Question;
import models.Story;
import newsfeed.metadata.TopCategoriesStoryMetadata;
import play.Play;
import play.db.jpa.JPA;
import scheduler.Schedule;
import scheduler.Task;
import scheduler.TaskExecutionContext;
import utils.CriteriaHelper;
import utils.CriteriaHelper.TableKey;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

@Schedule(on="0 0 6 ? * FRI")//Run every friday at 6am
public class EndOfWeekNewsfeedStoriesTask extends Task {

    public EndOfWeekNewsfeedStoriesTask(TaskExecutionContext context) {
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
        TableKey questionKey = query.addSourceEntity(Question.class);
        TableKey categoryKey = query.join(questionKey, "category");
        query.addGroup(query.getEntity(categoryKey));
        query.addCondition(builder.greaterThan(
                query.<Date>field(questionKey, "created"), weekAgo));
        query.addCondition(builder.isNotNull(query.getEntity(categoryKey)));
        CriteriaQuery finalQuery = query.finish();
        finalQuery.orderBy(
                builder.desc(builder.count(query.getEntity(questionKey))));
        finalQuery.select(query.getEntity(categoryKey));


        List<Category> topCategories = 
                CriteriaHelper.getResultList(finalQuery, 0, 5, true);

        //only create the Story if there are some Categories in the list.
        if(topCategories.size() > 0){
            new Story(1, Double.valueOf(Play.configuration.getProperty("weight.TOP_CATEGORIES")),
                    new Gson().toJson(new TopCategoriesStoryMetadata(topCategories))).save();
        }
    }
}
