package models.query.prepjob;

import controllers.Security;
import enums.RoleValue;
import models.User;
import models.tables.prepjob.PrepJobTable;

public class AccessiblePrepJobHelper extends PrepJobQueryHelper {

    public AccessiblePrepJobHelper() {
        super();

        User user = Security.connectedUser();
        // Restrict access to
        if(user != null && user.hasRole(RoleValue.APP_ADMIN)) {
            addCondition(builder.conjunction());
        }
        else {
            addCondition(builder.disjunction());
        }
    }
}
