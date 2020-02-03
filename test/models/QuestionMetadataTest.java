package models;

import org.junit.Before;
import org.junit.Test;
import play.test.Fixtures;
import play.test.UnitTest;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

public class QuestionMetadataTest extends UnitTest {
    private User user;
    private Question testQuestion;

    @Before
    public void setup() {
        Fixtures.deleteDatabase();
        Fixtures.loadModels("data.yml");
        user = User.all().first();
        testQuestion = Question.all().first();
    }

    //create a questionMetadata and ensure that the user and question are correctly associated
    @Test
    public void testCreate() {
        QuestionMetadata testSubject = new QuestionMetadata(testQuestion, user);
        testSubject.save();

        QuestionMetadata result = QuestionMetadata.find("byUser", user).first();
        assertThat(result, is(notNullValue()));
        assertThat(result.question, is(testQuestion));

    }

    //delete a question metadata and ensure that the user and question are still present
    @Test
    public void testDelete() {
        QuestionMetadata testSubject = new QuestionMetadata(testQuestion, user);
        testSubject.save();

        testSubject.delete();

        assertThat(testQuestion, is(notNullValue()));
        assertThat(user, is(notNullValue()));

    }


}
