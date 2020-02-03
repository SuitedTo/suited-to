package session.prep;

import java.lang.reflect.Method;

import models.prep.PrepUser;
import play.PlayPlugin;
import play.mvc.Http;
import play.mvc.Scope;
import play.mvc.results.Result;


public class SessionManager extends PlayPlugin{
	
	protected PrepUser owner;
	
	public static ThreadLocal<SessionManager> current = new ThreadLocal<SessionManager>();
	
	public static SessionManager current(){
		return current.get();
	}
	
	public final PrepUser getSessionOwner(){
		if(owner == null){
			Long id = getSessionOwnerId();
			if(id == null){
				return null;
			}
			owner = PrepUser.find.byId(id);
		}
		return owner;
	}
	
	public Long getSessionOwnerId(){ return null; }
	
	public void createSession(PrepUser owner){}
	
	public void restoreSession(){}
	
	public void clearSession(){}
	
	public void saveSession(){}
	
	
	public void beforeActionInvocation(Method actionMethod){
		Http.Cookie standardSessionCookie = Http.Request.current().cookies.get(Scope.COOKIE_PREFIX + "_SESSION");
		if(standardSessionCookie == null){
			current.set(new ClientSessionManager());
		} else {
			current.set(new DefaultSessionManager());
		}
		current().restoreSession();
	}
	
	
	@Override
	public void onActionInvocationResult(Result result){
		SessionManager sm = current();
		if(sm != null){
			sm.saveSession();
		}
	}
}
