
package models.query.user;

import controllers.Security;
import enums.RoleValue;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import models.Company;
import models.User;
import play.db.jpa.JPA;
import utils.FilteredCriteriaHelper;

/**
 * <p>An otherwise normal 
 * {@link utils.FilteredCriteriaHelper FilteredCriteriaHelper} initially set up
 * with the default logic for returning <em>accessible users</em>, i.e., the
 * {@link models.User User}s that the currently-logged-in user should be able
 * to view.</p>
 */
public class AccessibleUsersHelper extends FilteredCriteriaHelper {

    protected TableKey userKey = addSourceEntity(User.class);
    protected CriteriaBuilder builder = JPA.em().getCriteriaBuilder();
    protected User user = Security.connectedUser();

    public AccessibleUsersHelper() {

        
        if (user == null) {
            //A not-logged-in-user cannot access any users
            addCondition(builder.disjunction());
        }
        else {
            if (user.company == null) {
                
                //A user with APP_ADMIN privileges necessarily does not have a
                //company.  He should be able to see all users, which means just
                //don't restrict the query further
                if (!user.hasRole(RoleValue.APP_ADMIN)) {
                    //A user with a null company who is not APP_ADMIN is a loner
                    //and can't view any users
                    addCondition(builder.disjunction());
                }
            }
            else {
                Company company = user.company;
                Expression entryCompany = field(userKey, "company");
                
                Expression<Boolean> companyNotNull = 
                        builder.isNotNull(entryCompany);
                
                Expression<Boolean> sameCompany = 
                        builder.equal(entryCompany, company);
                
                Expression<Boolean> notLoggedUser =
                        builder.notEqual(getEntity(userKey), user);
                
                addCondition(companyNotNull);
                addCondition(sameCompany);
                addCondition(notLoggedUser);
            }
        }
    }
}
