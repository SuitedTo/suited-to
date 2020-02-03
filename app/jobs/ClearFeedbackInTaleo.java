package jobs;

import integration.taleo.TaleoCredentials;
import integration.taleo.TaleoHelper;
import integration.taleo.TaleoService;
import integration.taleo.generated.CalendarEventBean;
import integration.taleo.generated.LongArr;
import integration.taleo.generated.MapItem;
import integration.taleo.generated.SearchResultArr;
import integration.taleo.generated.SearchResultBean;
import integration.taleo.generated.WebServicesException;
import integration.taleo.types.EntityType;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import controllers.Security;

import models.Candidate;
import models.Feedback;

import play.Logger;
import play.jobs.Job;

public class ClearFeedbackInTaleo extends Job{
	private TaleoService taleo;
	private long taleoCandidateId;
	
	public ClearFeedbackInTaleo(TaleoService taleo, long taleoCandidateId){
		this.taleo = taleo;
		this.taleoCandidateId = taleoCandidateId;		
	}

	public ClearFeedbackInTaleo(long taleoCandidateId){
		try{
			taleo = TaleoService.INSTANCE();
			this.taleoCandidateId = taleoCandidateId;
		} catch (Exception e) {
			Logger.error("Unable to create ClearFeedbackInTaleo job: %s", e.getMessage());
		}
	}

	public void doJob(){
		try {

			if(taleo == null){
				return;
			}
			
			if(taleoCandidateId <= 0){
				return;
			}

			LongArr ids = taleo.getEventByEntity(EntityType.CAND, taleoCandidateId);
			if(ids != null){
				for(long id : ids.getArray().getItem()){
					CalendarEventBean next = taleo.getEventById(id);
					String subject = next.getSubject();
					if((subject != null) && (subject.equals("SuitedTo Interview Feedback"))){
						taleo.deleteEvent(id);
					}
				}
			}

		} catch (Exception e) {
			Logger.error("FeedbackToTaleo job failed: %s", e.getMessage());
		}
	}
}
