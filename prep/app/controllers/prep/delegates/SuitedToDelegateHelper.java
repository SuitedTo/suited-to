package controllers.prep.delegates;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import dto.suitedto.SuitedToDTO;
import models.ModelBase;
import play.classloading.enhancers.ControllersEnhancer.ControllerInstrumentation;
import play.mvc.Http.Response;
import play.mvc.Scope.RouteArgs;
import play.mvc.results.Result;
import play.utils.Java;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import controllers.Security;

public class SuitedToDelegateHelper {
	
	private SuitedToDelegateHelper(){}

	public static void prepareForAPICall(){
		prepareForAPICall(null);
	}
	
	public static void prepareForAPICall(ModelBase entity){
		ControllerInstrumentation.initActionCall();
		if(entity != null){
			RouteArgs.current().put("entity", entity);
			RouteArgs.current().put("user", Security.connectedUser());
		}
	}
	
	private static String extractResponse(Result result){
		Response r = new Response();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		r.out = baos;
		result.apply(null, r);
		try {
			return new String(baos.toByteArray(), "UTF-8");
		} catch (UnsupportedEncodingException e){
			
			e.printStackTrace();
		}
		return null;
	}
	
	public static JsonObject extractRawResponseObject(Result result){
		String response = extractResponse(result);
		if(response == null){
			return new JsonObject();
		}
		return extractDataAsObject(response);
	}
	
	public static <T extends SuitedToDTO> T extractSingleResponse(Class<T> clazz, Result result){
		String response = extractResponse(result);
		if(response == null){
			return null;
		}
		JsonObject data = extractDataAsObject(response);
		T dto = null;
		if(response != null){
			try {
				dto = (T) Java.invokeStatic(clazz, "fromJson", data.toString());
			} catch (Exception e1) {
			}
		}
		return dto;
	}
	
	public static <T extends SuitedToDTO> List<T> extractListResponse(Class<T> clazz, Result result){
		String response = extractResponse(result);
		if(response == null){
			return null;
		}
		JsonArray data = extractDataAsArray(response);
		Iterator<JsonElement> elements = data.iterator();
		List<T> dtos = new ArrayList<T>();
		while(elements.hasNext()){
			JsonElement e = elements.next();
			try {
				dtos.add((T) Java.invokeStatic(clazz, "fromJson", e.toString()));
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
		return dtos;
	}
	
	private static JsonArray extractDataAsArray(String data){
		if(data == null){
			return null;
		}
		JsonElement e = new JsonParser().parse(data);
		
		return e.getAsJsonObject().get("data").getAsJsonArray();
	}
	
	private static JsonObject extractDataAsObject(String data){
		
		if(data == null){
			return null;
		}
		JsonElement e = new JsonParser().parse(data);
		
		return e.getAsJsonObject().get("data").getAsJsonObject();
	}
}
