package models.events.sinks;

import java.util.List;

import enums.QuestionStatus;

import models.Notification;
import models.Question;
import models.QuestionNotification;
import models.QuestionWorkflow;
import models.User;
import models.events.EntityCreated;
import models.events.EntityEvent;
import models.events.EntityEventSink;
import models.events.EventSinkAttributes;
import play.Logger;
import play.jobs.Job;

public class QuestionStatusChangeManager implements EntityEventSink{

	@Override
	public EventSinkAttributes getAttributes() {
		EventSinkAttributes attr = new EventSinkAttributes();
		attr.addEventPattern(Question.class).created();
		attr.addEventPattern(Question.class).updated("status");
		attr.addEventPattern(QuestionWorkflow.class).created();
		return attr;
	}

	@Override
	public void processEvent(final EntityEvent event) {

		if(event.getSource() instanceof Question){
			try {
				new Job(){
					public void doJob(){
						List changeContextList = ((Question)event.getSource()).eventContext;
						for(Object changeContext : changeContextList){
							if((changeContext != null) && (changeContext instanceof QuestionWorkflow)){
								QuestionWorkflow wf = ((QuestionWorkflow)changeContext);
								if(wf.hasBeenSaved()){
									Logger.error("Not expecting a saved workflow %s", wf);
								} else {
									wf.save();
								}
							}
						}
					}
				}.now()
				
				/*
				 * Some tests and follow up invocations (redirects) depend on the workflow list
				 * so wait here.
				 */
				.get();
			} catch (Exception e) {
				e.printStackTrace();
			}

		} else if(event.getSource() instanceof QuestionWorkflow){

			new Job(){
				public void doJob(){
					QuestionWorkflow wf = QuestionWorkflow.findById(((QuestionWorkflow)event.getSource()).id);
					if(wf != null){
						QuestionStatus status = wf.status;
						Question question = Question.findById(wf.question.id);
						List<Notification> existingNotifications = QuestionNotification.getQuestionNotifications(question);
						for (Notification notification : existingNotifications) {
							notification.workflow.notifications.remove(notification);
							notification.delete();
						}

						switch (status) {
						case ACCEPTED:
							break;
						case RETURNED_TO_SUBMITTER:
							new QuestionNotification(question.user, wf).save();
							break;
						case OUT_FOR_REVIEW:
							//if the question has previously been returned to submitter add a notification to the reviewer that the
							//question is ready to be reviewed again
							break;
						case WITHDRAWN:
						default:
							break;
						}
					}
				}
			}.now();
		}
	}
}
