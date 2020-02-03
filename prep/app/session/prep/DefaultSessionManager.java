package session.prep;

import models.prep.PrepUser;
import errors.prep.PrepHttpError;
import errors.prep.PrepHttpErrorResult;
import play.mvc.Http;
import play.mvc.Scope;

public class DefaultSessionManager extends SessionManager{
	
	private static final String SESSION_USER_IDENTIFIER = "prepUserId";

	@Override
	public Long getSessionOwnerId() {
		Scope.Session session = Scope.Session.current();
        String idStr = session != null ? session.get(SESSION_USER_IDENTIFIER) : null;
        if(idStr != null){
        	return Long.valueOf(idStr);
        }
        return null;
	}

	@Override
	public void createSession(PrepUser owner) {
		if(owner != null){
			Scope.Session session = Scope.Session.current();
        	session.put(SESSION_USER_IDENTIFIER, owner.id);
		}
	}

	@Override
	public void restoreSession() {
		//Play handles this
	}

	@Override
	public void clearSession() {
		Scope.Session session = Scope.Session.current();
        session.remove(SESSION_USER_IDENTIFIER);
	}

	@Override
	public void saveSession() {
		//Play handles this
	}

}
