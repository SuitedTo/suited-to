package newsfeed.metadata;

import enums.StoryType;

public class StoryMetadata {
    private StoryType storyType;

    protected StoryMetadata(StoryType storyType) {
        this.storyType = storyType;
    }

    public StoryMetadata(){}

    public StoryType getStoryType() {
        return storyType;
    }
}
