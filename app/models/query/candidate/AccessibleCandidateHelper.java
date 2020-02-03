package models.query.candidate;

import controllers.Security;
import enums.RoleValue;
import models.User;

/**
 * Created with IntelliJ IDEA.
 * User: perryspyropoulos
 * Date: 8/13/13
 * Time: 11:37 AM
 * To change this template use File | Settings | File Templates.
 */
public class AccessibleCandidateHelper extends CandidateQueryHelper {

    public AccessibleCandidateHelper() {

        super();

        User user = Security.connectedUser();

        if(user != null) {
            if(user.company != null) {
                addCondition(builder.equal(field(candidateKey, "company"), user.company));
            } else if(user.hasRole(RoleValue.APP_ADMIN)) {
                addCondition(builder.conjunction());
            }
        } else {
            addCondition(builder.disjunction());
        }

    }

}
