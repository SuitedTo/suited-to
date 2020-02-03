/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package models;

import enums.ActiveInterviewState;
import enums.RoleValue;

import javax.persistence.*;

import play.mvc.Util;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * <p>An active interview represents an interview that can be manipulated in
 * real time, as when delivering the interview to a candidate. </p>
 */
@Entity
public class ActiveInterview extends Interview {

    @ManyToOne
    public Candidate interviewee;
    
    public boolean feedbackReminderSent;

    /**
     * The date when this interview is scheduled to take place.  May be null
     */
    public Date anticipatedDate;

    @Column(columnDefinition = "LONGTEXT")
    public String notes;

    /**
     * The User who is actually conducting this interview.  May or may not be the same as the creator
     */
    @ManyToOne
    @JoinColumn(name = "INTERVIEWER")
    public User interviewer;

    @OneToMany(mappedBy="activeInterview", cascade = CascadeType.ALL, fetch=FetchType.LAZY)
    public List<ActiveInterviewEvent> events = new LinkedList<ActiveInterviewEvent>();

    @OneToMany(mappedBy = "activeInterview", cascade = CascadeType.REMOVE)
    public List<Feedback> feedback = new ArrayList<Feedback>();

    public Double averageQuestionRating;

    /**
     * Default Constructor required for test data creation
     */
    public ActiveInterview() {}
    
    /**
     * <p>Note that constructing an active interview will save it (as we must 
     * start persisting events about it anyway).</p>
     */
    public ActiveInterview(User creatingUser, Candidate interviewee) {
        super(creatingUser);
        this.interviewee = interviewee;
    }

    public static String getNextName(Candidate candidate, Company company){
    	String name = candidate.name + " Interview";
        
        
        String query = "name = ? and active = ? and company ";
        Object[] queryParams;

        //todo: this seems pretty expensive just to stick a (x) at the end of the interview name, maybe we should rethink that requirement
        //x = null doesn't work?  Is this an SQL thing?  Because it's super 
        //annoying
        if (company == null) {
            query += "is null";
            queryParams = new Object[]{name, true};
        }
        else {
            query += "= ?";
            queryParams = new Object[]{name, true, company};
        }
        
        int count = 1;
        while (Interview.count(query, queryParams) != 0) {
            
            count++;
            name = candidate.name + " Interview (" + count + ")";
            queryParams[0] = name;
        }
        return name;
    }

    /**
     * Gets the most recent status change, Ordering is performed by creationTimestamp instead of created because of the possibility
     * that 2 state changes are created as part of a single transaction. Because the database only stores the timestamp
     * down to the second it's very likely that two state changes will have the same timestamp even though they are
     * created sequentially.  The creationTimestamp is the actual time in milliseconds since the epoch stored as a number
     * so it has precision down to the millisecond and should be used instead. the inclusion of id in the ordering is for
     * legacy data when id's were guaranteed to be sequential and creationTimestamp had not yet been introduced.
     *
     * @return the most recent ActiveInterviewStateChange
     */
    public ActiveInterviewStateChange getMostRecentStatusChange() {
        return ActiveInterviewStateChange.find("activeInterview = ? order by creationTimestamp desc, id desc", this).first();
    }
    
    public ActiveInterviewState getStatus() {
        return getMostRecentStatusChange().toState;
    }
    
    /**
     * <p>Note that this triggers a save() (as it might create a persisted 
     * notification anyway), and thus must be done after any validation has 
     * already been performed.</p>
     * 
     * <p>Returns the latest status change.</p>
     */
    public ActiveInterviewStateChange changeStatus(User u, ActiveInterviewState s) {
        
        ActiveInterviewStateChange lastStatusChange = 
                getMostRecentStatusChange();
        
        if (lastStatusChange == null || !s.equals(lastStatusChange.toState)) {

            ActiveInterviewStateChange e = 
                    new ActiveInterviewStateChange(this, user, s);
            e.save();
            
            if (s.equals(ActiveInterviewState.STARTED) || 
                    s.equals(ActiveInterviewState.FINISHED)) {
                
                //Might need to bug the user with an email here in a bit, either
                //to stop the interview or provide feedback
                feedbackReminderSent = false;
                
                //If we're starting the interview, we add a notification to the
                //user's dashboard so they'll know to come stop it.  If we're
                //finishing an interview, we add a notification so they'll know
                //to provide feedback
                if (this.feedback.isEmpty()) {
                    e.createNotification();
                }
            }
             
            //If there's a notification associated with the last status
            //change, seek and destroy it
            if (lastStatusChange != null) {
                lastStatusChange.removeNotifications();
            }

            events.add(e);
            lastStatusChange = e;
        }
        
        return lastStatusChange;
    }
    
    public boolean hasFeedbackSince(Date time) {
        return hasFeedbackSince(time, user);
    }
    
    public boolean hasFeedback(){
    	return Feedback.count("activeInterview = ?", this) > 0;
    }
    
    public boolean hasFeedbackSince(Date time, User provider) {
        return (Feedback.count(
                "activeInterview = ? and feedbackSource = ? and created >= ?",
                this, provider, time) 
            > 0);
    }

    @Deprecated
    public String getMagicID() {
        return "a" + id;
    }

    /**
     * The number of interviewQuestions for this interview that have a rating value
     * @return
     */
    public int getRatedQuestionCount(){
        if(interviewQuestions == null){
            return 0;
        }

        int count = 0;
        for (InterviewQuestion interviewQuestion : interviewQuestions) {
            if(interviewQuestion.rating != null){
                count++;
            }
        }

        return count;
    }

    /**
     * Determines if the given user has provided feedback for this ActiveInterview
     * @param user User to check for feedback
     * @return whether the user has provided feedback
     */
    public boolean hasUserProvidedFeedback(User user){
        Object result = Feedback.findFirst("byActiveInterviewAndFeedbackSource", this, user);
        return result != null;
    }

    /**
     * Ultimately, multiple users may be assigned as interviewer, so we 
     * encapsulate "is an interviewer on this interview" logic.
     */
    public boolean isInterviewer(User u) {
        return interviewer != null && interviewer.equals(u);
    }
    

    public boolean canEdit(User user){
        if(user == null || !hasAccess(user)){
            return false;
        }
        //at this point we know that the user should be from the same company or is an application Admin

        //app admins and company admins should be able to edit
        if(user.hasRole(RoleValue.APP_ADMIN) || user.hasRole(RoleValue.COMPANY_ADMIN)){
            return true;
        }

        //interview creator and interviewer should also be able to edit
        if(user.equals(interviewer) || user.equals(this.user)){
            return true;
        }

        return false;

    }

    @Override
    public void inactivate() {
        List<ActiveInterviewStateChange> statusChanges =
                ActiveInterviewStateChange.find("activeInterview = ? order by id desc", this).fetch();

        for (ActiveInterviewStateChange activeInterviewStateChange : statusChanges) {
            activeInterviewStateChange.removeNotifications();
        }

        super.inactivate();
    }
}
