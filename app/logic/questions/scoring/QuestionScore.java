package logic.questions.scoring;

import models.Question;
import models.User;

/**
 * This class provides access to all of the attributes that are considered when
 * scoring a question for the interview builder.
 * 
 * @author joel
 *
 */
public class QuestionScore{
	final User user;
	final Question question;
	
	public QuestionScore(long questionId, User user){
		this(Question.getQuestion(questionId),user);
	}
	
	public QuestionScore(Question question, User user){
		this.question = question;
		this.user = user;
	}
	
	public long getQuestionId() {
		return question.id;
	}
	
	public Question getQuestion() {
		return question;
	}
	
	/**
	 * @return The "interview score" associated with this question.
	 */
	public int getInterviewScore(){
		
		return QuestionScoreCalculator.calculateInterviewScore(question, user);
	}

	/**
	 * @return The total rating for this question.
	 */
	public int getTotalRating(){
		
		return question.totalRating;
	}
	
	/**
	 * @return The total number of interviews that contain this question.
	 */
	public int getTotalInterviews(){
		
		return question.totalInterviews;
	}
	
	/**
	 * @return The total number of interviews that contain this question and
	 * are associated with the connected user's company.
	 */
	public int getCurrentCompanyInterviews(){
		return question.getInterviews(user.company);
	}
	
	/**
	 * @return The total rating for this question by other users that belong to
	 * the connected user's company.
	 */
	public int getCurrentCompanyRating(){
		return question.getRating(user.company);
	}
	
	/**
	 * @return The rating for this question by the connected user.
	 */
	public int getCurrentUserRating(){
		return question.getRating(user);
	}

}
