package dto.prep;

import models.prep.PrepCoupon;

import java.util.Date;

public class PrepCouponDTO extends PrepDTO{
    public Long id;
    public Integer discount;
    public String name;
    public Integer maxUses;
    public Integer payPeriods;

    public static PrepCouponDTO fromPrepCoupon(PrepCoupon prepCoupon) {
        if (prepCoupon == null) {
            return null;
        }
        PrepCouponDTO d = new PrepCouponDTO();
        d.id = prepCoupon.id;
        d.discount = prepCoupon.discount;
        d.name = prepCoupon.name;
        d.maxUses = prepCoupon.maxUses;
        d.payPeriods = prepCoupon.payPeriods;

        return d;
    }
}
