package models;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

import models.embeddable.Email;
import models.embeddable.ExternalLink;
import models.embeddable.PhoneNumber;
import models.filter.activeinterview.ByActive;
import models.filter.activeinterview.ByInterviewee;
import models.query.activeinterview.AccessibleActiveInterviewsHelper;

import org.apache.commons.lang.ObjectUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;

import play.db.jpa.JPA;
import utils.CriteriaHelper.TableKey;
import enums.ActiveInterviewState;
import enums.FeedbackSummary;
import enums.RoleValue;

@Entity
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Candidate extends ModelBase implements Restrictable {

	@ManyToOne(optional = true)
	public User creator;
	
    public String name;

    public String address;

    public Long taleoCandId;

    @ElementCollection
    @Sort(type = SortType.NATURAL)
    public SortedSet<PhoneNumber> phoneNumbers = new TreeSet<PhoneNumber>();

    @ElementCollection
    @Sort(type = SortType.NATURAL)
    public SortedSet<ExternalLink> externalLinks = new TreeSet<ExternalLink>();

    @ElementCollection
    @Sort(type = SortType.NATURAL)
    public SortedSet<Email> emails = new TreeSet<Email>();

    @ManyToOne
    @Fetch(FetchMode.SELECT)
    public Company company;

    /***********************************************************************************
     * Relationships to Child Entities                                                 *
     ***********************************************************************************/

    @OneToMany(mappedBy = "interviewee", cascade = CascadeType.ALL, orphanRemoval = true)
    @Sort(type = SortType.NATURAL)
    public List<ActiveInterview> activeInterviews = new ArrayList<ActiveInterview>();
    
    @OneToMany(mappedBy="candidate", cascade = CascadeType.ALL)
    public List<Feedback> feedbackList;

    @OneToMany(mappedBy="candidate", cascade = CascadeType.ALL, fetch=FetchType.LAZY, orphanRemoval = true)
	public List<CandidateFile> files;

    @OneToMany(mappedBy="candidate", cascade = CascadeType.REMOVE)
    public List<TemporaryFeedbackAuthorization> feedbackAuthorizations;
    
    @OneToMany(mappedBy="candidate", cascade = CascadeType.REMOVE)
    public List<CandidateJobStatus> candidateJobStatuses;

    /**
     * Indicates that feedback is hidden for all non-admin users
     */
    public boolean feedbackHidden;

    public Candidate(String name, Company company, User creator) {
        this.name = name;
        this.company = company;
        this.creator = creator;
        feedbackList = new LinkedList<Feedback>();
        feedbackHidden = false;
        files = new ArrayList<CandidateFile>();
    }

    public Candidate(Company company, User user) {
        this(null, company, user);
    }

    public Candidate() { }


    public List<ActiveInterview> getActiveInterviews(User user){
        AccessibleActiveInterviewsHelper queryHelper = new AccessibleActiveInterviewsHelper(user);
        ByInterviewee intervieweeFilter = new ByInterviewee();
        intervieweeFilter.include(this.id.toString());
        queryHelper.addFilter(intervieweeFilter);

        ByActive byActiveFilter = new ByActive();
        byActiveFilter.include(Boolean.TRUE.toString());
        queryHelper.addFilter(byActiveFilter);

        CriteriaQuery cq = queryHelper.finish();
        
        TableKey activeInterviewKey = queryHelper.getKeys().iterator().next();
        
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        cq.orderBy(cb.asc(queryHelper.field(
                        activeInterviewKey, "anticipatedDate")),
                cb.asc(queryHelper.field(activeInterviewKey, "created")));
                
        TypedQuery<ActiveInterview> query = JPA.em().createQuery(cq);
        return query.getResultList();
    }

    public List<ActiveInterview> getActiveInterviewsPastStarted() {
        //todo: modify this to use QueryHelper and filters
        List<ActiveInterview> pastStarted = activeInterviews;

        List<ActiveInterview> result = new LinkedList<ActiveInterview>();

        Iterator<ActiveInterview> activeInterviewsIter =  pastStarted.iterator();
        ActiveInterview curActiveInterview;
        while (activeInterviewsIter.hasNext()) {
            
            curActiveInterview = activeInterviewsIter.next();
            
            if (!curActiveInterview.getStatus().equals(
                    ActiveInterviewState.NOT_STARTED) && 
                    curActiveInterview.active) {
                
                result.add(curActiveInterview);
            }
        }
        
        return result;
    }

    //todo: the next 2 addFeedback methods are legitimate and used by the controller.  I suspect that the other variations
    //could be removed

    public Feedback addFeedback(User sourceUser, Feedback feedback){
        feedback.candidate = this;
        feedback.feedbackSource = sourceUser;

        return finishFeedbackCreation(sourceUser, feedback);
    }

    public Feedback addFeedback(TemporaryFeedbackAuthorization authorization, Feedback feedback){
        feedback.candidate = this;
        feedback.feedbackEmail = authorization.email;

        return finishFeedbackCreation(null, feedback);
    }

    private Feedback finishFeedbackCreation(User sourceUser, Feedback feedback) {
        if(sourceUser != null && feedback.activeInterview != null){
            ActiveInterviewStateChange stateChange =
                    feedback.activeInterview.getMostRecentStatusChange();
            if (stateChange.toState.equals(ActiveInterviewState.STARTED)) {
                stateChange = feedback.activeInterview.changeStatus(sourceUser,
                        ActiveInterviewState.FINISHED);
            }

            //If the above set the interview to "finished", or if we were
            //already in "finished" state, there may exist a notification to
            //pester the user for feedback--we find and remove it
            if (stateChange.toState.equals(ActiveInterviewState.FINISHED)) {
                feedback.activeInterview.getMostRecentStatusChange().removeNotifications();
            }
        }

        feedbackList.add(feedback);

        save();

        return feedback;
    }

    
    public Feedback addFeedback(User sourceUser, ActiveInterview ai, 
                FeedbackSummary summary, String comments) {
        
        Feedback result = new Feedback(this, sourceUser, ai, 
                summary, comments, false);
        
        if (ai != null) {
            ActiveInterview aInterview = (ActiveInterview) ai;
            ActiveInterviewStateChange stateChange = 
                    aInterview.getMostRecentStatusChange();
            if (stateChange.toState.equals(ActiveInterviewState.STARTED)) {
                stateChange = aInterview.changeStatus(sourceUser, 
                        ActiveInterviewState.FINISHED);
            }
            
            //If the above set the interview to "finished", or if we were 
            //already in "finished" state, there may exist a notification to
            //pester the user for feedback--we find and remove it
            if (stateChange.toState.equals(ActiveInterviewState.FINISHED)) {
                aInterview.getMostRecentStatusChange().removeNotifications();
            }
        }
        
        feedbackList.add(result);
        
        save();
        
        return result;
    }
    
    public Feedback addFeedback(User sourceUser, String regardingMagicID, 
                FeedbackSummary summary, String comments) {
        
        Feedback result = new Feedback(this, sourceUser, regardingMagicID, 
                summary, comments, false);
        feedbackList.add(result);
        save();

        ActiveInterview interview =
                Interview.getCandidateInterviewAssociationFromMagicID(
                        regardingMagicID);

        if (interview != null) {
            ActiveInterview aInterview = (ActiveInterview) interview;
            ActiveInterviewStateChange stateChange =
                    aInterview.getMostRecentStatusChange();
            if (stateChange.toState.equals(ActiveInterviewState.STARTED)) {
                stateChange = aInterview.changeStatus(sourceUser,
                        ActiveInterviewState.FINISHED);
            }

            //If the above set the interview to "finished", or if we were
            //already in "finished" state, there may exist a notification to
            //pester the user for feedback--we find and remove it
            if (stateChange.toState.equals(ActiveInterviewState.FINISHED)) {
                aInterview.getMostRecentStatusChange().removeNotifications();
            }
        }

        return result;
    }
    
    public Feedback addFeedback(String sourceEmail, String regardingMagicID, 
                FeedbackSummary summary, String comments) {
        
        Feedback result = new Feedback(this, sourceEmail, regardingMagicID, 
                summary, comments, false);
        
        feedbackList.add(result);
        
        save();
        
        return result;
    }
    
    public void addFile(CandidateFile file){
		if(file == null){
			throw new IllegalArgumentException();
		}
		files.add(file);
	}

    public List<Question> getQuestionsToAvoid(){
    	List<Question> questionsToAvoid = new ArrayList<Question>();
    	List<ActiveInterview> interviews = getActiveActiveInterviews();
    	if(interviews != null){
    		for(ActiveInterview ci : interviews){
    			List<Question> alreadyAsked = ci.getQuestions();

    			questionsToAvoid.addAll(alreadyAsked);
    			for(Question q : alreadyAsked){
    				questionsToAvoid.addAll(q.getDuplicates());
    			}
    		}
    	}
    	return questionsToAvoid;
    }

    /**
     * Removes the file from the Candidate's collection of files and deletes the File Contents on S3.  Does not perform
     * any persistence but because the files relationship is defined as orphanRemoval = true, a subsequent save of the
     * candidate will persist the updated list of files and delete the CandidateFile record.
     * @param fileId Primary key of a CandidateFile
     */
    public void deleteFile(Long fileId){
    	CandidateFile file = CandidateFile.findById(fileId);
    	if (file != null){
    		file.contents.delete();
    		files.remove(file);
    	}
    }

    public List<ActiveInterview> getActiveActiveInterviews() {
        List<ActiveInterview> activeActiveInterviews = new ArrayList<ActiveInterview>();

        if (activeInterviews != null) {
            for (ActiveInterview activeInterview : activeInterviews) {
                if (activeInterview.active) {
                    activeActiveInterviews.add(activeInterview);
                }
            }
        }

        return activeActiveInterviews;
    }

    /**
     * If the user has access to the company the have access to its candidates
     * @param user User
     * @return whether the user has access to this candidate
     */
    @Override
    public boolean hasAccess(User user) {
        return (user != null) &&
                ((this.company != null &&
                        this.company.hasAccess(user)) ||
                        user.hasRole(RoleValue.APP_ADMIN));
    }

    @Override
    public boolean equals(Object o) {
        boolean result = (o instanceof Candidate);
        
        if (result) {
            Candidate oAsCandidate = (Candidate) o;
            
            result = ObjectUtils.equals(name, oAsCandidate.name) &&
                    ObjectUtils.equals(company, oAsCandidate.company);
        }
        
        return result;
    }
    
    @Override
    public int hashCode() {
        int hash = 13;
        hash = 83 * hash + ObjectUtils.hashCode(name);
        hash = 83 * hash + ObjectUtils.hashCode(company);
        
        return hash;
    }
}
