package controllers.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import controllers.api.serialization.InterviewSerializer;
import data.binding.types.FilterBinder;
import models.*;
import models.filter.activeinterview.ActiveInterviewFilter;
import models.filter.activeinterview.ByCompany;
import models.filter.activeinterview.ByInterviewee;
import models.filter.activeinterview.ByUser;
import models.query.activeinterview.ActiveInterviewQueryHelper;
import play.data.binding.As;
import play.db.jpa.JPA;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import java.util.*;

public class InterviewsApi extends ApiController {

    /**
     * Retrieve a list of interviews grouped by the anticipated date of the interview.  Interviews with a null
     * anticipated date will be grouped by the value "unknown"
     */
    public static void list(@As(value = ",", binder = FilterBinder.class) List<ActiveInterviewFilter> filters) {
        ModelBase entity = getSpecifiedEntity();


        ActiveInterviewQueryHelper queryHelper = new ActiveInterviewQueryHelper();
        if (filters != null) {
            for (ActiveInterviewFilter filter : filters) {
                queryHelper.addFilter(filter);
            }
        }

        if (entity instanceof Company) {
            ByCompany filter = new ByCompany();
            filter.include(entity.id.toString());
            queryHelper.addFilter(filter);
        } else if (entity instanceof Candidate) {
            ByInterviewee filter = new ByInterviewee();
            filter.include(entity.id.toString());
            queryHelper.addFilter(filter);
        } else if (entity instanceof User) {
            ByUser filter = new ByUser();
            filter.include(entity.id.toString());
            queryHelper.addFilter(filter);
        } else {
            throw new IllegalStateException(InterviewsApi.class.getName() + ".list method called with entity of" +
                    "type " + entity.getClass().getName());
        }

        CriteriaQuery cq = queryHelper.finish();
        TypedQuery<ActiveInterview> query = JPA.em().createQuery(cq);
        List<ActiveInterview> interviews = query.getResultList();
        Map<String, List<Interview>> interviewsByDate = new LinkedHashMap<String, List<Interview>>();
        for (ActiveInterview interview : interviews) {

            String interviewDate = interview.anticipatedDate != null ?
                    dateFormat.format(interview.anticipatedDate) : "unknown";

            List<Interview> interviewsForDate = interviewsByDate.get(interviewDate);
            if(interviewsForDate == null){
                interviewsForDate = new ArrayList<Interview>();
            }
            interviewsForDate.add(interview);
            interviewsByDate.put(interviewDate, interviewsForDate);
        }

        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("data", interviewsByDate);

        renderJSON(getDefaultGson().toJson(result));

    }

    public static void get() {
        Object entity = getSpecifiedEntity();

        Map<String, Object> result = new LinkedHashMap<String, Object>();
        List<ActiveInterview> interviews = new ArrayList<ActiveInterview>();
        if (entity instanceof ActiveInterview) {
            interviews.add((ActiveInterview) entity);
        } else {
            throw new IllegalStateException(InterviewsApi.class.getName() + ".get method called with entity of" +
                    "type " + entity.getClass().getName());
        }

        result.put("data", interviews);

        renderJSON(getDefaultGson().toJson(result));

    }

    private static Gson getDefaultGson() {
        return new GsonBuilder()
                    .serializeNulls()
                    .registerTypeAdapter(ActiveInterview.class, new InterviewSerializer(getUserInvokingService()))
                    .create();
    }

}
