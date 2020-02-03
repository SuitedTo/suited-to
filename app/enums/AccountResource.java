package enums;

public enum AccountResource {

	PRIVATE_QUESTIONS("private questions"),
	USERS("users"),
	INTERVIEWS("interviews"),
	QUESTIONS("questions"),
	CANDIDATES("candidates"),
	ADMINISTRATORS("administrators");
	
	private String prettyName;
	
	private AccountResource(String prettyName){
		this.prettyName = prettyName;
	}
	
	public String toString(){
		return prettyName;
	}
}
