package models.query.job;

import controllers.Security;
import enums.RoleValue;
import models.User;

public class AccessibleJobHelper extends JobQueryHelper {

    public AccessibleJobHelper() {
        super();

        User user = Security.connectedUser();
        if(user != null) {
            if(user.company != null) {
                addCondition(builder.equal(field(jobKey, "company"), user.company));
            }
            else if(user.hasRole(RoleValue.APP_ADMIN)) {
                addCondition(builder.conjunction());
            }
        }
        else {
            addCondition(builder.disjunction());
        }
    }
}
