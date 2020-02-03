package models;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import javax.persistence.Entity;

import play.Logger;
import play.Play;
import play.libs.Time.CronExpression;
import scheduler.TaskScheduler;

@Entity
public class CronTrigger extends TaskTrigger{

	public  boolean allTimeZones;
	
	public String cronExpression;
	
	public String timeZone;
	
	

	public CronTrigger(String cronExpression, String timeZone, boolean allTimeZones, boolean reschedule) {
		super();
		
		if (cronExpression.startsWith("cron.")) {
			this.cronExpression = Play.configuration.getProperty(cronExpression);
        }else{
        	this.cronExpression = cronExpression;
        }
		
		try {
			new CronExpression(this.cronExpression);
		} catch (ParseException e) {
			Logger.error("Invalid cron expression %s", cronExpression);
			throw new IllegalArgumentException("Invalid cron expression [" + cronExpression + "]");
		}
		
		this.allTimeZones = allTimeZones;
		this.reschedule = reschedule;
	}
	
	public CronTrigger(String cronExpression, String timeZone, boolean allTimeZones) {
		this(cronExpression, timeZone, allTimeZones, true);
	}
	
	public CronTrigger(String cronExpression, String timeZone){
		this(cronExpression, timeZone, false, true);
	}
	
	public CronTrigger(String cronExpression){
		this(cronExpression, null, false, true);
	}

        /**
         * <p>Returns a <code>CronTrigger</code> that will execute its job
         * <em>once</em>, as soon as possible.</p>
         * 
         * @return The <code>CronTrigger</code>.
         */
        public static CronTrigger getASAPTrigger() {
            return new CronTrigger("* * * * * ?", null, false, false);
        }
        
	@Override
	public final List<Calendar> getNextPlannedExecution(Date after) {
		Calendar c = new GregorianCalendar();
		if(after != null){
			c.setTime(after);
		}

		List<Calendar> results = new ArrayList<Calendar>();
		List<String> zones = new ArrayList<String>();

		if(allTimeZones){
			zones = TaskScheduler.supportedTimeZones;
			
			if(zones.size() == 0){
				zones.add(Play.configuration.getProperty("default.timezone"));
			}
		}else{
			if(timeZone != null){
				zones.add(timeZone);
			}else{
				zones.add(Play.configuration.getProperty("default.timezone"));
			}
		}
		
		CronExpression ce;
		try {
			ce = new CronExpression(cronExpression);
		} catch (ParseException e) {
			Logger.error("Invalid cron expression %s", cronExpression);
			return null;
		}
		Calendar soonest = new GregorianCalendar();
		soonest.setTime(new Date(Long.MAX_VALUE));
		for(String zoneStr : zones){

			c.setTimeZone(TimeZone.getTimeZone(zoneStr));

			Date nextDate = ce.getNextValidTimeAfter(c.getTime());
			if (nextDate == null) {
				return null;
			}
			if (nextDate.equals(ce.getNextValidTimeAfter(nextDate))) {
				// Bug #13: avoid running the job twice for the same time
				// (happens when we end up running the job a few minutes before the planned time)
				Date nextInvalid = ce.getNextInvalidTimeAfter(nextDate);
				nextDate = ce.getNextValidTimeAfter(nextInvalid);
			}
			if(soonest.getTime().after(nextDate)){
				soonest.setTime(nextDate);
				soonest.setTimeZone(TimeZone.getTimeZone(zoneStr));
			}
			
			results.add(soonest);
			
			soonest = new GregorianCalendar();
			soonest.setTime(nextDate);
			soonest.setTimeZone(TimeZone.getTimeZone(zoneStr));
		}
		
		Iterator<Calendar> it = results.iterator();
		while(it.hasNext()){
			Calendar result = it.next();
			if(result.getTime().getTime() != soonest.getTime().getTime()){
				it.remove();
			}
		}
		return results;
	}
}
