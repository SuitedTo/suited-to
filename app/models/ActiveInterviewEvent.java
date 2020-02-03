/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package models;

import com.google.gson.JsonElement;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Inheritance(strategy=InheritanceType.JOINED)
public abstract class ActiveInterviewEvent extends ModelBase {

    @ManyToOne
    public ActiveInterview activeInterview;
    
    @ManyToOne
    public User initiatingUser;

    @OneToMany(mappedBy = "event", cascade = CascadeType.REMOVE)
    public List<ActiveInterviewWorkflow> workflows = new ArrayList<ActiveInterviewWorkflow>();
    
    public ActiveInterviewEvent(ActiveInterview activeInterview, 
                User initiatingUser) {
        this.activeInterview = activeInterview;
        this.initiatingUser = initiatingUser;
    }

    /**
     * Default Constructor required for test data creation
     */
    public ActiveInterviewEvent() {}

    public abstract JsonElement serialize();
}
