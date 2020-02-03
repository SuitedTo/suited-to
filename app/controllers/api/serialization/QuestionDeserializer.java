package controllers.api.serialization;

import com.google.gson.*;
import models.Question;

import java.lang.reflect.Type;

/**
 * todo: this is really not ready for "prime time" for handling a full API but will handle creating a question with
 * the appropriate text filled in.
 */
public class QuestionDeserializer implements JsonDeserializer<Question> {

    @Override
    public Question deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();
        Long id = null;
        JsonPrimitive idPrimitive = obj.getAsJsonPrimitive("id");
        if(idPrimitive != null){
            id = idPrimitive.getAsLong();
        }

        Question question = new Question();

        JsonPrimitive text = obj.getAsJsonPrimitive("text");
        if(text != null){
            question.text = text.getAsString();
        }




        return question;
    }
}
