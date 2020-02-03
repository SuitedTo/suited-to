package models.events.sinks;

import java.util.List;

import models.Category;
import models.Question;
import models.QuestionWorkflow;
import models.User;
import models.events.EntityEvent;
import models.events.EntityEventSink;
import models.events.EntityUpdated;
import models.events.EventSinkAttributes;
import notifiers.Mails;
import play.jobs.Job;
import utils.ListDiff;
import enums.QuestionStatus;

public class MailGenerator implements EntityEventSink{

	@Override
	public EventSinkAttributes getAttributes() {
		EventSinkAttributes attr = new EventSinkAttributes();
		attr.addEventPattern(User.class).updated("reviewCategories");
		attr.addEventPattern(Question.class).updated("status");
		return attr;
	}

	@Override
	public void processEvent(EntityEvent ee) {
		final EntityUpdated event = (EntityUpdated)ee;
		
		if((event.getSource() instanceof Question) && (event.getPropertyName().equals("status"))){
			
			new Job(){
				public void doJob(){
					Question question = (Question)event.getSource();
					QuestionStatus status = (QuestionStatus)event.getNewValue();
					
					if(question.user.questionStatusEmails){
						if(status.equals(QuestionStatus.ACCEPTED)){
							Mails.questionAccepted(question);
						}else if(status.equals(QuestionStatus.RETURNED_TO_SUBMITTER)){
							QuestionWorkflow wf = QuestionWorkflow.find("byQuestion_idAndStatus",
									question.id,QuestionStatus.RETURNED_TO_SUBMITTER).first();
							if(wf != null){
								Mails.questionRejected(question, wf.comment);
							}
						}
		            }
				}
			}.now();
			
		} else if((event.getSource() instanceof User) && (event.getPropertyName().equals("reviewCategories"))){
			User reviewer = (User) ((EntityUpdated)event).getSource();
			List<Category> previousValues = (List<Category>) ((EntityUpdated)event).getOldValue();
			List<Category> currentValues = (List<Category>) ((EntityUpdated)event).getNewValue();
			ListDiff<Category> diff = new ListDiff<Category>(previousValues, currentValues);
			List<Category> added = diff.added();
			if (added.size() > 1) {
				Mails.reviewerMulti(reviewer, added);
			} else if (added.size() == 1) {
                Mails.reviewer(reviewer, added.get(0));
            }
		}
	}

}
