package models.query.prepcategory;

import models.prep.PrepCategory;
import play.db.jpa.JPA;
import utils.FilteredCriteriaHelper;

import javax.persistence.criteria.CriteriaBuilder;

/**
 * Created with IntelliJ IDEA.
 * User: swilly
 * Date: 2013/2/13
 * Time: 13:29
 * To change this template use File | Settings | File Templates.
 */
public class PrepCategoryQueryHelper extends FilteredCriteriaHelper {
    public PrepCategoryQueryHelper() {
        CriteriaBuilder builder = JPA.em().getCriteriaBuilder();

        TableKey companyKey = addSourceEntity(PrepCategory.class);
    }
}
