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
import models.events.EntityUpdated;
import models.events.EntityEvent.EntityEventType;
import models.events.EntityEventSink;
import models.events.EventSinkAttributes;
import play.Logger;
import play.jobs.Job;

public class QuestionActiveChangeManager implements EntityEventSink{

	@Override
	public EventSinkAttributes getAttributes() {
		EventSinkAttributes attr = new EventSinkAttributes();
		attr.addEventPattern(Question.class).deleted();
		attr.addEventPattern(Question.class).updated("active");
		return attr;
	}

	@Override
	public void processEvent(final EntityEvent ee) {

		new Job(){
			public void doJob(){
				Question question = (Question) ee.getSource();
				User user = User.findById(question.user.id);
				if(user != null){
					user.metrics.update();
					user.updateBadges();
				}
			}
		}.now();
	}
}
