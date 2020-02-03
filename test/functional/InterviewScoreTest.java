package functional;

import enums.UserStatus;
import logic.questions.scoring.QuestionScoreCalculator;
import models.Interview;
import models.Question;
import models.User;
import org.junit.Before;
import org.junit.Test;

/**
 * Functional test for interview score.
 *
 * @author joel
 */
public class InterviewScoreTest extends FunctionalBase {

    private static final int USER_RATED_UP = QuestionScoreCalculator.IS_USER_RATED_UP;
    private static final int USER_RATED_DOWN = QuestionScoreCalculator.IS_USER_RATED_DOWN;
    private static final int COWORKER_RATING = QuestionScoreCalculator.IS_COWORKER_RATING;
    private static final int NON_COWORKER_RATING = QuestionScoreCalculator.IS_NON_COWORKER_RATING;
    private static final int INTERVIEWS = QuestionScoreCalculator.IS_INTERVIEWS;
    private static final int COWORKER_CREATED = QuestionScoreCalculator.IS_COWORKER_CREATED;

    private Question testQuestion;

    @Before
    public void setup() {

        Question.initMetrics();
        testQuestion = Question.all().first();
    }

    @Test
    public void testUserRateUp() {
        int originalScore = QuestionScoreCalculator.calculateInterviewScore(testQuestion, user);
        int originalRating = testQuestion.totalRating;

        GET("/questions/rateQuestion?id=" + testQuestion.id + "&rating=" + (1 + originalRating));

        //ensure that the score went up as it should
        assertTrue(QuestionScoreCalculator.calculateInterviewScore(testQuestion, user) == (originalScore + USER_RATED_UP));
    }

    @Test
    public void testUserRateDown() {
        int originalScore = QuestionScoreCalculator.calculateInterviewScore(testQuestion, user);
        int originalRating = testQuestion.totalRating;

        GET("/questions/rateQuestion?id=" + testQuestion.id + "&rating=" + (originalRating - 1));

        //ensure that the score went down as it should
        assertTrue(QuestionScoreCalculator.calculateInterviewScore(testQuestion, user) == (originalScore - USER_RATED_DOWN));
    }

    @Test
    public void testCoworkerRateUp() {
        int originalScore = QuestionScoreCalculator.calculateInterviewScore(testQuestion, user);
        int originalRating = testQuestion.totalRating;

        //log in as coworker
        login(createCoworker(user), "secret");

        //rate the question up
        GET("/questions/rateQuestion?id=" + testQuestion.id + "&rating=" + (1 + originalRating));

        //log in as user
        login(user, "secret");

        //ensure that the score went up as it should
        assertTrue(QuestionScoreCalculator.calculateInterviewScore(testQuestion, user) == (originalScore + COWORKER_RATING));
    }

    @Test
    public void testCoworkerRateDown() {
        int originalScore = QuestionScoreCalculator.calculateInterviewScore(testQuestion, user);
        int originalRating = testQuestion.totalRating;

        //log in as coworker
        login(createCoworker(user), "secret");

        //rate the question down
        GET("/questions/rateQuestion?id=" + testQuestion.id + "&rating=" + (originalRating - 1));

        //log in as user
        login(user, "secret");

        //ensure that the score went down as it should
        assertTrue(QuestionScoreCalculator.calculateInterviewScore(testQuestion, user) == (originalScore - COWORKER_RATING));
    }

    @Test
    public void testNonCoworkerRateUp() {
        int originalScore = QuestionScoreCalculator.calculateInterviewScore(testQuestion, user);
        int originalRating = testQuestion.totalRating;

        //log in as non coworker
        login(createNonCoworker(user), "secret");

        //rate the question up
        GET("/questions/rateQuestion?id=" + testQuestion.id + "&rating=" + (1 + originalRating));

        //log in as user
        login(user, "secret");

        //ensure that the score went up as it should
        assertTrue(QuestionScoreCalculator.calculateInterviewScore(testQuestion, user) == (originalScore + NON_COWORKER_RATING));
    }

    @Test
    public void testNonCoworkerRateDown() {
        int originalScore = QuestionScoreCalculator.calculateInterviewScore(testQuestion, user);
        int originalRating = testQuestion.totalRating;

        //log in as non coworker
        login(createNonCoworker(user), "secret");

        //rate the question down
        GET("/questions/rateQuestion?id=" + testQuestion.id + "&rating=" + (originalRating - 1));

        //log in as user
        login(user, "secret");

        //ensure that the score went down as it should
        assertTrue(QuestionScoreCalculator.calculateInterviewScore(testQuestion, user) == (originalScore - NON_COWORKER_RATING));
    }

    @Test
    public synchronized void testInterview() {
        int originalScore = QuestionScoreCalculator.calculateInterviewScore(testQuestion, user);

        //create a new interview
        Interview interview = new Interview(user);

        interview.name = "test interview";
        
        //add the test question to it
        interview.addQuestionToInterview(testQuestion);
        interview.save();
        
        testQuestion.refresh();

        //ensure that the score went up as it should
        assertTrue(QuestionScoreCalculator.calculateInterviewScore(testQuestion, user) == (originalScore + INTERVIEWS));
        
        //now remove the question
        interview.removeQuestionFromInterview(testQuestion);
        interview.save();
        
        testQuestion.refresh();

        //ensure that the score went back down
        assertTrue(QuestionScoreCalculator.calculateInterviewScore(testQuestion, user) == originalScore);
        
    }

//    @Test
    public void testCoworkerCreated() {

        //Create a question on behalf of a coworker
        Question question = createCoworkerQuestion(user);

        //ensure that the score is correct
        assertTrue(QuestionScoreCalculator.calculateInterviewScore(question, user) == COWORKER_CREATED);
    }

    private Question createCoworkerQuestion(User user) {
        User coworker = createCoworker(user);
        Question question = new Question("blah", coworker);
        question.save();
        return question;
    }

    private User createCoworker(User user) {
        User coworker = new User("coworker@mycompany.com", "secret", "Co Worker", user.company);
        coworker.status = UserStatus.ACTIVE;
        coworker.save();
        return coworker;
    }

    private User createNonCoworker(User user) {
        User noncoworker = new User("noncoworker@mycompany.com", "secret", "Non Co Worker", null);
        noncoworker.status = UserStatus.ACTIVE;
        noncoworker.save();
        return noncoworker;
    }

}
