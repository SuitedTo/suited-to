package models.prep;

import static javax.persistence.CascadeType.REMOVE;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import modules.ebean.Finder;

import com.google.gson.JsonSerializer;

@Entity
@Table(name = "PREP_Client")
public class PrepClient extends EbeanModelBase{

	@Column(name="client_id")
	public String clientId;
	
	@Column(name="client_secret")
	public String clientSecret;
	
	@OneToMany(mappedBy="client", cascade = REMOVE)
	private List<PrepClientSession> sessions;
	
	public static Finder<Long,PrepClient> find = new Finder<Long,PrepClient>(
            Long.class, PrepClient.class
    );

	@Override
	public JsonSerializer serializer() {
		// TODO Auto-generated method stub
		return null;
	}
}
