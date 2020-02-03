/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.query.user;

import enums.RoleValue;
import enums.UserStatus;
import javax.persistence.criteria.*;
import models.Company;
import models.User;

/**
 *
 * @author hamptos
 */
public class ActiveOrPendingUsers extends UserQuery {
    
    private final Company myCompany;
    
    public ActiveOrPendingUsers(Company c, Integer iSortCol_0, 
                String sSortDir_0, String sSearch, Integer iDisplayStart, 
                Integer iDisplayLength) {
        super(iSortCol_0, sSortDir_0, sSearch, iDisplayStart, iDisplayLength);
        
        myCompany = c;
    }

    @Override
    public Predicate buildCriteria(CriteriaBuilder criteriaBuilder, Root<User> root, CriteriaQuery<Object> criteriaQuery) {
        Predicate hasCompany = root.get("company").isNotNull();
        	
        Predicate companyMatch = criteriaBuilder.and(hasCompany, 
                criteriaBuilder.equal(root.<Company>get("company"), myCompany));
        
        Path<UserStatus> status = root.get("status");
        
        Predicate isActiveOrPending = criteriaBuilder.and(companyMatch,
                   criteriaBuilder.or(
                        criteriaBuilder.equal(status, UserStatus.ACTIVE),
                        criteriaBuilder.equal(status, UserStatus.PENDING)));
        
        return isActiveOrPending;
    }
    
}
