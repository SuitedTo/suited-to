package models.filter.interview;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;


import play.db.jpa.JPA;
import play.utils.Java;

import models.Interview;
import models.filter.EntityAttributeFilter;

/**
 * Filter by some Interview attribute.
 *
 * @param <T> Attribute type
 * @author joel
 */
public abstract class InterviewFilter<T> extends EntityAttributeFilter<Interview, T> {

    @Override
    public boolean willAccept(Interview interview) {
        return super.willAccept(interview);
    }

    @Override
    public Class<Interview> getEntityClass() {
        return Interview.class;
    }
}
