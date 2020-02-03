package controllers.api.serialization;

import com.google.gson.*;
import models.ActiveInterview;
import models.Category;
import models.User;

import java.lang.reflect.Type;
import java.util.List;

public class InterviewSerializer extends BaseSerializer implements JsonSerializer<ActiveInterview> {

    private final User user;

    public InterviewSerializer(User user) {
        this.user = user;
    }

    public JsonElement serialize(ActiveInterview i, Type type,
                                 JsonSerializationContext jsc) {
        JsonObject result = new JsonObject();

        addStandardModelFields(result, i);

        nullSafeAdd(result, "name", i.name);
        nullSafeAdd(result, "date", i.anticipatedDate);
        nullSafeAdd(result, "duration", i.getDuration());
        nullSafeAdd(result, "questionCount", i.getQuestionCount());
        nullSafeAddId(result, "company", i.company);
        nullSafeAddId(result, "candidate", i.interviewee);
        nullSafeAdd(result,"status", i.getStatus());
        nullSafeAddField(result, "creator", i.user, "fullName");
        nullSafeAddField(result, "interviewer", i.interviewer, "fullName");

        List<Category> categories = i.getCategoriesInUse();
        JsonArray categoryNames = new JsonArray();
        for (Category category : categories) {
            categoryNames.add(new JsonPrimitive(category.name));
        }

        result.add("categories", categoryNames);

        nullSafeAdd(result, "hasAccessingUserProvidedFeedback", i.hasUserProvidedFeedback(user));

        return result;
    }
}
