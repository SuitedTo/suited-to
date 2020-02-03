package models.query;

import models.Category;
import models.Category.CategoryStatus;
import models.Company;
import models.User;
import models.query.category.AccessibleCategories;

import org.junit.Before;
import org.junit.Test;

public class CategoryQueryTest extends QueryTest<Category> {

	@Before
    public void setup() {
		super.setup("models/query/test-category-queries.yml");
	}

    @Test
    public void testAccessibleCategories() {

        reset(new AccessibleCategories(getUser("me"), null, null,
                null, null, null));

        shouldSelect(
        		"publicCategory",
        		"betaCategory",
        		"newCoworkerCreatedCategory",
        		"privateCoworkerCreatedCategory"
        		);
        
        shouldNotSelect(
        		"newNonCoworkerCreatedCategory",
        		"privateNonCoworkerCreatedCategory"
        		);
        
        testExecution();
    }
    
    protected void shouldSelect(String... keys){
    	for(String key : keys){
    		shouldSelect(getCategory(key));
    	}
    }
    
    protected void shouldNotSelect(String... keys){
    	for(String key : keys){
    		shouldNotSelect(getCategory(key));
    	}
    }

	@Override
	protected Class<Category> getEntityClass() {
		return Category.class;
	}
}
