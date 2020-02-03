package tasks;

import models.Category;
import play.Play;
import scheduler.Task;
import scheduler.TaskExecutionContext;

import java.util.Set;

public class CategoryCountTask extends Task {
    public static final String CATEGORY_ID_SET_ARG = "categoryIdSet";

    public CategoryCountTask(TaskExecutionContext context) {
        super(context);
    }

    @Override
    public void doTask() {
        super.doTask();
        if (!Play.runingInTestMode()) {
            Object categoryIdSetArg = getArg(CATEGORY_ID_SET_ARG);
            if(categoryIdSetArg != null){
                Set<Long> categoryIdSet = (Set<Long>)categoryIdSetArg;
                for (Long id : categoryIdSet) {
                    Category category = Category.findById((Long) id);
                    //possible for the task to be triggered but then the category deleted before the task is actually run
                    if(category != null){
                        category.updateQuestionCount();
                        category.isolatedSave();
                    }
                }

            }
        }
    }
}
