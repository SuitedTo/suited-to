package enums;

public enum AccountType {

    FREE(0.00, 5L, 1L, 0L, 0L, 1L),
    STANDARD(50.00, 40L, 15L, 25L, null, 5L),
    ENTERPRISE(200.00, 500L, null, null, null, 10L),
    UNLIMITED(0.00, null, null, null, null, null),//for app admins
    INDIVIDUAL(0.00, 1L, 0L, 0L, 0L, 0L);

    public final double price;

    private final Long maxUsers;
    private final Long maxInterviews;
    private final Long maxPrivateQuestions;
    private final Long maxCandidates;
    private final Long maxAdministrators;

    private AccountType(double price, Long maxUsers, Long maxInterviews, 
                Long maxPrivateQuestions, Long maxCandidates, 
                Long maxAdministrators){
        this.price = price;
        this.maxUsers = maxUsers;
        this.maxInterviews = maxInterviews;
        this.maxPrivateQuestions = maxPrivateQuestions;
        this.maxCandidates = maxCandidates;
        this.maxAdministrators = maxAdministrators;
    }


    public boolean requiresPayment(){
        return price > 0;
    }
    
    public boolean checkUsage(AccountResource resourceType, long currentUsage){
    	Long max = getMax(resourceType);
    	return (max == null) || (currentUsage < max);
    }
    
    /**
     * <p>Returns <code>true</code> <strong>iff</strong> the account has a
     * non-zero resource max.  This includes an infinite permissible amount,
     * as when {@link #getMax(enums.AccountResource) getMax()} would return
     * <code>null</code>.</p>
     * 
     * @param resourceType The resource type to check.
     * @return <code>true</code> if the account has a non-zero max.
     */
    public boolean hasPositiveMax(AccountResource resourceType) {
        Long max = getMax(resourceType);
        
        return (max == null || max > 0);
    }
    
    public Long getMax(AccountResource resourceType){
    	switch(resourceType){
    	case PRIVATE_QUESTIONS:
    		return maxPrivateQuestions;
    	case USERS:
    		return maxUsers;
    	case INTERVIEWS:
    		return maxInterviews;
    	case CANDIDATES:
    		return maxCandidates;
    	case ADMINISTRATORS:
    		return maxAdministrators;
    	default:
			throw new IllegalArgumentException("Unknown account resource");
    	}
    }
    
    public static AccountType getInstanceByName(String name) {
        AccountType result;
        
        name = name.toUpperCase();
        
        if (name.equals("FREE")) {
            result = FREE;
        }
        else if (name.equals("STANDARD")) {
            result = STANDARD;
        }
        else if (name.equals("ENTERPRISE")) {
            result = ENTERPRISE;
        }
        else if (name.equals("INDIVIDUAL")) {
            result = INDIVIDUAL;
        }
        else {
            throw new IllegalArgumentException("No AccountType by that name.");
        }
        
        return result;
    }
}
