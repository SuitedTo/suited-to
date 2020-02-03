package controllers;

import integration.classmarker.ClassMarkerService;
import integration.classmarker.ClassMarkerService.QuizResult;
import models.EEOCQuizResultNotification;
import models.EEOCQuizWorkflow;
import models.User;
import play.jobs.Job;
import java.util.List;

public class Quizes extends ControllerBase{

	public static void finish(final String quiz, final Long cm_user_id){
		//https://app.suitedto.com/quizes/finish&quiz=hrcompliance?cm_user_id=1234&cm_ts=3&cm_tp=100&cm_td=0-0-12

		if((quiz == null) || (cm_user_id == null)){
			return;
		}

        //Need to go get the score using the classmarker api. We could use the cm_ts
        //param but then a user could give himself any score with this method.
        QuizResult result = await(
                new Job<QuizResult>(){

                    public QuizResult doJobWithResult(){
                        return ClassMarkerService.getQuizResult(cm_user_id);
                    }
                }.now());

        User user = User.findById(cm_user_id);

        if("hrcompliance".equals(quiz.toLowerCase()) &&
                (result != null)) {
            int minPassing = 80;
            if(user != null){
                user.hrCompliant = result.getScore() >= minPassing;
                //Remove any "You failed" notificaitons"
                if(user.hrCompliant) {
                    List<EEOCQuizResultNotification> failedNotifications = EEOCQuizResultNotification.find("byUser", user).fetch();
                    for(EEOCQuizResultNotification notification: failedNotifications) {
                        notification.workflow.notifications.remove(notification);
                        notification.delete();
                    }
                }
                EEOCQuizWorkflow workflow = new EEOCQuizWorkflow(user.hrCompliant);
                workflow.save();
                EEOCQuizResultNotification notification = new EEOCQuizResultNotification(user, workflow);
                notification.save();
                user.notifications.add(notification);
                user.save();
            }
        }

        Application.home();
	}
	
	public static void show(String name, String key){
		
		renderArgs.put("title", name);
		renderArgs.put("key", key);
		render();
	}
}
