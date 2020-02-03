package models;

import javax.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * This is not a mapped superclass because: https://forum.hibernate.org/viewtopic.php?f=1&t=1003328
 * Please note that workflows are represented in a single table with a discriminator column.
 */

@Entity
public abstract class Workflow extends ModelBase implements Comparable {

    /*Ideally this would have an orphanremoval = true attribute but due to bug:
    https://play.lighthouseapp.com/projects/57987/tickets/1503-nested-onetomany-relationships-with-orphanremoval-fails-to-commit
    we need to omit that attribute in order to allow cascading deletes from the Company level. The bug has been fixed in
    play 1.2.5 so when the app is upgraded to that version we can add orphanremoval = true back to this relationship and
    manage deleting entities by removing from the collection. */
    @OneToMany(mappedBy = "workflow", cascade = CascadeType.ALL)
    public List<Notification> notifications = new ArrayList<Notification>();

    public int compareTo(Object other) {

        if (other == null) {
            return 0;
        }

        return id.compareTo(((Workflow) other).id);
    }

}
