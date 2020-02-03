/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package models;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import enums.ActiveInterviewState;
import play.i18n.Messages;

import javax.persistence.*;
import java.util.Date;

@Entity
public class ActiveInterviewStateChange extends ActiveInterviewEvent {
    /**
     * The time in milliseconds from the epoch when this record was created (not guaranteed to be consistent with created)
     * field
     * todo: eventually we probably want creationTimestamp to be removed and change created field on model base to be
     * the actual timestamp like this is.
     */
    public Long creationTimestamp;
    
    @Enumerated(EnumType.STRING)
    public ActiveInterviewState toState;
    
    public ActiveInterviewStateChange(ActiveInterview interview, 
            User initiatingUser, ActiveInterviewState toState) {
        super(interview, initiatingUser);
        
        this.toState = toState;
    }

    /**
     * Default Constructor required for test data creation
     */
    public ActiveInterviewStateChange() {}

    @Override
    public JsonElement serialize() {
        return new JsonPrimitive(Messages.get("" + toState));
    }
    
    @Override
    public String toString() {
        return "Change state to: " + Messages.get("" + toState);
    }
    
    public void createNotification() {
        removeNotifications();
        
        ActiveInterviewWorkflow w = new ActiveInterviewWorkflow(this);
        w.save();

        ActiveInterviewNotification n = 
                new ActiveInterviewNotification(initiatingUser, w);
        n.save();

        initiatingUser.notifications.add(n);
        initiatingUser.save();
    }
    
    public void removeNotifications() {
        ActiveInterviewWorkflow w = ActiveInterviewWorkflow.find(
                        "byEvent", this).first();

         if (w != null) {
            for (Notification notification : w.notifications) {
                notification.delete();
            }
        }
    }

    @PrePersist
    protected void createTimestamp() {
        creationTimestamp = new Date().getTime();
    }
}
