package newsfeed.metadata;

import java.util.List;

public class ProInterviewerEventMetadata extends EventMetadata {
    private List<Long> categoryIds;
    private List<String> categoryNames;

    public List<Long> getCategoryIds() {
        return categoryIds;
    }

    public void setCategoryIds(List<Long> categoryIds) {
        this.categoryIds = categoryIds;
    }

    public List<String> getCategoryNames() {
        return categoryNames;
    }

    public void setCategoryNames(List<String> categoryNames) {
        this.categoryNames = categoryNames;
    }
}
