package models;

import javax.persistence.Entity;
import models.Workflow;

@Entity
public class EEOCQuizResultNotification extends Notification {
    public EEOCQuizResultNotification(User u, EEOCQuizWorkflow w) {
        super(u, w);
    }
}
