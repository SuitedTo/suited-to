package common.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map.Entry;
import java.util.Set;

import models.ModelBase;

import org.apache.commons.lang.reflect.FieldUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

import play.Logger;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public abstract class JsonUtil {

    private JsonUtil(){}
    
    /**
     * Merge the given object into the given destination using the default
     * conflict resolution strategy.
     * 
     * @param destination The other object will be merged into this one.
     * @param object Will be merged into the destination object.
     * @return
     */
    public static JsonObject merge(JsonObject destination, JsonObject object){
    	if(destination == null){
    		destination = new JsonObject();
    	}
    	if(object == null){
    		object = new JsonObject();
    	}
    	
    	Set<Entry<String, JsonElement>> entries = object.entrySet();
    	for(Entry<String, JsonElement> entry : entries){
    		if(destination.get(entry.getKey()) != null){
    			//default strategy is to ignore conflicting props
    		} else {
    			destination.add(entry.getKey(), entry.getValue());
    		}
    	}
    	return destination;
    }
    
    /**
     * Convert an object to JsonNode.
     *
     * @param data Value to convert in Json.
     */
    public static JsonNode toJson(final Object data) {
        try {
            return new ObjectMapper().valueToTree(data);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Convert a JsonNode to a Java value
     *
     * @param json Json value to convert.
     * @param clazz Expected Java value type.
     */
    public static <A> A fromJson(JsonNode json, Class<A> clazz) {
        try {
            return new ObjectMapper().treeToValue(json, clazz);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a new empty ObjectNode.
     */ 
    public static ObjectNode newObject() {
        return new ObjectMapper().createObjectNode();
    }

    /**
     * Convert a JsonNode to its string representation.
     */
    public static String stringify(JsonNode json) {
        return json.toString();
    }

    /**
     * Parse a String representing a json, and return it as a JsonNode.
     */
    public static JsonNode parse(String src) {
        try {
            return new ObjectMapper().readValue(src, JsonNode.class);
        } catch(Throwable t) {
            throw new RuntimeException(t);
        }
    }
}
