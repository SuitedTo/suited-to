import data.binding.Binders;
import models.User;
import play.Play;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import play.test.Fixtures;

@OnApplicationStart
public class Bootstrap extends Job {

    public void doJob() {
        // Check if the database is empty
        if(User.count() == 0 && !Play.runingInTestMode()) {
            Fixtures.loadModels("initial-data.yml", "automation-data.yml");
        }
        
        //register all custom binders
        Binders.registerAll();
        
        //initialize all question metrics and scores/points
        //Question.initMetrics();
        
        //initialize aggregate data for interviews
        //Interview.init();
    }
}
