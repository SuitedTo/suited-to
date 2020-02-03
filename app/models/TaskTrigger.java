package models;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;

@MappedSuperclass
public abstract class TaskTrigger extends ModelBase{
	
	@OneToOne
	public TriggerEntry triggerEntry;
	
	public Date previousExecution;
	
	public boolean reschedule;
	
	/**
	 * Returns the next planned execution time.
	 * @param after
	 * @return The returned list of Calendar object will all have the 
	 * same time. There will be a different Calendar to represent each timezone.
	 */
	public abstract List<Calendar> getNextPlannedExecution(Date after);
}

