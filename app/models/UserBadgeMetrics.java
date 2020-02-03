package models;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.NaturalId;

import javax.persistence.Entity;
import java.util.Map;

/**
 * UserBadgeMetrics
 *
 * @author Mike
 *
 * Summarizes system-wide badge use. Please note that this class differs
 * from questionMetrics and userMetrics in that it represents metrics for all
 * badges of a particular type, NOT a particular badge.
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class UserBadgeMetrics extends ModelBase {

    @NaturalId // Only one badgeMetric should exist per badge
    public String name;
    public int usersEarnedCount;

    /**
     *
     * @param name The name of the badge to update
     *              the metrics of
     */
    public UserBadgeMetrics(String name) {
        super();
        Map<Object, Object> data = (Map<Object, Object>) UserBadge.badgeData.get(name);
        if(data == null || data.isEmpty()) {
            throw new IllegalArgumentException("Could not find badge " + name);
        }

        this.name = name;
        this.updateUsersEarnedCount();
    }

    public void updateUsersEarnedCount() {
        this.usersEarnedCount = User.find("select distinct u " +
                "from User as u " +
                "inner join u.badges as b " +
                "where b.name = :name "+
                "and b.multiplier > 0")
                .setParameter("name", this.name)
                .fetch()
                .size();
    }

    public BadgeMetricInfo getInfo() {
        return new BadgeMetricInfo(this);
    }

    public static class BadgeMetricInfo{

        public final String name;
        public final String title;
        public final String description;
        public final String icon;
        public final int usersEarnedCount;

        public BadgeMetricInfo(UserBadgeMetrics metric) {

            this.name = metric.name;
            this.usersEarnedCount = metric.usersEarnedCount;

            Map<Object, Object> data = (Map<Object, Object>) UserBadge.badgeData.get(name);

            this.icon = (String) data.get("icon");

            Object genDesc = data.get("generalizedDescription");
            if(genDesc != null) {
                this.description = (String) genDesc;
            }
            else {
                this.description = (String) data.get("description");
            }

            Object genTitle = data.get("generalizedTitle");
            if(genTitle != null) {
                this.title = (String) genTitle;
            }
            else {
                this.title = (String) data.get("title");
            }
        }
    }
}
