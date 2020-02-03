package models.filter.user;

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

import models.User;
import models.filter.EntityAttributeFilter;

/**
 * Filter by some User attribute.
 *
 * @param <T> Attribute type
 * @author joel
 */
public abstract class UserFilter<T> extends EntityAttributeFilter<User, T> {

    /**
     * Play's action invoker gets mad if this method isn't here.
     * <p/>
     * (non-Javadoc)
     *
     * @see logic.questions.filter.EntityAttributeFilter#willAccept(play.db.jpa.Model)
     */
    @Override
    public boolean willAccept(User user) {
        return super.willAccept(user);
    }

    @Override
    public Class<User> getEntityClass() {
        return User.class;
    }
}
