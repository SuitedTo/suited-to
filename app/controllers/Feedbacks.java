/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
*/
package controllers;

import com.google.gson.*;
import controllers.deadbolt.Restrict;
import controllers.deadbolt.Restrictions;
import controllers.deadbolt.RoleHolderPresent;
import enums.FeedbackSummary;
import enums.RoleValue;
import jobs.FeedbackPdfJob;
import models.*;
import models.filter.feedback.ByCandidateId;
import models.query.feedback.AccessibleFeedbackHelper;
import notifiers.Mails;
import play.Logger;
import play.Play;
import play.data.validation.Required;
import play.data.validation.Valid;
import play.db.jpa.JPA;
import play.libs.F.Promise;
import play.mvc.Before;
import play.templates.JavaExtensions;
import utils.StringUtils;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import java.io.ByteArrayInputStream;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Feedbacks extends ControllerBase {

    private static final FeedbackRenderer FEEDBACK_SERIALIZER  =
            new FeedbackRenderer();

    private static final Gson DATATABLE_RENDERER;

    static {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Feedback.class, FEEDBACK_SERIALIZER);
        builder.serializeNulls();
        DATATABLE_RENDERER = builder.create();
    }

    private static final List<String> emailRegexes = new ArrayList<String>();
    //Set up the regular expressions for parsing feedback from email replies
    static {

        emailRegexes.add("Sent from my iPhone");//IPHONE SIGNATURE
        emailRegexes.add("Sent from my iPad");//IPAD SIGNATURE
        emailRegexes.add("On [A-Z][a-z]{2} \\d\\d?, \\d{4}, at \\d\\d?:\\d\\d [A|P]M,"); //Iphone
        emailRegexes.add("On [A-Z][a-z]{2}, [A-Z][a-z]{2} \\d\\d?, \\d{4} at \\d{1,2}:\\d{2} [A|P]M,"); //ipad and gmail
        emailRegexes.add("From: [^\\[]+\\[[^:]+:[^\\[]+]");//Outlook
        emailRegexes.add("--- On [A-Z][a-z]{2}, \\d\\d?/\\d\\d?/\\d\\d,"); //Yahoo! Mail Classic
        emailRegexes.add("________________________________"); //Yahoo! Mail
        emailRegexes.add("Date: [A-Z][a-z]{2}, \\d\\d? [A-Z][a-z]{2} \\d{4} \\d\\d?:\\d\\d:\\d\\d -?\\d\\d\\d\\d"); //Live/Hotmail
        emailRegexes.add("Sent from my BlackBerry");
        emailRegexes.add("Sent from my Droid");
        emailRegexes.add("-----Original Message-----"); //AOL
        emailRegexes.add("------Original Message------"); //Android default mail client
        emailRegexes.add("On [A-Z][a-z]{2} \\d\\d?, \\d{4} \\d\\d?:\\d\\d [A|P]M,"); //Android gmails
        emailRegexes.add("### Reply above this line ###"); //Default, included in SuitedTo's feedback email
    }


    /**
     * Display the form to create a new Feedback associated with the given Candidate and optional Live Interview.
     * Authorization is handled by authorizationCheck
     * @param candidateId Primary key of the candidate that feedback is being given for (Required)
     * @param liveInterviewId Primary key of the live interview that feedback is being given for (Optional)
     * @param nonce TemporaryFeedbackAuthorization key (Optional)
     */
    public static void create(@Required Long candidateId, Long liveInterviewId, Integer nonce){
        Candidate candidate = Candidate.findById(candidateId);
        ActiveInterview liveInterview = null;
        if (liveInterviewId != null) {
            liveInterview = ActiveInterview.findById(liveInterviewId);
        }
        boolean fromLiveInterview = Boolean.valueOf(flash.get("fromLiveInterview"));
        render(candidate, liveInterview, fromLiveInterview, nonce);
    }

    /**
     * Saves feedback from the front end form. summaryChoice is explicitly bound because it is required in this context
     * but not always required on the model so we need to specify the @Required restriction here in the controller.
     * Authorization is handled by authorizationCheck utility method
     * @param feedback new Feedback record to bind request data to
     * @param summaryChoice enum value for selected FeedbackSummary
     * @param fromLiveInterview indicates that the page is being visited directly from the live interview flow, different
     *                          messaging will be used in the view.
     */
    public static void save(@Valid Feedback feedback, @Required FeedbackSummary summaryChoice,
                            boolean fromLiveInterview){

        feedback.summaryChoice = summaryChoice;

        if(validation.hasErrors()){
            //assign to explicit variables to that template has the expected variables to reference
            Candidate candidate = feedback.candidate;
            ActiveInterview liveInterview = feedback.activeInterview;
            render("@create", candidate, liveInterview, feedback, fromLiveInterview);
        }

        /*use the addFeedback method which will adjust the ActiveInterviewWorkflow status appropriately and save the
        feedback*/
        final User connectedUser = Security.connectedUser();
        if(connectedUser != null){
            feedback.candidate.addFeedback(connectedUser, feedback);
            //redirect back to candidates page after a successful save
            Candidates.show(feedback.candidate.id, null, null, null, true, null);
        } else {
            TemporaryFeedbackAuthorization authorization = (TemporaryFeedbackAuthorization)routeArgs.get("temporaryFeedbackAuthorization");
            feedback.candidate.addFeedback(authorization, feedback);
            boolean feedbackAdded = true;
            render("@temporaryAuthConfirmation", feedbackAdded);
        }
    }

    /**
     * Deletes the given feedback. Authorization is handled by deleteAuthorizationCheck utility method
     * @param feedbackId Primary key of the Feedback record to be deleted
     */
    public static void delete(@Required Long feedbackId) {
        Feedback feedback = Feedback.findById(feedbackId);
        feedback.delete();
        renderJSON(buildSuccessResponseMap());
    }


    /**
     * Custom authorization logic for creating feedback
     */
    @Before(only = {"create", "save"})
    private static void authorizationCheck(){

        /*check for temporary feedback authorization, this is used when "request feedback" email is sent to an email
        address that is not a member of the application*/
        String temporaryFeedbackNonce = request.params.get("nonce");
        if (!StringUtils.isEmpty(temporaryFeedbackNonce)) {
            TemporaryFeedbackAuthorization authorization = TemporaryFeedbackAuthorization.find("byNonce", Integer.valueOf(temporaryFeedbackNonce)).first();
            if (authorization != null) {
                routeArgs.put("temporaryFeedbackAuthorization", authorization);
                return;
            }
        }

        /*If the user is not authenticated redirect to the login flow before continuing with this action*/
        loginIfNecessary();

        User connectedUser = Security.connectedUser();

        Candidate candidate = null;
        String idString = request.params.get("candidateId");
        if(idString == null){
            idString = request.params.get("feedback.candidate.id");
        }
        if(idString != null){
            Long id = null;
            try {
                id = Long.valueOf(idString);
            } catch (NumberFormatException e) {
                Logger.error("invalid numerical value for candiateId. Value was: " + idString, e);
            }
            candidate = id != null ? Candidate.<Candidate>findById(id) : null;
        }
        notFoundIfNull(candidate);

        //check to make sure the user has access to the requested candidate
        if(!candidate.hasAccess(connectedUser)){
            forbidden();
        }

        //check to make sure the user has access to create feedback for the requested candidate
        if(candidate.feedbackHidden && !connectedUser.hasOneOf(RoleValue.APP_ADMIN, RoleValue.COMPANY_ADMIN)){
            forbidden();
        }
    }

    @Before(only = {"delete"})
    private static void deleteAutorizationCheck(){
        User connectedUser = Security.connectedUser();
        Feedback feedback = null;
        String idString = request.params.get("feedbackId");
        if(idString != null){
            Long id = null;
            try {
                id = Long.valueOf(idString);
            } catch (NumberFormatException e) {
                Logger.error("invalid numerical value for feedbackId. Value was: " + idString, e);
            }
            feedback = Feedback.findById(id);
        }
        notFoundIfNull(feedback);

        boolean isAdmin = connectedUser.hasOneOf(RoleValue.APP_ADMIN, RoleValue.COMPANY_ADMIN);
        if(!isAdmin && !feedback.isOwner(connectedUser)){
            unauthorized();
        }

        if(!isAdmin && feedback.candidate.feedbackHidden){
            unauthorized();
        }
    }





    public static void feedbackTableData(@Required Long candidateID) {

        Candidate candidate = Candidate.findById(candidateID);
        boolean noFBdisplay = false;

        Map<String, Object> result = new HashMap<String, Object>();

        AccessibleFeedbackHelper afh = new AccessibleFeedbackHelper(Security.connectedUser());
        ByCandidateId filter = new ByCandidateId();
        filter.include(candidateID.toString());
        afh.addFilter(filter);
        CriteriaQuery cq = afh.finish();
        TypedQuery<Feedback> query = JPA.em().createQuery(cq);
        
        List<Feedback> feedback = query.getResultList();
        result.put("aaData", feedback);

        renderJSON(DATATABLE_RENDERER.toJson(result));
    }
    
    @Restrictions({@Restrict({RoleValue.APP_ADMIN_STRING}), @Restrict({RoleValue.COMPANY_ADMIN_STRING})})
    public static void getFeedbackPdf(@Required Long candidateID, Boolean inline, Boolean convert){

        Candidate candidate = Candidate.findById(candidateID);
        List<Feedback> feedbacks = candidate.feedbackList;

        /*access the candidate to avoid the dreaded org.hibernate.LazyInitializationException trying to load
        Candidate.feedbackList inside the report generation*/
        for (Feedback feedback : feedbacks) {
            Object temp = feedback.candidate;
            if (feedback.activeInterview != null) {
                for (InterviewQuestion interviewQuestion : feedback.activeInterview.interviewQuestions) {
                    Object temp2 = interviewQuestion;
                }
            }
        }
        Promise<byte[]> pdfPromise = new FeedbackPdfJob(feedbacks).now();
        byte[] pdfBytes = await(pdfPromise);

        StringBuilder nameBuilder = new StringBuilder("suitedto-feedback-report");
        nameBuilder.append("-");
        nameBuilder.append(candidate.name.replaceAll("\\s+", "_"));

        nameBuilder.append("-");
        DateFormat dateFormat = new SimpleDateFormat("MMddyy");
        nameBuilder.append(dateFormat.format(new Date()));
        nameBuilder.append(".pdf");

        if (inline == null) {
            inline = true;
        }
        if (convert == null) {
            convert = false;
        }
        if (inline == false) {
            convert = false;
        }

        renderBinary(new ByteArrayInputStream(pdfBytes), nameBuilder.toString(), "application/pdf", inline, convert);

    }

    private static class FeedbackRenderer implements JsonSerializer<Feedback> {

        public JsonElement serialize(Feedback t, Type type,
                                     JsonSerializationContext jsc) {

            JsonObject result = new JsonObject();
            String feedback = "";
            DecimalFormat decimalFormat = new DecimalFormat("#.##");

            if (t.feedbackSource == null) {
                feedback += t.feedbackEmail;
            }
            else {
                feedback += t.feedbackSource.fullName + " (" +
                        t.feedbackSource.email + ")";
            }
            feedback += "<br/><br/>";

            feedback += "Interview: ";
            if (t.activeInterview != null) {
                feedback += t.activeInterview.name;
            }
            else {
                feedback += "<em>-None-</em>";
            }
            feedback += "<br/>";

            feedback += "Average Rating: ";
            if (t.activeInterview != null && t.activeInterview.averageQuestionRating != null) {
                feedback += Double.valueOf(decimalFormat.format(t.activeInterview.averageQuestionRating));
                feedback += " (on a scale of -2 to 2)";
            } else {
                feedback += "<em>-None-</em>";
            }
            feedback += "<br/>";

            feedback += "Recommendation: ";
            if(t.summaryChoice != null) {
                feedback += t.summaryChoice.toString();
            }
            else {
                feedback += "<em>-Not Selected-</em>";
            }

            feedback += "<br/><br/>";

            feedback += JavaExtensions.nl2br(t.comments);

            String email;
            if (t.feedbackSource == null) {
                email = t.feedbackEmail;
            }
            else {
                email = t.feedbackSource.email;
            }

            result.addProperty("timestamp", t.created.getTime());
            result.addProperty("source", email);
            result.addProperty("feedbackId", t.id);
            result.addProperty("since", JavaExtensions.since(t.created));
            result.addProperty("feedback", feedback);

            return result;
        }

    }

    public static void generateTestEmailReply(Long candidateId, String from, Long activeInterviewId){
    	if(play.Play.mode.equals(Play.Mode.DEV)){
    		
    		TemporaryFeedbackAuthorization fba =
    				new TemporaryFeedbackAuthorization(
    						(Candidate)Candidate.findById(candidateId),
    						from,
    						(ActiveInterview)ActiveInterview.findById(activeInterviewId));
    		int to = fba.nonce;
    		render(to, from, activeInterviewId);
    	}
    }

    public static void fromEmailReply() {

        int nonce;
        String reply = "";
        String parsedEmailAddress = "";

        Pattern pattern;
        Matcher matcher;

        int replyEnd = 0;

        String to = params.get("to");

        String from = params.get("from");

        Pattern removeEmailRegex = Pattern.compile("<[^<]+>");
        Matcher removeEmailMatcher = removeEmailRegex.matcher(from);

        if(removeEmailMatcher.find()) {
            parsedEmailAddress = removeEmailMatcher.group();
            //Remove the leading "<" and trailing ">"
            parsedEmailAddress = parsedEmailAddress.subSequence(1, parsedEmailAddress.length()-1).toString();
        }
        else {
            Logger.error("Could not parse a valid email address");
            return;
        }

        User userReplying = User.find("byEmail", parsedEmailAddress.toLowerCase()).first();

        Pattern noncePattern = Pattern.compile("-?\\d+");
        Matcher nonceMatcher = noncePattern.matcher(to);

        if(nonceMatcher.find()){
            String nonceStr = nonceMatcher.group();
            nonce = Integer.parseInt(nonceStr);

        }
        else {
            Logger.error("No proper nonce could be parsed from the email address" + to);
            return;
        }

        String emailBody=params.get("text");
        for(String regex : emailRegexes)
        {
            pattern = Pattern.compile(regex);
            matcher = pattern.matcher(emailBody);

            if(matcher.find()){
                replyEnd = matcher.start();
                break;
            }
        }

        reply = emailBody.substring(0, replyEnd);

        TemporaryFeedbackAuthorization authorization =
                TemporaryFeedbackAuthorization.find("byNonce", nonce).first();

        if(authorization != null) {

            String magicID;

            if(authorization.activeInterview != null) {
                magicID = authorization.activeInterview.getMagicID();
            }
            else {
                //General Feedback
                magicID = null;
            }
            if(userReplying != null) {
                authorization.candidate.addFeedback(userReplying, magicID, null,
                        reply);

                if(userReplying.feedbackReplyEmails) {
                    Mails.feedbackReceived(userReplying.email, authorization.candidate, true, userReplying);
                }
            }
            else {
                authorization.candidate.addFeedback(parsedEmailAddress, magicID, null, reply);

                Mails.feedbackReceived(parsedEmailAddress, authorization.candidate, false, null);
            }

            //Clean up the temporary authorization
            authorization.delete();
        }
        else {
            Logger.error("No temp authorization found in db for nonce " + nonce);
            return;
        }
    }

}

