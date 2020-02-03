import java.io.BufferedReader;
import java.io.InputStreamReader;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import play.exceptions.UnexpectedException;


public class ClientAppTest {
	
	/*
	 * The clientId and clientSecret need to be in the server db - PREP_Client table
	 */
	final static String clientId = "3fe81990";
	final static String clientSecret = "5841ecf0";
	
	/*
	 * Your prepado login credentials
	 */
	final static String user = "joel.johnston@sparcedge.com";
	final static String password = "googlerules";
	
	/*
	 * Host
	 */
	static String host = "https://dev.prepado.com/";

	
	
	public static void main(String[] args){
		
		if(args.length > 0){
			host = args[0];
		}
		//log in first
		String sessionId = createSession(host, user, password);
		
		
		if(sessionId != null){
			
			//Now you have a session id, use it to do whatever you want...
			
			
			//get an interview and print it out
			int interviewId = 7266055;
			System.out.println(getInterview(host, interviewId, sessionId));
		}
		
	}
	
	static String getInterview(String host, int id, String sessionId){
		HttpClient httpclient = new DefaultHttpClient();
		
		HttpGet httpget = new HttpGet(host + "interview/" + id);
		
		
		try {
			
			setHeaders(httpget, sessionId);
			
			HttpResponse response = httpclient.execute(httpget);
			if (response.getStatusLine().getStatusCode() != 200) {
				System.out.println("ERROR : "
				   + response.getStatusLine().getStatusCode());
			}
			BufferedReader br = new BufferedReader(
                    new InputStreamReader((response.getEntity().getContent())));

			StringBuffer interviews = new StringBuffer();
			String line = br.readLine();
			while(line != null) {
				interviews.append(line);
				line = br.readLine();
			}

			httpclient.getConnectionManager().shutdown();
			
			return interviews.toString();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	static void setHeaders(HttpRequestBase request, String sessionId){
		try {
			
			final String timestamp = "" + System.currentTimeMillis();
			final String idName = (sessionId == null)?"clientid":"sessionid";
			final String id = (sessionId == null)?clientId:sessionId;
			
			final String signature = sign(timestamp + id, clientSecret.getBytes("UTF-8"));
			
			
			request.setHeader(idName, id);
			request.setHeader("timestamp", timestamp);
			request.setHeader("signature", signature);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	static void updateInterview(String host, int id, String sessionId){
		HttpClient httpclient = new DefaultHttpClient();
		HttpPut httpput = new HttpPut(host + "interview/" + id);
		
		
		try {
		
			StringEntity requestEntity = new StringEntity(
				    "{\"id\": " + id + ",\"name\":\"Software Developer interview\",\"currentQuestion\":5}",
				    "application/json",
				    "UTF-8");
			
			setHeaders(httpput, sessionId);
			
			httpput.setEntity(requestEntity);
			
			HttpResponse response = httpclient.execute(httpput);
			if (response.getStatusLine().getStatusCode() != 200) {
				System.out.println("ERROR : "
				   + response.getStatusLine().getStatusCode());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	static String createSession(String host, String username, String password){
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(host + "session");
		
		
		try {
		
			StringEntity requestEntity = new StringEntity(
				    "{\"email\": \"" + username + "\", \"password\": \"" + password + "\"}",
				    "application/json",
				    "UTF-8");
			
			setHeaders(httppost, null);
			
			httppost.setEntity(requestEntity);
			
			HttpResponse response = httpclient.execute(httppost);
			if (response.getStatusLine().getStatusCode() != 200) {
				System.out.println("ERROR : "
				   + response.getStatusLine().getStatusCode());
			}
			
			Header[] responseHeaders = response.getHeaders("SESSIONID");
			if(responseHeaders.length == 1){
				String sessionId = responseHeaders[0].getValue();
				System.out.println("A session was successfully created with a SESSIONID of " + responseHeaders[0].getValue());
				return sessionId;
			} else {
				System.out.println("No session established");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	static String sign(String message, byte[] key) {

        if (key.length == 0) {
            return message;
        }

        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            SecretKeySpec signingKey = new SecretKeySpec(key, "HmacSHA1");
            mac.init(signingKey);
            byte[] messageBytes = message.getBytes("utf-8");
            byte[] result = mac.doFinal(messageBytes);
            int len = result.length;
            char[] hexChars = new char[len * 2];


            for (int charIndex = 0, startIndex = 0; charIndex < hexChars.length;) {
                int bite = result[startIndex++] & 0xff;
                hexChars[charIndex++] = HEX_CHARS[bite >> 4];
                hexChars[charIndex++] = HEX_CHARS[bite & 0xf];
            }
            return new String(hexChars);
        } catch (Exception ex) {
            throw new UnexpectedException(ex);
        }

    }
	
	private static final char[] HEX_CHARS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
}
