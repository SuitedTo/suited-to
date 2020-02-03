package models.query.prepcoupon;

import models.prep.PrepCoupon;
import play.db.jpa.JPA;
import utils.FilteredCriteriaHelper;

import javax.persistence.criteria.CriteriaBuilder;

public class PrepCouponQueryHelper extends FilteredCriteriaHelper {

    CriteriaBuilder builder = JPA.em().getCriteriaBuilder();
    RootTableKey prepCouponKey;

    public PrepCouponQueryHelper() {
        super();
        prepCouponKey = addSourceEntity(PrepCoupon.class);
    }

}
