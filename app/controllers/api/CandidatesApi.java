package controllers.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import controllers.api.serialization.CandidateSerializer;
import models.Candidate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CandidatesApi extends ApiController {

    private static final CandidateSerializer DEFAULT_CANDIDATE_SERIALIZER = new CandidateSerializer();

    public static void get(){
        Object entity = getSpecifiedEntity();
        Map<String, Object> result = new LinkedHashMap<String, Object>();

        List<Candidate> candidateList = new ArrayList<Candidate>();
        if(entity instanceof Candidate){
            candidateList.add((Candidate)entity);
        } else {
            throw new IllegalStateException(CandidatesApi.class.getName() + ".get method called with entity of" +
                    "type " + entity.getClass().getName());
        }

        result.put("data", candidateList);
        renderJSON(getDefaultGson().toJson(result));
    }

    private static Gson getDefaultGson() {
        return new GsonBuilder()
                .serializeNulls()
                .registerTypeAdapter(Candidate.class, DEFAULT_CANDIDATE_SERIALIZER)
                .create();
    }

}
