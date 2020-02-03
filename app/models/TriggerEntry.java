package models;

import javax.persistence.Entity;

@Entity
public class TriggerEntry extends ModelBase{

	public String triggerKey;
	
	public String taskClass;
	
	public String taskArgs;
	
	public boolean scheduled;
}
