package newsfeed.metadata;

import enums.StoryType;

public class QuestionCountStoryMetadata extends StoryMetadata {
    private long questionCount;

    public QuestionCountStoryMetadata(long questionCount) {
        super(StoryType.WEEKLY_QUESTIONS);
        this.questionCount = questionCount;
    }

    public long getQuestionCount() {
        return questionCount;
    }
}
