package models;

import enums.QuestionStatus;
import models.annotations.PropertyChangeListeners;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import trigger.assessor.UserClassificationListener;

import javax.persistence.Entity;
import javax.persistence.OneToOne;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

@Entity
@PropertyChangeListeners(UserClassificationListener.class)
public class UserMetrics extends ModelBase{
	
	@OneToOne
    @Fetch(FetchMode.SELECT)
	public User user;
	
	public int numberOfSubmittedQuestions;
	
	public int numberOfAcceptedQuestions;
	
	public int totalQuestionScore;
	
	public boolean reviewer;
	
	public boolean hasDisplayName;
	
	public boolean hasProfilePicture;
	
	public int numberOfLogins;
	
	public int numberOfQuestionsReviewed;
	
	public int numberOfUsersInvited;
	
	public int numberOfQuestionsRated;
	
	
	public UserMetrics(User user){
		this.user = user;
	}
	
	public void updateNumberOfSubmittedQuestions(){
		numberOfSubmittedQuestions = (int) Question.count("byUser", user);
	}
	
	public void updateNumberOfAcceptedQuestions(){
		numberOfAcceptedQuestions = (int) Question.count("byUserAndStatusAndActive", user, QuestionStatus.ACCEPTED,true);
	}
	
	public void incrementNumberOfAcceptedQuestions(){
		numberOfAcceptedQuestions = numberOfAcceptedQuestions + 1;
	}
	
	public void incrementNumberOfQuestionsReviewed(){
		numberOfQuestionsReviewed = numberOfQuestionsReviewed + 1;
	}
	
	public void incrementNumberOfUsersInvited(){
		numberOfUsersInvited = numberOfUsersInvited + 1;
	}
	
	public void updateNumberOfQuestionsRated(){
		numberOfQuestionsRated = (int) QuestionMetadata.count("from QuestionMetadata qm where qm.user = (?1) " +
                "AND qm.rating <> 0)", user);
	}
	
	public void incrementNumberOfQuestionsRated(){
		numberOfQuestionsRated = numberOfQuestionsRated + 1;
	}
	
	public void incrementNumberOfLogins(){
		numberOfLogins = numberOfLogins + 1;
	}
	
	public void updateReviewer(){
		reviewer = user.hasReviewCapability();
	}
	
	public void updateHasDisplayName(){
		hasDisplayName = !StringUtils.isEmpty(user.displayName);
	}
	
	public void updateHasProfilePicture(){
		hasProfilePicture = user.picture != null;
	}
	
	public void updateTotalQuestionScore(){
        //todo: do we want quick quesitons, private questions, flagged as inappropriate, or other scenarios to factor in here?
		totalQuestionScore = 0;
		List<Question> questions = Question.find("byUser", user).fetch();
		for(Question question : questions){
			totalQuestionScore += question.standardScore;
		}
	}
	
	/**
	 * Update all properties
	 */
	public void update(){
		updateNumberOfSubmittedQuestions();
		updateNumberOfAcceptedQuestions();
		updateNumberOfQuestionsRated();
		updateTotalQuestionScore();
		updateReviewer();
		updateHasDisplayName();
		updateHasProfilePicture();
		save();
	}

	public static class Synchronizer implements PropertyChangeListener{

		@Override
		public void propertyChange(PropertyChangeEvent evt) {

			if(evt.getSource() instanceof User){
				User user = (User)evt.getSource();
				if(evt.getPropertyName().equals("reviewCategories") ||
						evt.getPropertyName().equals("superReviewer")){
					user.metrics.updateReviewer();
				} else if(evt.getPropertyName().equals("lastLogin")){
					user.metrics.incrementNumberOfLogins();
				} else if(evt.getPropertyName().equals("displayName")){
					user.metrics.updateHasDisplayName();
				} else if(evt.getPropertyName().equals("picture")){
					user.metrics.updateHasProfilePicture();
				}
				return;
			}
		}
	}
}
