package models.query.user;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import models.Company;
import models.User;
import controllers.Security;
import enums.RoleValue;

/**
 * Query for user that the given user is allowed to access
 *
 * @author joel
 */
public class AccessibleUsers extends UserQuery {
    private User user;

    public AccessibleUsers() {
        super();
        this.user = Security.connectedUser();
    }

    public AccessibleUsers(User user, Integer iSortCol_0, String sSortDir_0, String sSearch,
                               Integer iDisplayStart, Integer iDisplayLength) {
        super(iSortCol_0, sSortDir_0, sSearch, iDisplayStart, iDisplayLength);

        if (user == null) {
            this.user = Security.connectedUser();
        } else {
            this.user = user;
        }
    }

    @Override
    public Predicate buildCriteria(CriteriaBuilder criteriaBuilder, Root<User> root,
                                   CriteriaQuery<Object> criteriaQuery) {
        
        
        if(user.hasRole(RoleValue.APP_ADMIN)){
    		return criteriaBuilder.conjunction();
    	}
        
        if (user.company != null) {
        	
        	Predicate hasCompany = root.get("company").isNotNull();
        	
            //match the user's company
        	Predicate companyMatch = criteriaBuilder.and(hasCompany,criteriaBuilder.equal(root.<Company>get("company"), user.company));
            
            //don't include the current user
            Predicate criteria = criteriaBuilder.and(companyMatch, criteriaBuilder.notEqual(root, user));
            
            return criteria;
        }else{
        	return criteriaBuilder.disjunction();
        }
        
       
    }

}
