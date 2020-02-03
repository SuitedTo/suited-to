package jobs;

import enums.QuestionStatus;
import models.Category;
import models.Question;
import play.jobs.Job;

import java.util.List;

public class CategoryStatsJob extends Job {

    @Override
    public void doJob() throws Exception {
        List<Category> allCategories = Category.findAll();
        for (Category category : allCategories) {
            category.questionCount = Question.count("category = ?1 and active = ?2 and status in(?3)", category, true, QuestionStatus.publiclyVisibleStatuses());
            category.isolatedSave();
        }
    }
}
