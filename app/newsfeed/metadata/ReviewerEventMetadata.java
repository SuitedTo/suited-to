package newsfeed.metadata;

import java.util.List;

public class ReviewerEventMetadata extends EventMetadata {
    private List<Long> categoryIds;
    private List<String> categoryNames;
    private Boolean superReviewer;

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

    public Boolean getSuperReviewer() {
        return superReviewer;
    }

    public void setSuperReviewer(Boolean superReviewer) {
        this.superReviewer = superReviewer;
    }
}
