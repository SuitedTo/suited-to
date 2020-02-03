package controllers.prep.delegates;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;

import dto.suitedto.SuitedToActiveInterviewDTO;
import models.ActiveInterview;
import models.Candidate;
import models.filter.activeinterview.ActiveInterviewFilter;
import models.filter.activeinterview.ByInterviewee;
import models.query.activeinterview.ActiveInterviewQueryHelper;
import play.Logger;
import play.db.jpa.JPA;
import controllers.Security;
import data.binding.types.FilterBinder;

public class InterviewsDelegate {

	public static List<SuitedToActiveInterviewDTO> list(String filters){
		List<SuitedToActiveInterviewDTO> dtos = new ArrayList<SuitedToActiveInterviewDTO>();
			/*
			 * Temporarily relying on an email match (the connected user's email will need to match one of the emails associated with
			 * a candidate). 
			 */
			Query q = JPA.em().createQuery("from Candidate c inner join c.emails e where e.emailAddress like :email");
			q.setParameter("email", Security.connectedUser().email);
			List candidates = q.getResultList();
			if((candidates != null) && (candidates.size() > 0)){
				Candidate c = (Candidate)candidates.get(0);

				ActiveInterviewQueryHelper queryHelper = new ActiveInterviewQueryHelper();
				
				ByInterviewee candidateFilter = new ByInterviewee();
				candidateFilter.include(String.valueOf(c.id));
				queryHelper.addFilter(candidateFilter);

				if(filters != null){
					FilterBinder fb = new FilterBinder();
					String[] filterStrings = filters.split(",");
					for (String filterString : filterStrings){
						try {
							queryHelper.addFilter(((ActiveInterviewFilter) fb.bind(null, null, filters, ActiveInterviewFilter.class, null)));
						} catch (Exception e) {
							Logger.error("Unable to parse filter string %s", filterString);
						}
					}
				}
				
				CriteriaQuery cq = queryHelper.finish();
		        TypedQuery<ActiveInterview> query = JPA.em().createQuery(cq);
		        List<ActiveInterview> interviews = query.getResultList();
		        for(ActiveInterview interview : interviews){
		        	dtos.add(SuitedToActiveInterviewDTO.fromSuitedToActiveInterview(interview));
		        }
			}
			return dtos;
	}


	public static SuitedToActiveInterviewDTO get(Long id){
		if(id == null){
			return null;
		}
		return SuitedToActiveInterviewDTO.fromSuitedToActiveInterview((ActiveInterview) ActiveInterview.findById(id));
	}
}
