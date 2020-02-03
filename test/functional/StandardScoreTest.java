package functional;

import logic.questions.scoring.QuestionScoreCalculator;
import models.Interview;
import models.Question;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Functional test for standard score.
 *
 * @author joel
 */
public class StandardScoreTest extends FunctionalBase {

    private final static int RATING_POINTS = QuestionScoreCalculator.SS_POINTS_PER_RATING;
    private final static int INTERVIEW_POINTS = QuestionScoreCalculator.SS_POINTS_PER_INTERVIEW;

    private Question testQuestion;

    @Before
    public void setup() {
        Question.initMetrics();
        testQuestion = Question.all().first();
    }

    @Test
    public void testRatingPoints() {
        final int originalScore = QuestionScoreCalculator.calculateStandardScore(testQuestion);

        GET("/questions/rateQuestion?id=" + testQuestion.id + "&rating=1");
        
        testQuestion.refresh();

        //ensure that the standard score went up as it should
        assertEquals(originalScore + RATING_POINTS,testQuestion.standardScore);

        GET("/questions/rateQuestion?id=" + testQuestion.id + "&rating=-1");

        testQuestion.refresh();

        //ensure that the standard score went down as it should
        assertEquals(originalScore - RATING_POINTS, QuestionScoreCalculator.calculateStandardScore(testQuestion));
    }

    @Test
    @Ignore
    public void testInterviewPoints() {

        int originalScore = QuestionScoreCalculator.calculateStandardScore(testQuestion);

        //create a new interview
        Interview interview = new Interview(user);

        interview.name = "test interview";

        //add the test question to it
        interview.addQuestionToInterview(testQuestion);
        interview.save();

        //ensure that the standard score went up as it should
        assertTrue(QuestionScoreCalculator.calculateStandardScore(testQuestion) == (originalScore + INTERVIEW_POINTS));

        //take the test question out of the interview
        interview.removeQuestionFromInterview(testQuestion);
        interview.save();

        //ensure that the standard score went back down as it should
        assertTrue(QuestionScoreCalculator.calculateStandardScore(testQuestion) == originalScore);
    }

}
