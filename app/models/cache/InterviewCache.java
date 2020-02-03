package models.cache;

import java.io.Serializable;

import org.hibernate.Hibernate;

import models.Candidate;
import models.Interview;
import models.InterviewQuestion;
import models.cache.CachedEntity.CachedEntityContext;
import play.mvc.Util;
import controllers.Security;
import exceptions.CacheMiss;

/**
 * Provides access to cached Interviews
 * 
 * @author joel
 *
 */
public class InterviewCache {
	
	private InterviewCache(){}

    /**
     * Save a new interview to cache as the in progress interview.
     * @param interview
     * @return The in progress interview
     */
    @Util
    public static Interview createInProgressInterview(){
    	return createInProgressInterview(new Interview(Security.connectedUser()));
    }
    
    public static Interview createInProgressInterview(Interview interview){
    	
    	Hibernate.initialize(interview.interviewQuestions);
    	return setInProgressInterview(interview);
    }
    
    /**
     * Save the given interview to cache as the in progress interview.
     * If there's already an interview in progress then it will be replaced but the
     * context will be preserved.
     * @param interview
     * @return The in progress interview
     */
    @Util
    public static Interview setInProgressInterview(Interview interview){
    	try {
            CachedEntity ce;
            ce = EntityCache.get(Interview.class);
            return EntityCache.savePrivate(interview, ce.getContext(), Interview.class).getEntity();
        } catch (CacheMiss e) {
        }
        
        return EntityCache.savePrivate(interview, Interview.class);
    }
    
    /**
     * Save the given interview to cache as the in progress interview.
     * @param interview
     * @param context context for cache
     * @return The in progress interview
     */
    @Util
    public static Interview setInProgressInterview(Interview interview, InProgressInterviewContext context){
    	
    	CachedEntity<Interview, InProgressInterviewContext> ce = EntityCache.savePrivate(interview, context, Interview.class);
    	if (ce != null){
    		return ce.getEntity();
    	}
    	return null;
    }
    
    /**
     * @return The in progress interview from cache or null if there isn't one.
     * @throws CacheMiss 
     */
    @Util
    public static Interview getInProgressInterview() throws CacheMiss{
    	
        return getInProgressInterviewEntity().getEntity();
    }
    
    /**
     * @return The in progress interview entity from cache or null if there isn't one.
     * @throws CacheMiss 
     */
    @Util
    public static CachedEntity<Interview, InProgressInterviewContext> getInProgressInterviewEntity() throws CacheMiss{
    	
        return  EntityCache.get(Interview.class);
    }
    
    /**
     * Gets the in progress interview from cache. If there isn't one then
     * one will be created.
     * 
     * @return An interview
     */
    @Util
    public static Interview safeGetInProgressInterview(){
    	
    	Interview interview;
		try {
			interview = getInProgressInterview();
		} catch (CacheMiss e) {
			interview = createInProgressInterview();
		}

        return interview;
    }
    
    /**
     * Gets the in progress interview entity from cache. If there isn't one then
     * one will be created.
     * 
     * @return An interview
     */
    @Util
    public static CachedEntity<Interview, InProgressInterviewContext> safeGetInProgressInterviewEntity(){
    	
    	CachedEntity<Interview, InProgressInterviewContext> ce;
		try {
			ce = getInProgressInterviewEntity();
		} catch (CacheMiss e) {
			Interview interview = createInProgressInterview();
			ce = new CachedEntity<Interview, InProgressInterviewContext>(interview,null);
		}

        return ce;
    }

    /**
     * Removes any inProgress interview from the cache
     */
    @Util
    public static void removeInProgressInterviewFromCache(){
    	
			try {
				EntityCache.deletePrivate(Interview.class);
			} catch (CacheMiss e) {
				//don't care
			}			
    }
    
    public static class InProgressInterviewContext extends CachedEntityContext implements Serializable {
    	private Candidate candidate;

		public InProgressInterviewContext(Candidate candidate) {
			super();
			this.candidate = candidate;
		}

		public Candidate getCandidate() {
			return candidate;
		}

		public void setCandidate(Candidate candidate) {
			this.candidate = candidate;
		}
    }
}
