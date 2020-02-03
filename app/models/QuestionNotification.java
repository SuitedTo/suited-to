package models;

import javax.persistence.Entity;
import java.util.ArrayList;
import java.util.List;

@Entity
public class QuestionNotification extends Notification {

    public QuestionNotification(User user, QuestionWorkflow questionWorkflow){
        super(user, questionWorkflow);
    }

    public static List<Notification> getQuestionNotifications(Question question) {
        List<Notification> notificationList = new ArrayList<Notification>();
        List<QuestionWorkflow> questionWorkflows = question.workflows;
        for (QuestionWorkflow questionWorkflow : questionWorkflows) {
            for (Notification notification : questionWorkflow.notifications) {
                notificationList.add(notification);
            }
        }

        return notificationList;
    }

    public static void deleteExistingNotifications(Question question) {

    }
}
