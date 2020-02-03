package newsfeed.metadata;

import enums.StoryType;
import models.Category;

import java.util.ArrayList;
import java.util.List;

public class TopCategoriesStoryMetadata extends StoryMetadata {
    private final List<Long> categoryIds;
    private final List<String> categoryNames;

    public TopCategoriesStoryMetadata(List<Category> categories) {
        super(StoryType.TOP_CATEGORIES);
        categoryIds = new ArrayList<Long>();
        categoryNames = new ArrayList<String>();
        for (Category category : categories) {
            categoryIds.add(category.id);
            categoryNames.add(category.name);
        }
    }

    public List<Long> getCategoryIds() {
        return categoryIds;
    }

    public List<String> getCategoryNames() {
        return categoryNames;
    }
}
