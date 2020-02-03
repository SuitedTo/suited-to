package newsfeed.display;

import com.google.gson.Gson;
import enums.StoryType;
import models.Story;
import models.User;
import newsfeed.metadata.TopCategoriesStoryMetadata;
import org.apache.commons.lang.StringEscapeUtils;
import play.i18n.Messages;
import utils.DisplayUtil;

public class TopCategoriesStoryDisplay extends StoryDisplay {
    private transient TopCategoriesStoryMetadata metadata;

    public TopCategoriesStoryDisplay(User userInContext, Story story, Boolean thirdPersonMessages) {
        super(userInContext, story, thirdPersonMessages);
    }

    private TopCategoriesStoryMetadata getMetadata(){
        if(this.metadata == null){
            this.metadata = new Gson().fromJson(story.metadata, TopCategoriesStoryMetadata.class);
        }

        return this.metadata;
    }

    private String getCategoryDescription() {
        return StringEscapeUtils.escapeHtml(
                DisplayUtil.listAsProperlyPunctuatedEnglish(getMetadata().getCategoryNames()));
    }

    @Override
    protected String buildMessage() {
        return Messages.get(getMessageKey(), getMetadata().getCategoryIds().size(), getCategoryDescription());
    }



    @Override
    protected String determineType() {
        return StoryType.TOP_CATEGORIES.name();
    }
}
