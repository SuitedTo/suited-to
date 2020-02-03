package models.query.prepjob;

import controllers.Security;
import enums.RoleValue;
import models.User;
import models.prep.PrepJob;
import play.db.jpa.JPA;
import utils.FilteredCriteriaHelper;

import javax.persistence.criteria.CriteriaBuilder;

public class PrepJobQueryHelper extends FilteredCriteriaHelper {

    CriteriaBuilder builder = JPA.em().getCriteriaBuilder();
    RootTableKey prepJobKey;

    public PrepJobQueryHelper() {
        super();
        prepJobKey = addSourceEntity(PrepJob.class);
    }

}
