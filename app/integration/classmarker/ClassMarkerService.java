package integration.classmarker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import play.Play;
import play.libs.WS;
import play.libs.WS.HttpResponse;
import play.libs.WS.WSRequest;
import play.mvc.Http;

public class ClassMarkerService {
	
	private final static String API_KEY = Play.configuration.getProperty("classmarker.apiKey");
	private final static String API_SECRET = Play.configuration.getProperty("classmarker.secret");
	private final static String ALL_LINKS_RECENT_RESULTS_URL = "http://api.classmarker.com/v1/links/recent_results.json";

	private ClassMarkerService(){}
	
	public static QuizResult getQuizResult(long userId){
		QuizResult result = null;
		List<QuizResult> results = getQuizResults(userId);
		if(results.size() > 0){
			result = results.get(0);
		}
		return result;
	}
	
	private static List<QuizResult> getQuizResults(Long userId){
		List<QuizResult> rtn = new ArrayList<QuizResult>();
		HttpResponse res = buildRequest(ALL_LINKS_RECENT_RESULTS_URL).get();
		if(res.getStatus() == Http.StatusCode.OK){
			JsonElement json = res.getJson();
			final JsonObject jsonObject = json.getAsJsonObject();
			for (final Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
			   final String key = entry.getKey();
			   if(key.equals("results")){
				   final JsonArray results = entry.getValue().getAsJsonArray();
				   Iterator<JsonElement> it = results.iterator();
				   while(it.hasNext()){
					   QuizResult qr =
							   QuizResult.fromJson(it.next().getAsJsonObject().get("result").getAsJsonObject());
					   if((userId == null) || (userId.equals(qr.userId))){
						   rtn.add(qr);
					   }
				   }
			   }
			}
			Collections.sort(rtn);			
		}
		return rtn;
	}
	
	private static WSRequest buildRequest(String url){
		
		long now = System.currentTimeMillis()/1000L;
		
		long fiveMinutesAgo = now - 5*60L;
		
		String signature = DigestUtils.md5Hex(API_KEY + API_SECRET + now);
		
		WSRequest request = WS.url(url);
		
		request.setParameter("api_key", API_KEY);
		
		request.setParameter("signature", signature);
		
		request.setParameter("timestamp",now);
		
		request.setParameter("finishedAfterTimestamp", fiveMinutesAgo);
		
		return request;
	}
	
	public static class QuizResult implements Comparable<QuizResult>{
		private final long userId; 
		private final int score;

		public QuizResult(long userId, int score) {
			this.userId = userId;
			this.score = score;
		}
		
		public static QuizResult fromJson(JsonObject json) {
			long userId = json.get("cm_user_id").getAsLong();
			int score = json.get("percentage").getAsInt();
			
			return new QuizResult(userId, score);
		}
		
		public long getUserId(){
			return userId;
		}
		
		public int getScore(){
			return score;
		}

		@Override
		public int compareTo(QuizResult other) {
			return other.score - score;
		}
	}
}
