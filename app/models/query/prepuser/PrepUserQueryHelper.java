package models.query.prepuser;

import models.prep.PrepUser;
import play.db.jpa.JPA;
import utils.FilteredCriteriaHelper;

import javax.persistence.criteria.CriteriaBuilder;

/**
 * Created with IntelliJ IDEA.
 * User: phutchinson
 * Date: 3/4/13
 * Time: 10:20 AM
 * To change this template use File | Settings | File Templates.
 */
public class PrepUserQueryHelper extends FilteredCriteriaHelper {
    protected CriteriaBuilder builder;
    protected RootTableKey rootTableKey;

    public PrepUserQueryHelper() {
        super();
        builder = JPA.em().getCriteriaBuilder();
        rootTableKey = addSourceEntity(PrepUser.class);
    }
}
