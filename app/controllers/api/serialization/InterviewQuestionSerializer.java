package controllers.api.serialization;

import com.google.gson.*;
import models.Company;
import models.Interview;
import models.InterviewQuestion;
import models.Question;

import java.lang.reflect.Type;

public class InterviewQuestionSerializer extends BaseSerializer implements JsonSerializer<InterviewQuestion> {

    /**
     * The company to use when serializing needed to retrieve company-specific notes, may be null
     */
    private final Company company;

    /**
     * The interview to use when determining interview order, may be null
     */
    private final Interview interview;

    public InterviewQuestionSerializer(Company company, Interview interview) {
        this.company = company;
        this.interview = interview;
    }

    public JsonElement serialize(InterviewQuestion iq, Type type,
                                 JsonSerializationContext jsc) {
        Question i = iq.question;

        JsonObject result = new JsonObject();

        addStandardModelFields(result, i);

        nullSafeAdd(result, "text", i.text);
        nullSafeAdd(result, "active", i.active);
        nullSafeAdd(result, "status", i.status);
        nullSafeAdd(result, "answers", i.answers);
        if (iq.comment != null) {
            nullSafeAdd(result, "comment", iq.comment);
        }
        if (iq.rating != null) {
            nullSafeAdd(result, "rating", iq.rating.getLabel());
        }
        nullSafeAdd(result, "tips", i.tips);
        nullSafeAddField(result, "category", i.category, "name");
        nullSafeAdd(result, "difficulty", i.difficulty);
        nullSafeAdd(result, "time", i.time);
        nullSafeAdd(result, "flaggedAsInapropriate", i.flaggedAsInappropriate);
        nullSafeAdd(result, "flaggedReason", i.flaggedReason);
        nullSafeAdd(result, "standardScore", i.standardScore);
        nullSafeAdd(result, "submittedTime", i.getTimeOfSubmission());
        if(company != null){
            nullSafeAddField(result, "companyNote", i.getQuestionNote(company), "text");
        } else {
            result.add("companyNote", new JsonNull());
        }
        if(interview != null){

            nullSafeAdd(result, "interviewOrder", interview.getOrderOfQuestion(i));
        } else {
            result.add("interviewOrder", new JsonNull());
        }


        return result;
    }
}
