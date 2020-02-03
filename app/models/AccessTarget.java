package models;

import enums.AccessScope;

public interface AccessTarget {

	public boolean canAccess(User user, AccessScope scope);
}
