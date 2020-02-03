package newsfeed.metadata;

import enums.QuestionStatus;

public class QuestionWorkflowEventMetadata extends EventMetadata {
    private Long questionId;
    private QuestionStatus newStatus;

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public QuestionStatus getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(QuestionStatus newStatus) {
        this.newStatus = newStatus;
    }
}
