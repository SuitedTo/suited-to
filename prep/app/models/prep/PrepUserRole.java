package models.prep;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import models.prep.PrepUser.RoleValue;
import modules.ebean.Finder;

import com.google.gson.JsonSerializer;


@Entity
@Table(name = "PrepUser_roles")
public class PrepUserRole extends EbeanModelBase{

	@ManyToOne
	public PrepUser prepUser;
	
	@Column(name="roles")
	@Enumerated(EnumType.STRING)
	public RoleValue role;
	
	
	public static Finder<Long,PrepUserRole> find = new Finder<Long,PrepUserRole>(
            Long.class, PrepUserRole.class
    );
	
	@Override
	public JsonSerializer serializer() {
		// TODO Auto-generated method stub
		return null;
	}

	public PrepUserRole(PrepUser user, RoleValue role) {
		super();
		this.prepUser = user;
		this.role = role;
	}

}
