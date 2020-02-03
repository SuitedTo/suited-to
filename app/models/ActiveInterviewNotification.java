
package models;

import javax.persistence.Entity;

@Entity
public class ActiveInterviewNotification extends Notification {
    public ActiveInterviewNotification(User u, ActiveInterviewWorkflow w) {
        super(u, w);
    }

    /**
     * Default Constructor required for test data creation
     */
    public ActiveInterviewNotification(){}
}
