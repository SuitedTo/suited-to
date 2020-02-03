package models.query.activeinterview;

import enums.RoleValue;
import models.ActiveInterview;
import models.Company;
import models.User;
import models.filter.activeinterview.ByActive;
import play.db.jpa.JPA;
import utils.FilteredCriteriaHelper;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;

import controllers.Security;

public class AccessibleActiveInterviewsHelper extends FilteredCriteriaHelper {

    private TableKey activeInterviewKey = addSourceEntity(ActiveInterview.class);
    private CriteriaBuilder builder = JPA.em().getCriteriaBuilder();

    public AccessibleActiveInterviewsHelper() {
    	this(Security.connectedUser());
    }
    public AccessibleActiveInterviewsHelper(User user) {
        if (user == null) {
            //A null user cannot acces any interviews
            addCondition(builder.disjunction());
        } else {

            // filter out inactive interviews
            filterInactive();

            if (user.company == null) {

                //A user with APP_ADMIN priveledges necessarily does not have a
                //company.  He should be able to see all ActiveInterviews, which means just
                //don't restrict the query further
                if (!user.hasRole(RoleValue.APP_ADMIN)) {
                    //A user with a null company who is not APP_ADMIN is a loner
                    //and can't view any ActiveInterviews
                    addCondition(builder.disjunction());
                }
            } else {
                Company company = user.company;
                Expression entryCompany = field(activeInterviewKey, "company");
                addCondition(builder.isNotNull(entryCompany));
                addCondition(builder.equal(entryCompany, company));
            }
        }
    }

    /**
     * Helper method to create a filter to eliminate interviews where "active" = false
     */
    private void filterInactive() {
        ByActive byActiveFilter = new ByActive();
        byActiveFilter.include(Boolean.TRUE.toString());
        addFilter(byActiveFilter);
    }
}
