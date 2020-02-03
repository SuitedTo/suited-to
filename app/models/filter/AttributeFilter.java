package models.filter;

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
import play.db.jpa.Model;
import play.utils.Java;

/**
 * Filter for any type of attribute of any type of object.
 * <p/>
 * * By default, all Questions are accepted. By specifying inclusion criteria, the set of accepted questions
 * will be limited to the ones that match the inclusion criteria. Using exclusion criteria, some of these
 * questions can be rejected.
 * <p/>
 * Creating one filter instance with multiple exclusions will be functionally equivalent
 * to creating a filter instance for every exclusion. The same cannot be said for inclusions;
 * if you want to use inclusions then you need to pack them all into one filter.
 *
 * @param <O> Object type
 * @param <T> Attribute type
 * @author joel
 */
public abstract class AttributeFilter<O, T> {
    protected List<T> exclude;
    protected List<T> include;

    public AttributeFilter() {
        exclude = new ArrayList<T>();
        include = new ArrayList<T>();
    }

    public abstract String getAttributeName();

    protected abstract String toString(T attribute);

    public abstract T fromString(String attributeStr);

    public abstract boolean willAccept(O object);

    /**
     * Clear all includes and excludes
     */
    public void clear() {
        include.clear();
        exclude.clear();
    }

    /**
     * Add an exclusion to the list. Duplicates will be ignored.
     *
     * @param exclusionCriterion
     */
    public final void exclude(String exclusionCriterion) {
        if (exclusionCriterion.charAt(0) == '$') {
            List<String> criteria = interpret(exclusionCriterion.substring(1));
            for (String c : criteria) {
                exclude(c);
            }
        } else {
            List<String> excludes = getExcludeStrings();
            if ((exclusionCriterion != null) && (!excludes.contains(exclusionCriterion))) {
                addExcludeString(exclusionCriterion);
            }
        }
    }

    /**
     * Add an inclusion to the list. Duplicates will be ignored.
     *
     * @param inclusionCriterion
     */
    public final void include(String inclusionCriterion) {
        if (inclusionCriterion.charAt(0) == '$') {
            List<String> criteria = interpret(inclusionCriterion.substring(1));
            for (String c : criteria) {
                include(c);
            }
        } else {
            List<String> includes = getIncludeStrings();
            if ((inclusionCriterion != null) && (!includes.contains(inclusionCriterion))) {
                addIncludeString(inclusionCriterion);
            }
        }
    }

    public final List<String> getIncludes() {
        List<String> result = getIncludeStrings();
        if (result != null) {
            Collections.sort(result);
        }
        return result;
    }

    public final List<String> getExcludes() {
        List<String> result = getExcludeStrings();
        if (result != null) {
            Collections.sort(result);
        }
        return result;
    }

    /**
     * @param key
     * @return
     */
    protected List<String> interpret(String key) {
        return new ArrayList<String>();
    }

    public void addExcludeString(String string) {
        if (string != null) {
            T attribute = fromString(string);
            if (attribute != null) {
                exclude.add(attribute);
            }
        }
    }

    public void addIncludeString(String string) {
        if (string != null) {
            T attribute = fromString(string);
            if (attribute != null) {
                include.add(attribute);
            }
        }
    }

    List<String> getIncludeStrings() {
        List<String> result = new ArrayList<String>();
        for (T attribute : include) {
            result.add(toString(attribute));
        }
        return result;
    }

    List<String> getExcludeStrings() {
        List<String> result = new ArrayList<String>();
        for (T attribute : exclude) {
            result.add(toString(attribute));
        }
        return result;
    }
}