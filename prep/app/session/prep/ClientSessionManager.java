package session.prep;

import java.util.Map;
import java.util.UUID;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import models.prep.PrepClient;
import models.prep.PrepClientSession;
import models.prep.PrepUser;
import play.Play;
import play.libs.Crypto;
import play.mvc.Http;
import play.mvc.Http.Header;

public class ClientSessionManager extends SessionManager{
	
	private PrepClientSession session;

	@Override
	public Long getSessionOwnerId() {
		if((session != null) && (session.isValid())){
			return session.user.id;
		}
		return null;
	}

	@Override
	public void createSession(PrepUser owner) {
		if(owner != null){
			session = new PrepClientSession();
			
			Http.Request request = Http.Request.current();
			
			Map<String, Header> headers = request.headers;
			
			Header cidHeader = headers.get("clientid");
			if(cidHeader == null){
				return;
			}
			
			Header sigHeader = headers.get("signature");
			if(sigHeader == null){
				return;
			}
			
			Header tsHeader = headers.get("timestamp");
			if(tsHeader == null){
				return;
			}
			
			if(!isValidTimestamp(tsHeader.value())){
				return;
			}

			String clientId = cidHeader.value();
			String timestampStr = tsHeader.value();
			String signatureStr = sigHeader.value();
			
			PrepClient client = PrepClient.find.where().eq("clientId", clientId).findUnique();
			if(client == null){
				return;
			}
			
			String secret = client.clientSecret;
			String signature = Crypto.sign(timestampStr + clientId, secret.getBytes());
			
			if(signatureStr.equals(signature)){
				session.client = client;
				session.user = owner;
				session.sessionId = UUID.randomUUID().toString();
				session.sessionId = Crypto.sign(session.sessionId, Play.secretKey.getBytes()) + "-" + session.sessionId;
			}
		}
	}

	@Override
	public void restoreSession() {
		session = new PrepClientSession();
		
		if(!Play.started){
			return;
		}
		
		Http.Request request = Http.Request.current();
		
		Map<String, Header> headers = request.headers;
		Header sidHeader = headers.get("sessionid");
		if(sidHeader == null){
			return;
		}
		
		Header sigHeader = headers.get("signature");
		if(sigHeader == null){
			return;
		}
		
		Header tsHeader = headers.get("timestamp");
		if(tsHeader == null){
			return;
		}
		
		if(!isValidTimestamp(tsHeader.value())){
			return;
		}

		String sessionId = sidHeader.value();
		String timestampStr = tsHeader.value();
		String signatureStr = sigHeader.value();
		
		PrepClientSession dbSession = PrepClientSession.find.where().eq("sessionId", sessionId).findUnique();
		if(dbSession == null){
			return;
		}
		
		dbSession.client.refresh();
		
		String secret = dbSession.client.clientSecret;
		String signature = Crypto.sign(timestampStr + sessionId, secret.getBytes());
		
		if(signatureStr.equals(signature)){
			session = dbSession;
		}
		
	}

	@Override
	public void clearSession() {
		if(session != null){
			session.delete();
			session = null;
		}
	}

	@Override
	public void saveSession() {
		if((session != null) && (session.isValid())){
			session.save();
			Http.Response.current().setHeader("sessionid", session.sessionId);
		}
	}
	
	private static boolean isValidTimestamp(String timestamp){
		try{
			DateTime now = new DateTime(System.currentTimeMillis(), DateTimeZone.UTC);

			DateTime min = now.minusMillis(30000);
			DateTime max = now.plusMillis(30000);
			
			long ts = Long.valueOf(timestamp);

			DateTime reqestTime = new DateTime(ts, DateTimeZone.UTC);
			if(reqestTime.isAfter(min) || reqestTime.isBefore(max)){
				return true;
			}

		} catch(Exception e){
			e.printStackTrace();
		}
		return false;
	}

}
