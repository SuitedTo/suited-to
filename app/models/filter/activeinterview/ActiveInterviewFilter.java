package models.filter.activeinterview;

import models.ActiveInterview;
import models.filter.EntityAttributeFilter;

public abstract class ActiveInterviewFilter<T> extends EntityAttributeFilter<ActiveInterview, T> {

    @Override
    public boolean willAccept(ActiveInterview interview) {
        return super.willAccept(interview);
    }

    @Override
    public Class<ActiveInterview> getEntityClass() {
        return ActiveInterview.class;
    }
}
