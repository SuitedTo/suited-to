package models.query;

import models.Question;
import models.query.question.AccessibleQuestions;
import models.query.question.InactiveQuestions;
import models.query.question.NeedsReview;
import org.junit.Before;
import org.junit.Test;

public class QuestionQueryTest extends QueryTest<Question> {
    
    @Before
    public void setup() {
		super.setup("models/query/test-question-queries.yml", "models/query/test-question-queries.drl");
	}
    
    @Test
    public void testInactive() {
    	
        reset(new InactiveQuestions(getUser("me"), null, null,
                null, null, null));

        testExecution("Inactive Questions");
    }
	
	@Test
    public void testAccessible() {
    	
        reset(new AccessibleQuestions(getUser("me"), null, null,
                null, null, null));
        
        shouldSelect(
        		"privateCoworkerQuestion",
        		"nonCoworkerAcceptedQuestion"
        		);
        
        shouldNotSelect(
        		"privateNonCoworkerQuestion"
        		);
        
        
        //Pivotal bug 32358511
//        shouldSelect("nonCoworkerSubmittedReviewableQuestion");

        testExecution();
    }
    
    @Test
    public void testNeedsReview() {
    	
        reset(new NeedsReview(getUser("me"),
        		null, null, null, null, null));
        
        shouldSelect(
        		"coworkerSubmittedReviewableQuestion",
        		"nonCoworkerSubmittedReviewableQuestion");
        

        //Is this right? Seems like this should be selected.
        shouldNotSelect("privateCoworkerQuestion");
        
        testExecution();
    }
    
    protected void shouldSelect(String... keys){
    	for(String key : keys){
    		shouldSelect(getQuestion(key));
    	}
    }
    
    protected void shouldNotSelect(String... keys){
    	for(String key : keys){
    		shouldNotSelect(getQuestion(key));
    	}
    }

	@Override
	protected Class<Question> getEntityClass() {
		return Question.class;
	}
}
