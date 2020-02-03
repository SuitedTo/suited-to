package models;

import models.Workflow;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import play.data.validation.Required;

import javax.persistence.Entity;

@Entity
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)

public class EEOCQuizWorkflow extends Workflow {

    @Required
    boolean pass;

    public EEOCQuizWorkflow(boolean userPassed) {
        pass = userPassed;
    }

}
