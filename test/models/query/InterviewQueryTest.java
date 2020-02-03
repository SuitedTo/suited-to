package models.query;

import java.util.Hashtable;
import java.util.Map;

import enums.RoleValue;
import models.Company;
import models.Interview;
import models.User;
import models.query.interview.AccessibleInterviews;
import models.query.question.InactiveQuestions;

import org.junit.Before;
import org.junit.Test;

public class InterviewQueryTest extends QueryTest {
	@Before
    public void setup() {
		super.setup("models/query/test-interview-queries.yml", "models/query/test-interview-queries.drl");
	}
	
	@Test
	public void testAccessible(){
		reset(new AccessibleInterviews(getUser("appAdmin"), null, null,
                null, null, null));
		
		Map<String, Object> args = new Hashtable<String, Object>();
		args.put("user", getUser("appAdmin"));
        testExecution("Accessible Interviews - APP_ADMIN", args);
        
        
        reset(new AccessibleInterviews(getUser("oneCompanyUser"), null, null,
                null, null, null));
		args.put("user", getUser("oneCompanyUser"));
        testExecution("Accessible Interviews - NON_ADMIN", args);
	}

	@Override
	protected Class getEntityClass() {
		return Interview.class;
	}
}
