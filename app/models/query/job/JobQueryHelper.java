package models.query.job;

import models.Job;
import play.db.jpa.JPA;
import utils.FilteredCriteriaHelper;

import javax.persistence.criteria.CriteriaBuilder;

public class JobQueryHelper extends FilteredCriteriaHelper {

    CriteriaBuilder builder = JPA.em().getCriteriaBuilder();
    RootTableKey jobKey = addSourceEntity(Job.class);


    public JobQueryHelper() {
        super();
    }

}
