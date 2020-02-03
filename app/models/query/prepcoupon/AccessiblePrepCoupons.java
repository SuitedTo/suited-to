package models.query.prepcoupon;

import controllers.Security;
import enums.RoleValue;
import models.User;

public class AccessiblePrepCoupons extends PrepCouponQueryHelper {

    public AccessiblePrepCoupons() {
        super();

        User user = Security.connectedUser();
        // Restrict access to
        if (user != null && user.hasRole(RoleValue.APP_ADMIN)) {
            addCondition(builder.conjunction());
        } else {
            addCondition(builder.disjunction());
        }
    }
}
