package models.filter.feedback;

import models.Feedback;
import models.filter.EntityAttributeFilter;

public abstract class FeedbackFilter<T> extends EntityAttributeFilter<Feedback, T> {


    @Override
    public Class<Feedback> getEntityClass() {
        return Feedback.class;
    }
}
