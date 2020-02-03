package newsfeed.display;

import com.google.gson.Gson;
import enums.StoryType;
import models.Story;
import models.User;
import newsfeed.metadata.QuestionCountStoryMetadata;
import play.i18n.Messages;

public class WeeklyQuestionCountStoryDisplay extends StoryDisplay {

    private transient QuestionCountStoryMetadata metadata;

    public WeeklyQuestionCountStoryDisplay(User userInContext, Story story, Boolean thirdPersonMessages) {
        super(userInContext, story, thirdPersonMessages);
    }

    private QuestionCountStoryMetadata getMetadata(){
        if(this.metadata == null){
            this.metadata = new Gson().fromJson(story.metadata, QuestionCountStoryMetadata.class);
        }

        return this.metadata;
    }



    @Override
    protected String buildMessage() {
        return Messages.get(getMessageKey(), getMetadata().getQuestionCount());
    }


    @Override
    protected String determineType() {
        return StoryType.WEEKLY_QUESTIONS.name();
    }
}
