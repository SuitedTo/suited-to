package models;

import org.junit.Before;
import org.junit.Test;
import play.test.Fixtures;
import play.test.UnitTest;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

public class InterviewTest extends UnitTest {

    private Interview testInterview;

    @Before
    public void setup() {
        Fixtures.deleteDatabase();
        Fixtures.loadModels("data.yml");
        testInterview = Interview.find("byName", "A Test Interview").first();
    }

    @Test
    public void testGetQuestions() {
        List<Question> questions = testInterview.getQuestions();
        assertThat(questions, is(notNullValue()));
        Question testQuestion1 = Question.find("byText", "testQuestion1").first();
        Question testQuestion2 = Question.find("byText", "testQuestion2").first();

        assertThat(testInterview.interviewQuestions.size(), is(2));
        assertThat(testInterview.interviewQuestions.get(0).question, is(testQuestion1));
        assertThat(testInterview.interviewQuestions.get(1).question, is(testQuestion2));

        //switch the order
        testInterview.reorderQuestion(testQuestion2, 1);
        
        assertThat(testInterview.interviewQuestions.size(), is(2));
        assertThat(testInterview.interviewQuestions.get(0).question, is(testQuestion2));
        assertThat(testInterview.interviewQuestions.get(1).question, is(testQuestion1));


    }

    @Test
    public void testGetInterviewQuestions() {
        //bug when questions have categories returning duplicates
        Question testQuestion = new Question("sampleText", User.all().<User>first());
        Category category1 = new Category("category1").save();
        Category category2 = new Category("category2").save();
        testQuestion.category = category1;
        testQuestion.save();
        testInterview.interviewQuestions.clear();
        testInterview.addQuestionToInterview(testQuestion);
        testInterview.name = "TEST";
        testInterview.save();

        Interview resultInterview = Interview.find("byName", "TEST").first();

        List<InterviewQuestion> interviewQuestions = resultInterview.interviewQuestions;
        assertThat(interviewQuestions, is(notNullValue()));
        assertThat(interviewQuestions.size(), is(1));

    }

}
