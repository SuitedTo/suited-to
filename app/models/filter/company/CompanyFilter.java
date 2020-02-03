package models.filter.company;

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

import enums.Difficulty;

import play.db.jpa.JPA;
import play.utils.Java;

import models.Company;
import models.filter.EntityAttributeFilter;

/**
 * Filter by some Company attribute.
 *
 * @param <T> Attribute type
 * @author joel
 */
public abstract class CompanyFilter<T> extends EntityAttributeFilter<Company, T> {

    /**
     * Play's action invoker gets mad if this method isn't here.
     * <p/>
     * (non-Javadoc)
     *
     * @see logic.questions.filter.EntityAttributeFilter#willAccept(play.db.jpa.Model)
     */
    @Override
    public boolean willAccept(Company company) {
        return super.willAccept(company);
    }

    @Override
    public Class<Company> getEntityClass() {
        return Company.class;
    }
}
