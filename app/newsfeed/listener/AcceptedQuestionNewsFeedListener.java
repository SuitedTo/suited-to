package newsfeed.listener;

import enums.EventType;
import enums.QuestionStatus;
import models.Event;
import models.Question;
import newsfeed.metadata.QuestionWorkflowEventMetadata;

import java.beans.PropertyChangeEvent;

public class AcceptedQuestionNewsFeedListener extends NewsFeedEventListener {
    private static final String STATUS_FIELD = "status";

    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        Object source = propertyChangeEvent.getSource();
        if(!(source instanceof Question)){
            throw new IllegalStateException(this.getClass().getName() + " registered with invalid Type.  " +
                    "Expected " + Question.class.getName() + " found " + source.getClass().getName());
        }
        Question question = (Question) source;

        final String propertyName = propertyChangeEvent.getPropertyName();
        if(propertyName.equals(STATUS_FIELD)){
            QuestionStatus newValue = (QuestionStatus) propertyChangeEvent.getNewValue();
            QuestionStatus oldValue = (QuestionStatus) propertyChangeEvent.getOldValue();

            if(!newValue.equals(oldValue) && QuestionStatus.ACCEPTED.equals(newValue)){
                Event event = new Event();
                event.eventType = EventType.QUESTION_WORKFLOW;
                event.relatedUser = question.user;
                //build the event metadata
                QuestionWorkflowEventMetadata metadata = new QuestionWorkflowEventMetadata();
                metadata.setNewStatus(newValue);
                metadata.setQuestionId(question.id);
                event.serializeAndSetEventMetadata(metadata);

                registerEvent(question, event, propertyChangeEvent);
            }
        }
    }
}
