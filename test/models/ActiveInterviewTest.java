package models;

import org.junit.Before;
import org.junit.Test;
import play.test.Fixtures;
import play.test.UnitTest;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

public class ActiveInterviewTest extends UnitTest {

    @Before
    public void before(){
        Fixtures.deleteDatabase();
    }

    @Test
    public void testDelete(){
        Fixtures.loadModels("model-relationship-data.yml");
        ActiveInterview testEntity = ActiveInterview.all().first();
        testEntity.delete();
    }

    @Test
    public void testFeedback(){
        Fixtures.loadModels("model-relationship-data.yml");
        ActiveInterview testEntity = ActiveInterview.all().first();
        assertThat(testEntity.feedback, is(notNullValue()));
        assertThat(testEntity.feedback.size(), is(1));
    }

}
