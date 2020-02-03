package models.events.sinks;

import enums.QuestionStatus;
import models.Question;
import models.QuestionWorkflow;
import models.User;
import models.UserMetrics;
import models.events.EntityEvent;
import models.events.EntityEventSink;
import models.events.EntityUpdated;
import models.events.EventSinkAttributes;
import play.jobs.Job;

public class AcceptedQuestionsCounter implements EntityEventSink{

	@Override
	public EventSinkAttributes getAttributes() {
		EventSinkAttributes attr = new EventSinkAttributes();
		attr.addEventPattern(Question.class).updated("status");
		return attr;
	}

	@Override
	public void processEvent(final EntityEvent event) {

		final Question question = (Question)event.getSource();
		final EntityUpdated eu = (EntityUpdated)event;
		if(QuestionStatus.ACCEPTED.equals(eu.getNewValue())){
			new Job(){
				public void doJob(){
					UserMetrics um = UserMetrics.find("byUser", question.user).first();
					if(um != null){
						um.incrementNumberOfAcceptedQuestions();
	        			um.save();
					}
				}
			}.now();
		}
	}
}
