package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import enums.AlertType;

@Entity
public class Alert extends ModelBase{
	
	@ManyToOne
	public User user;

	public AlertType type;
	
	@Temporal(TemporalType.TIMESTAMP)
	public Date acknowledged;
	
	public String message;

	public Alert(User user, AlertType type, String message) {
		super();
		this.user = user;
		this.type = type;
		this.message = message;
	}
	
}
