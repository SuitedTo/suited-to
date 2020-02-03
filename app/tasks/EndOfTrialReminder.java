package tasks;

import enums.RoleValue;
import models.User;
import models.filter.user.ByRole;
import notifiers.Mails;
import play.Play;
import play.db.jpa.JPA;
import scheduler.Schedule;
import scheduler.Task;
import scheduler.TaskExecutionContext;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.Arrays;
import java.util.List;

@Schedule(on="0 0 9 * * ?", allTimeZones=true)
public class EndOfTrialReminder extends Task{

	public EndOfTrialReminder(TaskExecutionContext context) {
		super(context);
	}

	public void doTask(){
        super.doTask();
		//by default, send out an email three days before trial expiration and
		//one day before trial expiration.
		List<String> reminderDays = Arrays.asList(
				Play.configuration.getProperty("endOfTrialReminders", "3,1").split(","));



		CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
		CriteriaQuery<User> cq = cb.createQuery(User.class);
		Root<User> root = cq.from(User.class);
		ByRole companyAdminRole = new ByRole();
		companyAdminRole.include(RoleValue.COMPANY_ADMIN.name());
		List<User> companyAdmins = JPA.em().createQuery(cq.where(companyAdminRole.asPredicate(root))).getResultList();		
		for(User companyAdmin : companyAdmins){
			if(!timeZoneMatch(companyAdmin.timeZone)){
				continue;
			}
			try{
				int daysRemaining = companyAdmin.company.daysRemainingInFreeTrial();
				String descr = (daysRemaining == 1)?"1 day":daysRemaining + " days";
				if(reminderDays.contains(String.valueOf(daysRemaining))){
					Mails.endOfTrialReminder(companyAdmin, descr);
				}
			}catch(IllegalStateException ise){

			}
		}

	}
}
