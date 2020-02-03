package controllers.prep.rest;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.basic.DefaultOAuthConsumer;

import org.apache.commons.codec.binary.Base64;

import play.Logger;
import play.jobs.Job;
import play.libs.F.Promise;
import play.mvc.Http.Cookie;
import play.mvc.Http.Request;
import utils.EncodingUtil;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import controllers.prep.auth.PublicAction;

public class LinkedInTokens extends PrepController{

	/**
	 * Take the short lived bearer token from the linkedin_oauth cookie, exchange it
	 * for an access token, and pass back the access token.
	 */
	@PublicAction
	public static void get(){
		Request request = Request.current();
		Map<String, Cookie> cookies = request.cookies;
		Iterator<String> keys = cookies.keySet().iterator();
		while(keys.hasNext()){
			String key = keys.next();
			if(key.startsWith("linkedin_oauth")){
				Cookie cookie = cookies.get(key);
				String value = EncodingUtil.decodeURIComponent(cookie.value);
				if(String.valueOf(value).equals("null")){
					continue;
				}
				JsonObject credentials = new JsonParser().parse(value).getAsJsonObject();
				JsonElement so = credentials.get("signature_order");
				if((so != null) && (so.isJsonArray())){
					StringBuilder baseString = new StringBuilder();
					JsonArray soArray = so.getAsJsonArray();
					Iterator<JsonElement> it = soArray.iterator();
					while(it.hasNext()){
						baseString.append(credentials.get(it.next().getAsString()).getAsString());
					}
					
					if(validateSignature(
							credentials.get("signature_version").getAsInt(),
							baseString.toString(),
							credentials.get("signature").getAsString())){
						
						OAuthConsumer consumer = new DefaultOAuthConsumer(
								play.Play.configuration.getProperty("prep.linkedin.api-key"),
								play.Play.configuration.getProperty("prep.linkedin.api-secret"));
						
						final String bearerToken = credentials.get("access_token").getAsString();
						
						String accessTokenString = await(exchangeTokens(bearerToken, consumer));
						if(accessTokenString != null){
							renderRefinedJSON(parseQuery(accessTokenString));
						}
						
					} else {
						Logger.error("Invalid signature found in linkedin_oauth cookie");
					}
				}
			}
		}
		emptyObject();
	}
	
	private static Promise<String> exchangeTokens(final String bearerToken, final OAuthConsumer consumer){
		return new Job<String>(){
			public String doJobWithResult(){
				try {
		        	URL url = new URL("https://api.linkedin.com/uas/oauth/accessToken?xoauth_oauth2_access_token=" + bearerToken);
		            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		            
		            conn.setRequestMethod("POST");
		            conn.setDoOutput(true);
		            conn.setDoInput(true);
		            
		            consumer.sign(conn);
		            
		            conn.connect();
		            
		            BufferedReader in = (conn.getResponseCode() < 400)? new BufferedReader(new InputStreamReader(conn.getInputStream()))
	                	:  new BufferedReader(new InputStreamReader(conn.getErrorStream()));
	                
	                StringBuilder providerResponse = new StringBuilder();
	                String line = in.readLine();
	                while(line != null){
	                	providerResponse.append(line);
	                	line = in.readLine();
	                }
	                
	                in.close();
	                
	                return providerResponse.toString();
	                
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
			}
		}.now();
	}
	
	private static Map<String, String> parseQuery(String query) {
	    Map<String, String> props = new Hashtable<String, String>();
	    String[] pairs = query.trim().split("&");
	    for (String pair : pairs) {
	        int pos = pair.indexOf("=");
	        props.put(pair.substring(0, pos), pair.substring(pos + 1));
	    }
	    return props;
	}
	
	private static String sign(String message, String key){
		try{
			Mac mac = Mac.getInstance("HmacSHA1");
			SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), "HmacSHA1");
			mac.init(signingKey);
			byte[] messageBytes = message.getBytes("utf-8");
			return new String(Base64.encodeBase64(mac.doFinal(messageBytes)));
		} catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	private static boolean validateSignature(int signatureVersion, String baseString, String signature){
		if (signatureVersion == 1) {
		    return sign(baseString, play.Play.configuration.getProperty("prep.linkedin.api-secret"))
		    		.equals(signature);
		} else {
			Logger.error("Unknown linkedin_oauth cookie version");
		}
		return false;
	}
	
	//private static final String testVal = "{\"signature_version\":\"1\",\"signature_method\":\"HMAC-SHA1\",\"signature_order\":[\"access_token\",\"member_id\"],\"access_token\":\"ldLsvcIaqv7KRzhDKfAgkoTeX1hIsc3kcl9i\",\"signature\":\"jCqDoyzBqXYsRHxo0zVA+ssg7m0=\",\"member_id\":\"XG7KKXnwu_\"}";
}
