package models.query;

import models.ModelBase;

public interface EntityMatchHandler<M extends ModelBase> {

	public void handleMatch(M entity);
	
}
