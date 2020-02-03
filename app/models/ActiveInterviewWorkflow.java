/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package models;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 * <p>Ok, this class exists to map between a couple different ways of doing 
 * things.  ActiveInterviews use a generic event system to record their history
 * that is less efficient but more flexible.  Because it is inheritance-heavy,
 * reusing the existing workflow system (which uses the single-table inheritance
 * storage method) isn't really an option.  This class implements the adapter
 * patter, bridging from Workflow space into Event space.  One use for this (and
 * the use that triggered creating this class) is associating notifications with
 * active interview events.</p>
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class ActiveInterviewWorkflow extends Workflow {

    @ManyToOne
    public ActiveInterviewEvent event;
    
    public ActiveInterviewWorkflow(ActiveInterviewEvent event) {
        this.event = event;
    }

    /**
     * Default Constructor required for test data creation
     */
    public ActiveInterviewWorkflow() {}
}
