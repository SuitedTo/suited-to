
package models.tables.question;

import models.Category;
import models.User;
import models.filter.question.ByDifficulty;
import models.filter.question.ById;
import models.filter.question.ByStatus;
import models.filter.question.ByTime;
import models.query.question.AccessibleQuestions;
import models.tables.QueryBaseTable;
import utils.CriteriaHelper.RootTableKey;
import utils.CriteriaHelper.TableKey;
import utils.ObjectTransformer;

public class QuestionTable extends QueryBaseTable {

    public QuestionTable() {
        super(new AccessibleQuestions());
        
        RootTableKey root = getQueryBaseRootKey();
        TableKey category = join(root, "category");
        
        addColumn(root, "id");
        addColumn(root, "status", ObjectToMessage.INSTANCE);
        addColumn(root, "text");
        addColumn(root, "time", ObjectToMessage.INSTANCE);
        addColumn(root, "difficulty", ObjectToMessage.INSTANCE);
        addColumn(root, "standardScore", new DefaultForNull((Integer) 0));
        addColumn(category, CategoryLabelBuilder.INSTANCE);
        addColumn(category, "name");
        
        includeAsExactMatchSearch(root, new ById());
        includeAsExactMatchSearch(root, new ByStatus());
        includeInSearch(field(root, "text", String.class));
        includeAsExactMatchSearch(root, new ByTime());
        includeAsExactMatchSearch(root, new ByDifficulty());
        includeInSearch(field(category, "name", String.class));
    }
    
    @Override
    public boolean canAccess(User u) {
        return u != null;
    }
    
    private static class CategoryLabelBuilder implements ObjectTransformer {

        public static final ObjectTransformer INSTANCE = 
                new CategoryLabelBuilder();
        
        public Object transform(Object input) {
            String result;
            
            if (input == null) {
                result = "";
            }
            else {
                Category c = (Category) input;
                result = c.name + " (" + c.questionCount + ")";
            }
            
            return result;
        }
    }
}
