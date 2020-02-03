package enums;

import models.AccountHolder;
import models.Candidate;
import models.Company;
import models.User;

/**
 * <p>This enum encapsulates the logic for all actions the user may want to take
 * that may be allowed or disallowed based on their account type and current
 * account statistics.</p>
 */
public enum AccountLimitedAction {
    CREATE_PRIVATE_QUESTION(AccountResource.PRIVATE_QUESTIONS),
    CREATE_INTERVIEW(AccountResource.INTERVIEWS),
    CREATE_CANDIDATE(AccountResource.CANDIDATES),
    CREATE_ADMINISTRATOR(AccountResource.ADMINISTRATORS), 
    CREATE_USER(AccountResource.USERS);
    
    private AccountResource resourceType;
    
    private AccountLimitedAction(AccountResource resourceType){
		this.resourceType = resourceType;
	}
    
    //canBePerformedBy
    public boolean canPerform(AccountHolder accountHolder){
    	return checkUsage(resourceType, accountHolder);
    }
    
    private static boolean checkUsage(AccountResource resourceType, AccountHolder accountHolder){
    	return accountHolder.getAccountType().checkUsage(resourceType,
        		accountHolder.getResourceUsage(resourceType));
    }
}
