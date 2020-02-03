package controllers.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import controllers.api.serialization.QuestionDeserializer;
import controllers.api.serialization.QuestionSerializer;
import models.ActiveInterview;
import models.Interview;
import models.Question;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class QuestionsApi extends ApiController {

    public static void list(){
        Object entity = getSpecifiedEntity();

        Map<String, Object> result = new LinkedHashMap<String, Object>();

        List<Question> questions = new ArrayList<Question>();

        Interview interviewForSerializationContext = null;
        if(entity instanceof ActiveInterview){
            ActiveInterview interview = (ActiveInterview)entity;
            questions = interview.getQuestions();
            interviewForSerializationContext = interview;
        } else {
            throw new IllegalStateException(QuestionsApi.class.getName() + ".list method called with entity of" +
                    "type " + entity.getClass().getName());
        }

        result.put("data", questions);


        renderJSON(getDefaultGson(interviewForSerializationContext).toJson(result));
    }

    public static void get(){
        Object entity = getSpecifiedEntity();
        Map<String, Object> result = new LinkedHashMap<String, Object>();

        List<Question> questions = new ArrayList<Question>();
        if(entity instanceof Question){
            questions.add((Question)entity);
        } else {
            throw new IllegalStateException(QuestionsApi.class.getName() + ".get method called with entity of" +
                    "type " + entity.getClass().getName());
        }

        result.put("data", questions);

        renderJSON(getDefaultGson(null).toJson(result));
    }

    public static void create(String message){
        Question question = getDefaultGson(null).fromJson(message, Question.class);
        question.user = getUserInvokingService();
        question.initPublicWorkflow(null);
        question.save();
        renderJSON(buildSuccessResponseMap());
    }

    private static Gson getDefaultGson(Interview interviewForSerializationContext){
        return new GsonBuilder()
                .serializeNulls()
                .registerTypeAdapter(Question.class, new QuestionSerializer(getUserInvokingService().company, interviewForSerializationContext))
                .registerTypeAdapter(Question.class, new QuestionDeserializer())
                .create();
    }
}
