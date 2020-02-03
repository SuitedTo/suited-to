package newsfeed.display;

import com.google.gson.Gson;
import models.Question;
import models.Story;
import models.User;
import newsfeed.metadata.QuestionWorkflowEventMetadata;
import org.apache.commons.lang.StringEscapeUtils;
import play.i18n.Messages;
import play.mvc.Router;

import java.util.HashMap;
import java.util.Map;

public class AcceptedQuestionStoryDisplay extends ShareableStoryDisplay{

    private transient QuestionWorkflowEventMetadata metadata;
    private transient Question question;

    public AcceptedQuestionStoryDisplay(User userInContext, Story story, Boolean thirdPersonMessages) {
        super(userInContext, story, thirdPersonMessages);
    }

    private QuestionWorkflowEventMetadata getMetadata() {
        if(this.metadata == null){
            this.metadata = new Gson().fromJson(story.event.metadata, QuestionWorkflowEventMetadata.class);
        }
        return this.metadata;
    }

    private Question getQuestion() {
        if(this.question == null){
            this.question = Question.findById(getMetadata().getQuestionId());
        }
        return this.question;
    }

    @Override
    protected String buildMessage() {
        Map<String, Object> args = new HashMap<String, Object>();
        Question question = getQuestion();
        args.put("id", question.id);
        String url = Router.getFullUrl("Questions.view", args);
        if(useFirstPersonMessages()){
            return Messages.get(getMessageKey(), url, 
                    StringEscapeUtils.escapeHtml(question.text));
        } else {
            return Messages.get(getMessageKey(), getEventRelatedUserDisplay(), url,
                    StringEscapeUtils.escapeHtml(question.text));
        }

    }

    @Override
    protected ShareMessage buildStandardShareMessage() {
        return buildDefaultShareMessage();
    }

    @Override
    protected ShareMessage buildTwitterShareMessage() {
        return buildDefaultShareMessage();
    }
}
