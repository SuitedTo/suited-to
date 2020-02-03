package newsfeed.display;

import com.google.gson.Gson;
import models.Story;
import models.User;
import newsfeed.metadata.ReviewerEventMetadata;
import org.apache.commons.lang.StringEscapeUtils;
import play.i18n.Messages;
import utils.DisplayUtil;

public class NewReviewerStoryDisplay extends ShareableStoryDisplay {
    private transient ReviewerEventMetadata metadata;

    public NewReviewerStoryDisplay(User userInContext, Story story, Boolean thirdPersonMessages) {
        super(userInContext, story, thirdPersonMessages);
    }

    private ReviewerEventMetadata getMetadata() {
        if (this.metadata == null) {
            this.metadata = new Gson().fromJson(story.event.metadata, ReviewerEventMetadata.class);
        }
        return this.metadata;
    }

    @Override
    protected String buildMessage() {
        String categoryDescription = getCategoryDescription();


        if (useFirstPersonMessages()) {
            return Messages.get(getMessageKey(), categoryDescription);
        } else {
            return Messages.get(getMessageKey(), getEventRelatedUserDisplay(),
                    categoryDescription);
        }

    }



    private String getCategoryDescription() {
        String categoryDescription;
        ReviewerEventMetadata metadata = getMetadata();
        if (Boolean.TRUE.equals(metadata.getSuperReviewer())) {
            categoryDescription = "all categories";
        } else {
            categoryDescription = StringEscapeUtils.escapeHtml(
                    DisplayUtil.listAsProperlyPunctuatedEnglish(metadata.getCategoryNames()));
        }
        return categoryDescription;
    }

    @Override
    protected ShareMessage buildStandardShareMessage() {
        return buildDefaultShareMessage(getCategoryDescription());
    }

    @Override
    protected ShareMessage buildTwitterShareMessage() {
        return buildDefaultShareMessage(getCategoryDescription());
    }


}
