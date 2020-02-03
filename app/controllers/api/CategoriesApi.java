package controllers.api;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import models.Category;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import controllers.api.serialization.CategorySerializer;

public class CategoriesApi extends ApiController{
	
	public static void list(){
		List<Category> categories = Category.find("byIsAvailableExternally", true).fetch();
		
		Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("data", categories);
        renderJSON(getDefaultGson().toJson(result));
	}
	
	public static void get(){
		Object entity = getSpecifiedEntity();
        Map<String, Object> result = new LinkedHashMap<String, Object>();

        List<Category> categoryList = new ArrayList<Category>();
        if(entity instanceof Category){
        	categoryList.add((Category)entity);
        } else {
            throw new IllegalStateException(CategoriesApi.class.getName() + ".get method called with entity of" +
                    "type " + entity.getClass().getName());
        }

        result.put("data", categoryList);
        renderJSON(getDefaultGson().toJson(result));
	}
	
	private static Gson getDefaultGson() {
        return new GsonBuilder()
                    .serializeNulls()
                    .registerTypeAdapter(Category.class, new CategorySerializer())
                    .create();
    }
}
