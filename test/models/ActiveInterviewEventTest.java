package models;

import org.junit.Before;
import org.junit.Test;
import play.test.Fixtures;
import play.test.UnitTest;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

public class ActiveInterviewEventTest extends UnitTest {

    @Before
    public void before(){
        Fixtures.deleteDatabase();
    }

    @Test
    public void testAddAndRemoveEvent(){
        ActiveInterview activeInterview = new ActiveInterview();
        ActiveInterviewEvent activeInterviewEvent = new ActiveInterviewStateChange();
        activeInterviewEvent.activeInterview = activeInterview;
        activeInterview.events.add(activeInterviewEvent);

        activeInterview.save();

        ActiveInterview result = ActiveInterview.all().first();
        assertThat(result.events, is(notNullValue()));
        assertThat(result.events.size(), is(1));

        result.events.clear();
        result.save();

        ActiveInterview result2 = ActiveInterview.all().first();
        assertThat(result2.events, is(notNullValue()));
        assertThat(result2.events.size(), is(0));

    }

    @Test
    public void testWorkflowAssociation(){
        ActiveInterview activeInterview = new ActiveInterview();
        ActiveInterviewEvent activeInterviewEvent = new ActiveInterviewStateChange();
        activeInterviewEvent.activeInterview = activeInterview;

        ActiveInterviewWorkflow workflow = new ActiveInterviewWorkflow(activeInterviewEvent);
        activeInterviewEvent.workflows.add(workflow);
        activeInterview.events.add(activeInterviewEvent);

        activeInterview.save();

        ActiveInterview result = ActiveInterview.all().first();
        ActiveInterviewEvent resultEvent = result.events.get(0);
        ActiveInterviewWorkflow resultWorkflow = resultEvent.workflows.get(0);
        assertThat(resultWorkflow, is(notNullValue()));
    }



}
