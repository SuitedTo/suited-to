package notifiers.prep;

import models.*;

import models.prep.PrepInterviewReview;
import models.prep.PrepUser;
import play.Play;
import play.mvc.Http;
import play.mvc.Mailer;
import play.mvc.Router;

public class Mails extends Mailer{

    static final String FROM = "Prepado <notifications@prepado.com>";
    static final String REPLY_TO = "Prepado <notifications@prepado.com>";

    public static void reviewRequest(PrepInterviewReview review, String message){

        PrepUser user = review.prepInterview.owner;
        String userIdentifier = user.firstName != null ? user.firstName : user.email;
        Long interviewId = review.prepInterview.id;
        String reviewLink = Http.Request.current().getBase() + "/interviewer/" + interviewId + "/review?reviewerKey=" + review.reviewKey;

        setSubject("Prepado Review Request");
        addRecipient(review.reviewerEmail);
        setFrom(FROM);
        setReplyTo(REPLY_TO);
        send(review, userIdentifier, message, reviewLink);

    }
    
    public static void feedbackReceived(PrepInterviewReview review){

        PrepUser user = review.prepInterview.owner;
        String userName = user.firstName != null ? user.firstName : user.email;
        String reviewerName = review.reviewerEmail.substring(0, review.reviewerEmail.indexOf('@'));
        Long interviewId = review.prepInterview.id;
        String interviewLink = Http.Request.current().getBase() + "/interviewer/" + interviewId + "/review";

        setSubject("You have received feedback on your practice interview in Prepado!");
        addRecipient(user.email);
        setFrom(FROM);
        setReplyTo(REPLY_TO);
        send(userName, reviewerName, interviewLink);

    }

    public static void mobileTrialComplete(PrepUser user){
        String name = user.firstName == null? "Friend" : user.firstName;
        String link = Http.Request.current().getBase() + "/subscribe";


        setSubject("We hope you enjoyed your first practice interview");
        addRecipient(user.email);
        setFrom(FROM);
        setReplyTo(REPLY_TO);

        send(name, link);
    }

    public static void webAppTrialComplete(PrepUser user){
        String name = user.firstName == null? "Friend" : user.firstName;;
        String link = Http.Request.current().getBase() + "/subscribe";


        setSubject("We hope you enjoyed your first practice interview");
        addRecipient(user.email);
        setFrom(FROM);
        setReplyTo(REPLY_TO);

        send(name, link);
    }
}
