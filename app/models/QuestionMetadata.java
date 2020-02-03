package models;

import logic.questions.scoring.QuestionScoreCalculator;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * User-specific information for a given question
 */
@Entity
@Table(name = "question_metadata")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class QuestionMetadata extends ModelBase {


    @ManyToOne
    public Question question;

    @ManyToOne
    public User user;

    /**
     * Numerical rating of the question. A "thumbs up" is a 1 and a "thumbs down" is -1
     */
    public Integer rating;
    
    public Integer ratingPoints;
    
    /**
     * Represents the last time that this question was rated
     * or used in an interview.
     */
    @Temporal(TemporalType.TIMESTAMP)
	public Date lastActivity;


    public QuestionMetadata(Question question, User user) {
        this.question = question;
        this.user = user;
        this.ratingPoints = 0;
        this.rating = 0;
    }

    /**
     * Default Constructor required for test data creation
     */
    public QuestionMetadata() {}

    public void updateRatingPoints() {
    	ratingPoints = QuestionScoreCalculator.calculateQuestionRatingPoints(question, user);
        save();
    }
    
    public static void updateRatingPoints(Question question) {
    	List<QuestionMetadata> metadata = QuestionMetadata.find("byQuestion", question).fetch();
        for (QuestionMetadata questionMetadata : metadata) {
            if (questionMetadata.rating != null) {
                questionMetadata.updateRatingPoints();
            }
        }
    }
}
