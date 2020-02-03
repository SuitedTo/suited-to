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
import enums.FeedbackSummary;

import models.Candidate;
import models.Feedback;
import models.Interview;

import play.Logger;
import play.jobs.Job;

public class FeedbackToTaleo extends Job{
	private TaleoService taleo;
	private long candidateId;
	
	public FeedbackToTaleo(TaleoService taleo, long candidateId){
		this.taleo = taleo;
		this.candidateId = candidateId;		
	}

	public FeedbackToTaleo(long candidateId){
		try{
			taleo = TaleoService.INSTANCE();
			this.candidateId = candidateId;
		} catch (Exception e) {
			Logger.error("Unable to create FeedbackToTaleo job: %s", e.getMessage());
		}
	}

	public void doJob(){
		try {

			if(taleo == null){
				return;
			}
			
			Candidate candidate = Candidate.findById(candidateId);
			if(candidate == null){
				return;
			}
			
			long taleoCandidateId = candidate.taleoCandId;
			
			if(taleoCandidateId <= 0){
				return;
			}

			//No great way to update an event
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

			List<Feedback> fb = candidate.feedbackList;
			if((fb != null) && (fb.size() > 0)){

				CalendarEventBean event = new CalendarEventBean();

				event.setEntityId(taleoCandidateId);
				event.setEntityType(EntityType.CAND.name());

				GregorianCalendar now = new GregorianCalendar();
				now.setTime(new Date());
				XMLGregorianCalendar startDate;
				startDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(now);
				event.setStartDate(startDate);
				event.setDuration(15L * 60000L);			
				event.setStatus("New");
				event.setSubject("SuitedTo Interview Feedback");
				
				Collections.sort(fb, new Comparator<Feedback>(){

					@Override
					public int compare(Feedback arg0, Feedback arg1) {
						return arg0.created.compareTo(arg1.created);
					}
					
				});
				
				StringBuilder aggregatedFeedback = new StringBuilder();
				for(Feedback feedback : fb){
					aggregatedFeedback.append(feedback.feedbackSource.fullName);
					aggregatedFeedback.append("(").append(feedback.feedbackSource.email).append(")").append("\n");
					aggregatedFeedback.append("Submitted: ").append(feedback.created).append("\n");
					
					Interview interview = feedback.activeInterview;
					if(interview != null){
						aggregatedFeedback.append("Interview: ").append(interview.name).append("\n");
					}else{
						aggregatedFeedback.append("Interview: ").append("-none-").append("\n");
					}
					
					FeedbackSummary nextStep = feedback.summaryChoice;
					if(nextStep != null){
						aggregatedFeedback.append("Recommendation: ").append(nextStep).append("\n");
					}else{
						aggregatedFeedback.append("Recommendation: ").append("-none-").append("\n");
					}
					
					
					aggregatedFeedback.append(feedback.comments).append("\n\n");
				}
				event.setDescription(aggregatedFeedback.toString());
				
				taleo.createEvent(event);
			}


		} catch (Exception e) {
			Logger.error("FeedbackToTaleo job failed: %s", e.getMessage());
		}
	}
}
