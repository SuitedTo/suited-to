package models.filter.category;

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

import models.Category;
import models.filter.EntityAttributeFilter;

/**
 * Filter by some Category attribute.
 *
 * @param <T> Attribute type
 * @author joel
 */
public abstract class CategoryFilter<T> extends EntityAttributeFilter<Category, T> {


    @Override
    public Class<Category> getEntityClass() {
        return Category.class;
    }
}
