package models;

import enums.AccountResource;
import enums.AccountType;

public interface AccountHolder {

	public AccountType getAccountType();
	
	public long getResourceUsage(AccountResource resource);
	
}
