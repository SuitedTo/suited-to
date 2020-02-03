package controllers.api.serialization;

import com.google.gson.*;

import models.ActiveInterview;
import models.Category;
import models.User;

import java.lang.reflect.Type;
import java.util.List;

public class CategorySerializer extends BaseSerializer implements JsonSerializer<Category> {

    public JsonElement serialize(Category c, Type type,
                                 JsonSerializationContext jsc) {
        JsonObject result = new JsonObject();

        addStandardModelFields(result, c);

        nullSafeAdd(result, "name", c.name);
        nullSafeAdd(result, "companyName", c.companyName);
        nullSafeAdd(result, "date", c.created);

        return result;
    }
}
