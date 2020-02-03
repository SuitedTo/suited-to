package enums;

import newsfeed.display.TopCategoriesStoryDisplay;
import newsfeed.display.WeeklyQuestionCountStoryDisplay;

/**
 * Marker for NewsFeed Stories that do not have associated events.  Typically stored as part of the Story's metadata
 */
public enum StoryType {


    WEEKLY_QUESTIONS(WeeklyQuestionCountStoryDisplay.class),
    TOP_CATEGORIES(TopCategoriesStoryDisplay.class);

    private final Class defaultStoryDisplayType;

    private StoryType(Class defaultStoryDisplayType) {
        this.defaultStoryDisplayType = defaultStoryDisplayType;
    }

    public Class getDefaultStoryDisplayType() {
        return defaultStoryDisplayType;
    }
}
