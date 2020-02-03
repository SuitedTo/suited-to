package models.filter;

import play.db.jpa.GenericModel;
import play.db.jpa.JPA;
import play.utils.Java;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.lang.reflect.Field;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Filter for attributes of entity objects.
 *
 * @param <M> Entity type
 * @param <T> Attribute type
 * @author joel
 */
public abstract class EntityAttributeFilter<M extends GenericModel, T> extends AttributeFilter<M, T> implements Filter<M> {

    public Predicate asPredicate(Root<M> root) {

        CriteriaBuilder criteriaBuilder = JPA.em().getCriteriaBuilder();

        Predicate criteria = (include.size() > 0) ? criteriaBuilder.disjunction() : criteriaBuilder.conjunction();

        Path path = getAttributePath(root);

        for (T member : include) {
            criteria = criteriaBuilder.or(criteria, criteriaBuilder.equal(path, member));
        }

        for (T member : exclude) {
            criteria = criteriaBuilder.and(criteria, criteriaBuilder.not(criteriaBuilder.equal(path, member)));
        }

        return criteria;
    }
    
    /**
     * This method is only intended to support the interview builder's native query
     * and shouldn't be called from anywhere else.
     * 
     * @param root
     * @param alias
     * @return
     */
    public final String asNativeMySQL(Root<M> root, String alias) {
    	Path path = getAttributePath(root);
    	boolean stringy = false;
    	if(path.getJavaType().isEnum()){
    		stringy = true;
    	} else if(path.getJavaType().equals(String.class)){
    		stringy = true;
    	}
    	
    	
    	StringBuilder criteria = new StringBuilder((include.size() > 0) ? "( 1=0" : "( 1=1");

        for (T member : include) {
        	
        	String value = toString(member);
        	if(stringy){
        		value = "'" + value + "'";
        	}
            criteria.append(" or ")
            .append(alias)
            .append(".")
            .append(getAttributeName()).append("=").append(value);
        }

        for (T member : exclude) {
        	String value = toString(member);
        	if(stringy){
        		value = "'" + value + "'";
        	}
        	criteria.append(" and ")
            .append(alias)
            .append(".")
            .append(getAttributeName()).append("<>").append(value);
        }

        return criteria.append(")").toString();
    }

    protected Path<T> getAttributePath(Root<M> root) {
        return root.<T>get(getAttributeName());
    }

    public boolean willAccept(M entity) {

        if (entity == null) {
            return false;
        }

        Set<Field> entityFields = new LinkedHashSet<Field>();
        Java.findAllFields(getEntityClass(), entityFields);

        for (Field field : entityFields) {
            if (field.getName().equals(getAttributeName())) {
                Object attribute;
                try {
                    attribute = field.get(entity);

                    if (attribute == null) {
                        return false;
                    }

                    if (include.size() != 0) {
                        if (!include.contains(attribute)) {
                            return false;
                        }
                    }

                    if (exclude.contains(attribute)) {
                        return false;
                    }

                    return true;
                } catch (Exception e) {
                    return false;
                }
            }
        }
        return false;
    }

    public abstract Class<M> getEntityClass();

}
