package tasks;

import models.Question;
import models.User;
import play.Logger;
import play.Play;
import scheduler.Task;
import scheduler.TaskExecutionContext;

import java.util.List;

public class StreetCredUpdateTask extends Task {

    public static final String USER_ID_ARG = "userId";
    public static final String QUESTION_ID_LIST_ARG = "questionIdList";

    public StreetCredUpdateTask(TaskExecutionContext context) {
        super(context);
    }

    @Override
    public void doTask() {
        super.doTask();

        //don't run this job in test mode but rely on specific invocation of street cred calculations where we care about
        //it.  Since this is asynchronous many times questions will have been removed from the database before the job
        //gets a chance to execute leading to errors
        if (!Play.runingInTestMode()) {

            Object userIdArg = getArg(USER_ID_ARG);
            Object questionIdListArg = getArg(QUESTION_ID_LIST_ARG);
            if (userIdArg != null) {
                User user = User.findById((Long)userIdArg);
                if(user != null){
                	user.updateStreetCred();
                	user.save();
                }else{
                	Logger.error("Could not update street cred for missing user: %s", String.valueOf(userIdArg));
                }
            } else if (questionIdListArg != null) {

                for (Long questionId : (List<Long>)questionIdListArg) {
                    Question question = null;
                    try {
                        question = Question.findById(questionId);
                    } catch (Exception e) {
                        Logger.warn(this.getClass().getName() + " could not load question with id: " + questionId + " The question may have been deleted before this task had a chance to run.");
                    }
                    if(question != null){
                        List<User> users = question.getReviewers();
                        users.add(question.user);
                        for (User user : users) {
                            user.updateStreetCred();
                            user.isolatedSave();
                        }
                    }
                }
            } else {
                List<User> users = User.<User>findAll();
                for(User user : users){
                    user.updateStreetCred();
                    user.isolatedSave();
                }
            }
        }
    }
}
