package tasks;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import models.ActiveInterview;
import models.User;
import notifiers.Mails;
import play.Logger;
import play.db.jpa.JPA;
import scheduler.Schedule;
import scheduler.Task;
import scheduler.TaskExecutionContext;

@Schedule(on="cron.InterviewReminder", allTimeZones=true)
public class InterviewReminder extends Task{


	public InterviewReminder(TaskExecutionContext context) {
		super(context);
	}

	@Override
	public void doTask() {
        super.doTask();
		Map<User, List<ActiveInterview>> userInterviews = new Hashtable<User, List<ActiveInterview>>();		
		
		Calendar startOfToday = new GregorianCalendar();
		startOfToday.set(Calendar.HOUR_OF_DAY, 0);
		startOfToday.set(Calendar.MINUTE, 0);
		startOfToday.set(Calendar.SECOND, 0);
		startOfToday.set(Calendar.MILLISECOND, 0);
		
        Calendar startOfTommorrow = new GregorianCalendar();
        startOfTommorrow.set(Calendar.HOUR_OF_DAY, 0);
        startOfTommorrow.set(Calendar.MINUTE, 0);
        startOfTommorrow.set(Calendar.SECOND, 0);
        startOfTommorrow.set(Calendar.MILLISECOND, 0);
        startOfTommorrow.add(Calendar.DAY_OF_MONTH, 1);

		Query query = JPA.em().createQuery("select ci from ActiveInterview ci where ci.active is true and ci.anticipatedDate >= :start and ci.anticipatedDate < :end");
        query.setParameter("start", startOfToday.getTime());
        query.setParameter("end", startOfTommorrow.getTime());
		List<ActiveInterview> ciList = query.getResultList();
		Logger.debug("InterviewReminder found " + ciList.size() + " total active interviews for today.");
		for(ActiveInterview ci : ciList){
			User recipient = ci.user;
			if(timeZoneMatch(recipient.timeZone)){
				try{
					List<ActiveInterview> interviews = userInterviews.get(recipient);
					if(interviews == null){
						interviews = new ArrayList<ActiveInterview>();						
					}
					interviews.add(ci);
					userInterviews.put(recipient, interviews);
				}catch(Exception e){
					Logger.error(e, "Unable to send reminder for interview [%d]", ci.id);
				}
			}
		}

		Iterator<User> it = userInterviews.keySet().iterator();
		while(it.hasNext()){
			User user = it.next();
			List<ActiveInterview> interviews = userInterviews.get(user);
			Mails.interviewReminder(user, interviews);
		}
	}
}
