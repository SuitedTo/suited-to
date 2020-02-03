package logic.questions.scoring;

import java.util.List;

import javax.persistence.Query;

import controllers.Security;
import models.Company;
import models.InterviewQuestion;
import models.Question;
import models.QuestionMetadata;
import models.User;
import models.query.question.QuestionRatings;
import play.Play;
import play.db.jpa.JPA;


/**
 * This class is used to calculate various metrics based on
 * QuestionScore objects.
 * 
 * This class pulls straight from the db so in the typical case you call on this
 * class when you're updating some value as a result of some change.
 * 
 * @author joel
 *
 */
public class QuestionScoreCalculator {
	public static final int SS_POINTS_PER_RATING;
	public static final int SS_POINTS_PER_INTERVIEW;

	public static final int IS_USER_RATED_UP;
	public static final int IS_USER_RATED_DOWN;
	public static final int IS_COWORKER_RATING;
	public static final int IS_NON_COWORKER_RATING;
	public static final int IS_INTERVIEWS;
	public static final int IS_COWORKER_CREATED;

	static{
		SS_POINTS_PER_RATING = Integer.valueOf(Play.configuration.getProperty("score.standard.rating.weight"));
		SS_POINTS_PER_INTERVIEW = Integer.valueOf(Play.configuration.getProperty("score.standard.interviews.weight"));

		IS_USER_RATED_UP = Integer.valueOf(Play.configuration.getProperty("score.interview.userRatedUp.weight"));
		IS_USER_RATED_DOWN = Integer.valueOf(Play.configuration.getProperty("score.interview.userRatedDown.weight"));
		IS_COWORKER_RATING = Integer.valueOf(Play.configuration.getProperty("score.interview.coworkerRating.weight"));
		IS_NON_COWORKER_RATING = Integer.valueOf(Play.configuration.getProperty("score.interview.nonCoworkerRating.weight"));
		IS_INTERVIEWS = Integer.valueOf(Play.configuration.getProperty("score.interview.interviews.weight"));
		IS_COWORKER_CREATED = Integer.valueOf(Play.configuration.getProperty("score.interview.coworkerCreated.weight"));
	}

	/**
	 * Calculates the "standard score" for the given Question.
	 * 
	 * @param question The question
	 * @return The standard score.
	 */
    public static int calculateStandardScore(Question question) {
        if (question == null) {
            throw new IllegalArgumentException();
        }

        return calculateTotalInterviews(question) * SS_POINTS_PER_INTERVIEW +
        		calculateTotalRating(question) * SS_POINTS_PER_RATING;
    }
    
    public static int calculateTotalRating(Question question){
    	String sumQueryString = "SELECT SUM(qm.rating) FROM QuestionMetadata qm WHERE qm.question=:question";
    	Query sumQuery = JPA.em().createQuery(sumQueryString);
    	sumQuery.setParameter("question", question);
    	long total = 0L;
    	try{
    		total = (Long) sumQuery.getSingleResult();
    	} catch (NullPointerException npe){
    	}
    	return (int)total;
    }


    /**
	 * Calculates the "rating points" for the given Question. This is
	 * the user specific aspect of the interview score.
	 * 
	 * 
	 * @param question The question
	 * @return The question rating points.
	 */
	public static int calculateQuestionRatingPoints(Question question, User user){

		if(question == null){
			throw new IllegalArgumentException();
		}

		/**
		 *     	+50 for any question the user has rated up
		 *		-1000 for any question the user has rated down
    			+5 for any question a user in the same company has rated up
    			-5 for any question a user in the same company has rated down
    			+1 for any question a user in a different company has rated up
    			-1 for any question a user in a different company has rated down
    			+100 for any question created by someone in your company, whether public or private.
		 */
		User me = (user == null)?Security.connectedUser():user;
		int total = 0;
		if (me != null){
			int userRating = QuestionRatings.userRating(me, question);
			int multiplier = (userRating < 0)?IS_USER_RATED_DOWN:IS_USER_RATED_UP;
			total += userRating * multiplier;

			Company company = me.company;
			if (company != null){
				
				total += QuestionRatings.coworkerRating(me, question) * IS_COWORKER_RATING;
				
				total += QuestionRatings.nonCoworkerRating(me, question) * IS_NON_COWORKER_RATING;
				
				if((question.user != null) && (question.user.company != null) &&
						!question.user.equals(user) &&
						(question.user.company.equals(company))){
					total += IS_COWORKER_CREATED;
				}
			}

			return total;
		}

		return 0;
	}
	
	/**
	 * Calculates the "interview points" for the given Question. This is
	 * the part of the question's interview score that is not user specific.
	 * 
	 * 
	 * @param question The question
	 * @return The "interview points".
	 */
	public static int calculateQuestionInterviewPoints(Question question){

		/**
    			+2 for each interview the question is used in
        		-2 when a question is removed from an interview.
		 */
		return calculateTotalInterviews(question) * IS_INTERVIEWS;
	}
	
	public static int calculateTotalInterviews(Question question){
		return (int) InterviewQuestion.count("byQuestion", question);
	}


	/**
	 * Calculates the "interview score" for the given Question. This includes
	 * the user specific rating points as well as the question based interview
	 * points.
	 * 
	 * 
	 * @param question The question
	 * @return The "interview score".
	 */
	public static int calculateInterviewScore(Question question, User user){
		

		if(question == null){
			throw new IllegalArgumentException();
		}

		return calculateQuestionInterviewPoints(question) + calculateQuestionRatingPoints(question, user);
	}
	
	public static int calculateInterviewScore(Question question){
		return calculateInterviewScore(question, null);
	}
}
