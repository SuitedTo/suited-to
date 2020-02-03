package models.query.interview;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import controllers.Security;
import enums.RoleValue;
import models.ActiveInterview;

import models.Company;
import models.Interview;
import models.User;

/**
 * All interviews that are available to the connected user.
 *
 * @author joel
 */
public class AccessibleInterviews extends InterviewQuery {

    private User user;

    public AccessibleInterviews() {
        super();
        this.user = Security.connectedUser();
    }

    public AccessibleInterviews(User user, Integer iSortCol_0, String sSortDir_0, String sSearch,
                                Integer iDisplayStart, Integer iDisplayLength) {
        super(iSortCol_0, sSortDir_0, sSearch, iDisplayStart, iDisplayLength);

        if (user == null) {
            this.user = Security.connectedUser();
        } else {
            this.user = user;
        }
    }

    @Override
    public Predicate buildCriteria(CriteriaBuilder criteriaBuilder,
                                   Root<Interview> root, CriteriaQuery<Object> criteriaQuery) {

        Path<Boolean> active = root.get("active");
            
        Predicate isActive = criteriaBuilder.isTrue(active);
        
        Subquery<ActiveInterview> activeInterviews = 
                criteriaQuery.subquery(ActiveInterview.class);
        Root<ActiveInterview> activeInterviewRoot = 
                activeInterviews.from(ActiveInterview.class);
        activeInterviews.select(activeInterviewRoot);
        isActive = criteriaBuilder.and(isActive,
                criteriaBuilder.not(
                    criteriaBuilder.in(root).value(activeInterviews)));
        
        if (user.hasRole(RoleValue.APP_ADMIN)) {
            return isActive; //criteriaBuilder.conjunction();
        }

        final Path<User> creator = root.get("user");

        Company connectedUserCompany = user.company;
        if (connectedUserCompany != null) {
            Subquery<User> companyUsersSubquery = criteriaQuery.subquery(User.class);
            Root fromUser = companyUsersSubquery.from(User.class);
            companyUsersSubquery.select(fromUser).where(criteriaBuilder.equal(fromUser.get("company"), connectedUserCompany));

            Predicate sameCompany = criteriaBuilder.in(creator).value(companyUsersSubquery);    
            
            //(interview.user in user.coworkers)
            return criteriaBuilder.and(sameCompany, isActive);
        } else {
            return criteriaBuilder.disjunction();
        }
    }

}
