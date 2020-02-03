
package jobs;

import enums.ActiveInterviewState;
import java.util.*;
import models.ActiveInterview;
import models.ActiveInterviewStateChange;
import models.Interview;
import notifiers.Mails;
import play.Logger;
import play.jobs.Every;
import play.jobs.Job;
import play.mvc.Router;

/**
 * <p>Seeks out active interviews that were started but never finished, or 
 * finished but without feedback, and sends reminders to either stop the 
 * interview or provide feedback.</p>
 */
@Every("15min")
public class DelinquentInterviewJob extends Job {

	public static final int MINUTES_AFTER_INTERVIEW = 120;

	@Override
	public void doJob() throws Exception {

		new MultiNodeRunnable("DelinquentInterviewJob", new Runnable(){
			public void run(){
				Logger.debug("Sending notifications for delinquent interviews...");

				//TODO : I've got everything loadind lazily inside ActiveInterview, but
				//       still there's got to be a better way of doing this than 
				//       iterating over every ActiveInterview.  JPQL makes the necessary
				//       logic a little tricky, however, with its restrictions on
				//       subqueries and joins
				List<ActiveInterview> interviews = 
						ActiveInterview.find("byFeedbackReminderSent", false).fetch();

				for (ActiveInterview interview : interviews) {
					ActiveInterviewStateChange mostRecentStateChange = 
							interview.getMostRecentStatusChange();

					if(mostRecentStateChange != null){

						int offset = interview.getDuration() + MINUTES_AFTER_INTERVIEW;

						Calendar calendar = Calendar.getInstance();
						calendar.add(Calendar.MINUTE, -1 * offset);
						Date threshold = calendar.getTime();

						if (mostRecentStateChange.created.before(threshold)) {
							if (mostRecentStateChange.toState.equals(
									ActiveInterviewState.STARTED) && interview.interviewer.interviewStopReminder) {
								//Need a reminder to stop and provide feedback
								pesterToStop(interview);
							}
							else if (mostRecentStateChange.toState.equals(
									ActiveInterviewState.FINISHED) && 
									!interview.hasFeedbackSince(
											mostRecentStateChange.created) && interview.interviewer.feedbackRequestedReminder) {

								//Need a reminder to provide feedback
								pesterForFeedback(interview);
							}
						}
					}
				}
			}
		}).run();
	}

	private void pesterToStop(ActiveInterview i) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("id", i.id);
		params.put("fresh", Boolean.TRUE);

		String subject = "Your interview with " + i.interviewee.name;

		String url = Router.getFullUrl("Interviews.show", params);

		pester(i, subject, "It's been a while since your interview, " + i.name + 
				", with " + i.interviewee.name + " started.  Visit " + url + 
				"to stop it manually, or provide some feedback to stop it " +
				"automatically.");
	}

	private void pesterForFeedback(ActiveInterview i) {

		String subject = "Feedback on " + i.interviewee.name;

		pester(i, subject, "You recently completed your interview, " + i.name + 
				", for " + i.interviewee.name + ".  You can add your " +
				"feedback to SuitedTo right from this email!");
	}

	private void pester(ActiveInterview i, String subject, String message) {
		Mails.getFeedback(i.user.email, i.interviewee, i.getMagicID(),
				subject, message);

		i.feedbackReminderSent = true;
		i.save();
	}
}
