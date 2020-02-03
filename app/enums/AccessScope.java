/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package enums;

import models.Company;
import models.User;
import org.apache.commons.lang.ObjectUtils;

/**
 * <p><code>AccessScope</code> provides a semantic way of defining which users
 * have access to a resource.  What is meant by "access" and how such contextual
 * ideas as "the company under consideration" or "the user under consideration" 
 * are defined is situationally-determined.  For example, when determining 
 * whether or not the "name" field of a {@link models.User User} can be viewed, 
 * "access" means "access to view", "the company" is the user's company and "the
 * user" is the user herself.</p>
 */
public enum AccessScope {
    /**
     * <p>Indicates that anyone, even a non-signed-in-user, has access to the
     * given resource.</p>
     */
    PUBLIC, 
    /**
     * <p>Indicates that anyone, so long as they are a registered user, has
     * access to the given resource.</p>
     */
    ALL_USERS,
    /**
     * <p>Indicates that anyone belonging to the same company, as well as 
     * SuitedTo admins, have access to the given resource.  What company is under
     * consideration differs by context.</p>
     */
    COMPANY_AND_APP_ADMIN,
    /**
     * <p>Indicates that the user, anyone that is an admin for the company, and
     * SuitedTo admins, have access to the given resource.  What user and company
     * are under consideration differs by context.</p>
     */
    USER_AND_COMPANY_ADMIN_AND_APP_ADMIN,
    /**
     * <p>Indicates that anyone that is an admin for the company, as well as
     * SuitedTo admins, have access to the given resource.  What company is under
     * consideration differs by context.</p>
     */
    COMPANY_ADMIN_AND_APP_ADMIN,
    /**
     * <p>Indicates that the user and SuitedTo admin have access to the given
     * resource.  What user is under consideration differs by context.</p>
     */
    USER_AND_APP_ADMIN,
    /**
     * <p>Indicates that SuitedTo admin alone has access to the given resource.
     * </p>
     */
    APP_ADMIN;
    
}
