package models;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import play.data.validation.Required;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public abstract class Notification extends ModelBase {

    @Required
    @ManyToOne
    public Workflow workflow;

    @Required
    @ManyToOne
    public User user;

    /**
     * Default Constructor required for test data creation
     */
    public Notification(){}

    public Notification(User user, Workflow workflow){
        this.user = user;
        this.workflow = workflow;
    }
}
