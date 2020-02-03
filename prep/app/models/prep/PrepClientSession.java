package models.prep;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import modules.ebean.Finder;

import com.google.gson.JsonSerializer;


@Entity
@Table(name = "PREP_ClientSession")
public class PrepClientSession extends EbeanModelBase{
	
	@ManyToOne
	public PrepUser user;
	
	@ManyToOne
	public PrepClient client;
	
	@Column(name="session_id")
	public String sessionId;
	
	
	public static Finder<Long,PrepClientSession> find = new Finder<Long,PrepClientSession>(
            Long.class, PrepClientSession.class
    );

	@Override
	public JsonSerializer serializer() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public boolean isValid(){
		
		return user != null;
	}

}
