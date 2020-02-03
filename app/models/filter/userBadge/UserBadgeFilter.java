package models.filter.userBadge;

import models.UserBadge;
import models.filter.EntityAttributeFilter;

public abstract class UserBadgeFilter<T> extends EntityAttributeFilter<UserBadge, T>{

	@Override
	public Class<UserBadge> getEntityClass() {
		return UserBadge.class;
	}
}
