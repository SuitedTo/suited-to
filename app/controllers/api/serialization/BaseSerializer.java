package controllers.api.serialization;

import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import models.ModelBase;
import org.apache.commons.lang.reflect.FieldUtils;
import play.Logger;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

abstract class BaseSerializer {

    private static final DateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");

    static void addStandardModelFields(JsonObject result, ModelBase entity){
        nullSafeAdd(result, "id", entity.id);
        nullSafeAdd(result, "created", entity.created);
        nullSafeAdd(result, "updated", entity.updated);
    }

    static void nullSafeAdd(JsonObject result, String fieldName, Object value){
        if(value instanceof String){
            nullSafeAdd(result, fieldName, (String)value);
        } else if (value instanceof Boolean){
            nullSafeAdd(result, fieldName, (Boolean) value);
        } else if (value instanceof Enum){
            nullSafeAddEnumName(result, fieldName, (Enum) value);
        } else {
            String stringValue = value != null ? value.toString() : null;
            nullSafeAdd(result, fieldName, stringValue);
        }
    }

    static void nullSafeAdd(JsonObject result, String fieldName, String value){
        if(value == null){
            result.add(fieldName, new JsonNull());
        } else {
            result.add(fieldName, new JsonPrimitive(value));
        }
    }

    static void nullSafeAdd(JsonObject result, String fieldName, Boolean value){
        if(value == null){
            result.add(fieldName, new JsonNull());
        } else {
            result.add(fieldName, new JsonPrimitive(value));
        }
    }

    static void nullSafeAdd(JsonObject result, String fieldName, Number value){
        if(value == null){
            result.add(fieldName, new JsonNull());
        } else {
            result.add(fieldName, new JsonPrimitive(value));
        }
    }

    static void nullSafeAdd(JsonObject result, String fieldName, Date value){
        if(value == null){
            result.add(fieldName, new JsonNull());
        } else {
            result.add(fieldName, new JsonPrimitive(dateFormat.format(value)));
        }
    }

    static void nullSafeAddId(JsonObject result, String fieldName, ModelBase value){
        if(value == null){
            result.add(fieldName, new JsonNull());
        } else {
            result.add(fieldName, new JsonPrimitive(value.id));
        }
    }

    static void nullSafeAddField(JsonObject result, String fieldName, Object value, String fieldToAdd){
        if(value == null){
            result.add(fieldName, new JsonNull());
        } else {
            try {

                nullSafeAdd(result, fieldName, FieldUtils.readDeclaredField(value, fieldToAdd));
            } catch (Exception e) {
                Logger.error("could not load property: " + fieldToAdd + " from object: " + value);
            }
        }
    }

    static void nullSafeAddEnumName(JsonObject result, String fieldName, Enum value){
        if(value == null){
            result.add(fieldName, new JsonNull());
        } else {
            nullSafeAdd(result, fieldName, value.name());
        }
    }


}
