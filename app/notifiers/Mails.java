
package notifiers;

import java.net.MalformedURLException;
import java.util.*;

import controllers.Feedbacks;
import controllers.Security;

import models.*;

import org.apache.commons.mail.EmailAttachment;
import play.Logger;
import play.Play;
import play.mvc.Mailer;
import play.mvc.Router;

public class Mails extends Mailer {

    static final String FROM = "SuitedTo <notifications@suitedto.com>";
    static final String REPLY_TO = "SuitedTo <notifications@suitedto.com>";
    //static final String FEEDBACK = "SuitedTo <test@in.suitedto.com>";

    public static void temporaryPassword(User user, String temporaryPassword){
        setSubject("SuitedTo Information");
        addRecipient(user.email);
        setFrom(FROM);
        setReplyTo(REPLY_TO);
        send(user, temporaryPassword);
    }

    public static void flaggedAsInappropriate(User user, Question question){
        setSubject("Your Question was Flagged!");
        addRecipient(user.email);
        setFrom(FROM);
        setReplyTo(REPLY_TO);
        Long streetCred = user.streetCred;
        String name = (user.displayName != null)?user.displayName: user.fullName.split(" ")[0];
        String questionTitle = question.text;
        Long questionId = question.id;
        send(name, questionTitle, streetCred, questionId);
    }

    public static void feedbackReceived(String email, Candidate candidate, Boolean candidateLink, User user) {
        setSubject("We received your feedback");
        addRecipient(email);
        setFrom(FROM);
        setReplyTo(REPLY_TO);
        send(candidate, candidateLink, user, email);
    }

    public static void reviewer(User user, Category category) {
        setSubject("Congratulations! You are now a reviewer for " + category.name);
        addRecipient(user.email);
        setFrom(FROM);
        setReplyTo(REPLY_TO);
        send(user, category);
    }

    public static void reviewerMulti(User user, List<Category> categories) {
        setSubject("Congratulations! You are now a reviewer for new categories");
        addRecipient(user.email);
        setFrom(FROM);
        setReplyTo(REPLY_TO);
        send(user, categories);
    }

    public static void revokeReviewer(User user) {
        setSubject("Update from SuitedTo");
        addRecipient(user.email);
        setFrom(FROM);
        setReplyTo(REPLY_TO);
        send(user);
    }

    public static void nearingReviewer(User user, Long questionThreshold, Long streetCredThreshold) {
        setSubject("Almost there!");
        addRecipient(user.email);
        setFrom(FROM);
        setReplyTo(REPLY_TO);
        send(user, questionThreshold, streetCredThreshold);
    }

    public static void getFeedback(String email, Candidate candidate,
                                   String regardingMagicID, String subject, String requestReason) {

        ActiveInterview regarding =
                Interview.getCandidateInterviewAssociationFromMagicID(
                        regardingMagicID);

        User interviewer = User.find("byEmailIlike", email).first();

        TemporaryFeedbackAuthorization authorization =
                new TemporaryFeedbackAuthorization(candidate, email, regarding);

        String FEEDBACK = "SuitedTo <A" + authorization.nonce + "@" + Play.configuration.getProperty("mail.replyToAddressDomain") + ">";

        setSubject(subject);
        addRecipient(email);
        setFrom(FEEDBACK);
        setReplyTo(FEEDBACK);

        send(interviewer, email, candidate, regardingMagicID, authorization,
                requestReason, regarding);
    }

    public static void invitation(User invitingUser, User user, String host, String customMessage){
        setSubject("Invitation to join SuitedTo");
        addRecipient(user.email);
        setFrom(FROM);
        setReplyTo(REPLY_TO);
        String invitationUrl = buildInvitationUrl(user, host);
        send(invitingUser, user, invitationUrl, customMessage);
    }

    public static void emailFeedback(String email, Candidate candidate, int numWaiting, int numSubmitted,
                                     int numFutureInterviews, User sendingUser, User destinationUser) {

        setSubject("Feedback for Candidate " + candidate.name);
        addRecipient(email);
        setFrom(FROM);
        setReplyTo(REPLY_TO);
        /** Only counts feedback that has been requested via email.
         *
         *  Nnumwaiting and numsubmitted only include feedback that is requested of interviewer by SuitedTo
         *  when a live interview is finished.
         *  TODO: Include the feedback from the interviewer in these numbers
         */
        send(candidate, numWaiting, numSubmitted, numFutureInterviews, sendingUser, destinationUser, email);
    }

    public static void adminWelcome(User user){
        setSubject("Welcome to SuitedTo");
        addRecipient(user.email);
        setFrom(FROM);
        setReplyTo(REPLY_TO);
        
        String onlineVersionURL = Router.getFullUrl("Mails.adminWelcome");
        
        send(onlineVersionURL);
    }

    public static void individualWelcome(User user){
        setSubject("Thanks for choosing SuitedTo!");
        addRecipient(user.email);
        setFrom(FROM);
        setReplyTo(REPLY_TO);
        
        String onlineVersionURL = Router.getFullUrl("Mails.individualWelcome");
        
        send(onlineVersionURL);
    }

    public static void userWelcome(User user){
        setSubject("Welcome to SuitedTo");
        addRecipient(user.email);
        setFrom(FROM);
        setReplyTo(REPLY_TO);
        
        String onlineVersionURL = Router.getFullUrl("Mails.userWelcome");
        
        send(onlineVersionURL);
    }

    public static void questionAccepted(Question question) {

        User user = question.user;

        setSubject("SuitedTo: Your Question Was Accepted to the Public Pool!");
        addRecipient(user.email);
        setFrom(FROM);
        setReplyTo(REPLY_TO);

        Long streetCred = user.streetCred;
        String name = (user.displayName != null)?user.displayName: user.fullName.split(" ")[0];
        send(name, streetCred, question);
    }

    public static void questionRejected(Question question, String rejectComment) {

        User user = question.user;

        setSubject("SuitedTo: Your Question has been reviewed!");
        addRecipient(user.email);
        setFrom(FROM);
        setReplyTo(REPLY_TO);

        String name = (user.displayName != null)?user.displayName: user.fullName.split(" ")[0];
        send(name, question, rejectComment);
    }

    public static void interviewReminder(User interviewer, List<ActiveInterview> interviews){
        setSubject("SuitedTo: Today's interviews");
        addRecipient(interviewer.email);
        setFrom(FROM);
        setReplyTo(REPLY_TO);

        Comparator c = new Comparator<ActiveInterview>(){

            @Override
            public int compare(ActiveInterview ci1, ActiveInterview ci2) {
                return ci1.anticipatedDate.compareTo(ci2.anticipatedDate);
            }

        };
        Collections.sort(interviews, c);
        String name = (interviewer.displayName != null)?interviewer.displayName: interviewer.fullName.split(" ")[0];

        Boolean plural = interviews.size() > 1;

        send(name, interviews, plural);

    }
    
    public static void endOfTrialReminder(User user, String remaining){
    	setSubject("SuitedTo: Trial Expiration");
    	addRecipient(user.email);
    	setFrom(FROM);
    	setReplyTo(REPLY_TO);
    	
    	
    	String name = (user.displayName != null)?user.displayName: user.fullName.split(" ")[0];
    	send(name, remaining);
    	
    }

    public static void categoryChangeNotification(Category category, Category.CategoryStatus oldStatus, Category.CategoryStatus newStatus){
        setSubject("Congratulations! Your Category's Status Was Changed!");
        addRecipient(category.creator.email);
        setFrom(FROM);
        setReplyTo(REPLY_TO);
        send(category, oldStatus, newStatus);
    }
    
    public static void criticalStatus(String status){
    	setSubject("Critical Status");     
    	
    	String[] recipients = play.Play.configuration.getProperty("statusRecipients").split(",");
    	for(String recipient : recipients){
    		addRecipient(recipient);
    	}
    	setFrom(FROM);
    	setReplyTo(REPLY_TO);
    	send(status);    	
    }

    private static final String buildInvitationUrl(User user, String host){
        StringBuilder invitationUrl = new StringBuilder();
        invitationUrl.append("http://");
        invitationUrl.append(host);
        invitationUrl.append("/invitations/");
        invitationUrl.append(user.invitationKey);

        return invitationUrl.toString();
    }
}
