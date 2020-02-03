package models;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.Entity;

/**
 * Created with IntelliJ IDEA.
 * User: swilly
 * Date: 2012/11/30
 * Time: 13:42
 * To change this template use File | Settings | File Templates.
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class NearingReviewerNotification extends Notification {
    public NearingReviewerNotification(User user) {
        super(user, null);
    }
}