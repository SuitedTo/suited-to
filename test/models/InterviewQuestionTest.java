package models;

import org.junit.Before;
import org.junit.Test;
import play.test.Fixtures;
import play.test.UnitTest;

import java.util.Set;
import java.util.TreeSet;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

public class
        InterviewQuestionTest extends UnitTest {
    private Question testQuestion;
    private User user;
    private String test;

    @Before
    public void setup() {
        Fixtures.deleteDatabase();
        Fixtures.loadModels("data.yml");
        testQuestion = Question.all().first();
        user = User.all().first();
    }

    @Test
    public void testAssociation() {
        Interview interview = new Interview(user);
        interview.save();
        InterviewQuestion interviewQuestion = new InterviewQuestion();
        interviewQuestion.interview = interview;
        interviewQuestion.question = testQuestion;
        interviewQuestion.sortOrder = 2;
        interview.interviewQuestions.add(interviewQuestion);
        interview.save();


        InterviewQuestion result = interview.interviewQuestions.get(0);
        assertThat(result, is(notNullValue()));
        assertThat(result.sortOrder, is(2));
        assertThat(result.question, is(testQuestion));
    }

    @Test
    public void testSort() {
        InterviewQuestion a = new InterviewQuestion();
        a.sortOrder = 1;

        InterviewQuestion b = new InterviewQuestion();
        b.sortOrder = 1;

        assertThat(a.compareTo(b), is(0));

        a.sortOrder = 2;
        assertThat(a.compareTo(b), is(1));

        b.sortOrder = 3;
        assertThat(a.compareTo(b), is(-1));

        Set<InterviewQuestion> collection = new TreeSet<InterviewQuestion>();
        collection.add(a);
        collection.add(b);

        for (InterviewQuestion interviewQuestion : collection) {
            assertThat(interviewQuestion, is(a));
            break;
        }

        a.sortOrder = 4;
        collection.remove(a);
        collection.add(a);

        for (InterviewQuestion interviewQuestion : collection) {
            assertThat(interviewQuestion, is(b));
            break;
        }


    }
}
