package tasks;

import controllers.Community;
import models.UserBadge;
import models.UserBadgeMetrics;
import scheduler.Schedule;
import scheduler.Task;
import scheduler.TaskExecutionContext;

import java.util.*;

@Schedule(on="0 0 0/1 * * ?")//Hourly
public class UpdateUserBadgeMetricsTask extends Task {

    public UpdateUserBadgeMetricsTask(TaskExecutionContext context) {
        super(context);
    }

    @Override
    public void doTask() {
        super.doTask();

        /**
         * In lieu of finding a cozy home inside of another entity for a list of
         * UserBadgeMetrics, they are being persisted on their own and cleared out
         * each time the metrics are updated.
         */
        List<UserBadgeMetrics> metrics = UserBadgeMetrics.findAll();

        for(UserBadgeMetrics m : metrics) {
            m.delete();
        }

        Set badgeNames = UserBadge.badgeData.keySet();
        Iterator<String> namesIt = badgeNames.iterator();

        while(namesIt.hasNext()) {
            new UserBadgeMetrics(namesIt.next()).save();
        }

    }
}
