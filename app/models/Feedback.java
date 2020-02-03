
package models;

import enums.FeedbackSummary;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Required;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
public class Feedback extends ModelBase {
    
    public boolean adminVisibleOnlyFlag = false;
    
    /**
     * <strong>Invariant:</strong> <code>feedbackSource != null ^ 
     * feedbackEmail != null</code>
     */
    @ManyToOne
    public User feedbackSource;
    public String feedbackEmail;

    public FeedbackSummary summaryChoice;
    
    @ManyToOne
    public ActiveInterview activeInterview;
    
    @Required
    public String comments;
    
    @Required
    @ManyToOne
    public Candidate candidate;

    /**
     * @deprecated use a constructor that does not rely on "magicId"
     */
    @Deprecated
    public Feedback(Candidate target, User source, 
                String regardingMagicID, FeedbackSummary summaryChoice, 
                String comments, boolean visibleOnlyToAdmin) {
        this(target,
                source,
                Interview.getCandidateInterviewAssociationFromMagicID(regardingMagicID),
                summaryChoice,
                comments,
                visibleOnlyToAdmin);
    }
    
    /**
     * @deprecated use a constructor that does not rely on "magicId"
     * <p>Creates feedback on the given non-<code>null</code> candidate,
     * originating from the given non-<code>null</code> email and visible either 
     * to the entire company or only company admins, as specified by 
     * <code>visibleOnlyToAdmin</code>.</p>
     * 
     * @param target The {@link Candidate Candidate} for which this feedback is
     *            being provided.
     * @param source The user email who provided the feedback.
     * @param visibleOnlyToAdmin <code>true</code> <strong>iff</strong> this
     *            feedback should be visible only to admins, otherwise it is
     *            visible to the entire company.
     *
     */
    @Deprecated
    public Feedback(Candidate target, String source, 
                String regardingMagicID, FeedbackSummary summaryChoice, 
                String comments, boolean visibleOnlyToAdmin) {
        adminVisibleOnlyFlag = visibleOnlyToAdmin;
        feedbackEmail = source;
        
        this.candidate = target;
        this.summaryChoice = summaryChoice;
        this.comments = comments;
        
        activeInterview = Interview.getCandidateInterviewAssociationFromMagicID(
                regardingMagicID);
    }

    public Feedback(Candidate target, User source,
                    ActiveInterview interview, FeedbackSummary summaryChoice,
                    String comments, boolean visibleOnlyToAdmin){
        feedbackSource = source;
        this.candidate = target;
        this.activeInterview = interview;
        this.summaryChoice = summaryChoice;
        this.comments = comments;
        adminVisibleOnlyFlag = visibleOnlyToAdmin;
    }

    /**
     * Indicates that the given user is the "owner" either by userId association or email address with this feedback
     * @param user User to check for ownership
     * @return whether the given user owns this feedback record
     */
    public boolean isOwner(User user){
        if(user == null){
            throw new IllegalArgumentException("Feedback.isOwner called with null user argument");
        }

        return user.equals(this.feedbackSource) || user.email.equals(this.feedbackEmail);
    }

}
