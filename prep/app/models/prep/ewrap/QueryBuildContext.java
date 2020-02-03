package models.prep.ewrap;

import models.prep.PrepUser;


/**
 * An immutable context for building a query.
 * 
 * @author joel
 *
 */
public class QueryBuildContext {
	
	private final PrepUser connectedUser;
	
	public QueryBuildContext(PrepUser connectedUser){
		this.connectedUser = connectedUser;
	}
	
	public PrepUser getConnectedUser() {
		return connectedUser;
	}

}
