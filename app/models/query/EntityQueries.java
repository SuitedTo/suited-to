package models.query;

import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import models.ModelBase;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Entity;

import play.Play;
import play.db.jpa.JPA;
import play.db.jpa.JPABase;

/**
 * Our own version of the JPQL that adds our own common model methods and
 * does custom things like setting query hints.
 * 
 * @author joel
 *
 */
public class EntityQueries {

	    public EntityManager em() {
	        return JPA.em();
	    }
	    
	    public long count(String entity) {
	    	Query query = em().createQuery("SELECT COUNT(*) FROM " + entity + " e");
	    	
	    	query.setHint("org.hibernate.cacheable", true);
	    	
	    	return (Long) query.getSingleResult();
	    }
	    
	    public List findAll(String entity) {
	    	Query query = em().createQuery("select e from " + entity + " e");
	    	
	    	//Set cacheable only for the types of entities that aren't frequently
	    	//added or updated and not so numerous.
	    	if(entity.equals("Company") || entity.equals("Category")){
	    		query.setHint("org.hibernate.cacheable", true);
	    	}
	    	
	    	return query.getResultList();
	    }
	    
	    public ModelBase findById(String entity, Object id, boolean optimize) throws Exception {
	    	
	    	if(!optimize){
	    		return selectById(entity, (Long)id);
	    	}
                
                /*
                //The commented-out block here is a fix for a hibernate bug
                //discussed here:
                //  http://andykayley.blogspot.com/2009/05/how-to-avoid-classcastexceptions-when.html
                //Which we only ever saw in relation to SQS queuing (which we
                //ended up not using).  The fix has been removed to decrease our
                //coupling with Hibernate, but preserved in case this bug should
                //rear it's head again.
                 
                Object untypedResult = 
                        em().find(Play.classloader.loadClass(entity), id);
                
                Hibernate.initialize(untypedResult);
                
	    	return (ModelBase) untypedResult;
                */
                
                return (ModelBase) em().find(Play.classloader.loadClass(entity), id);
	    }
	    
	    public ModelBase findFirst(String entity, String query, Object[] params) {
	        Query q = em().createQuery(
	                createFindByQuery(entity, entity, query, params));
	        List result = bindParameters(q, params).getResultList();
	        if(result.size() > 0){
	        	return (ModelBase) result.get(0);
	        }
	        return null;
	    }
	    
	    /**
	     * Play's findById method is optimized to return an object from the persistent
	     * store if available. This is fine but this also means that when an object is fetched
	     * from the persistent store your @preload and @postload handlers won't be called because
	     * the object didn't need to be loaded. Our enhancer checks for these annotations and
	     * calls this method if they are present to ensure that the handlers are invoked.
	     * 
	     * Please leave this method private
	     * 
	     * @param id
	     * @return
	     */	   
	    private ModelBase selectById(String entity, long id) throws Exception {
	    	Query query = em().createQuery("SELECT e FROM " + entity + " e where id=:id");
	    	query.setParameter("id", id);
	    	query.setHint("org.hibernate.cacheable", true);
	    	Object result = query.getSingleResult();
	    	if(result == null){
	    		return null;
	    	}
	        return (ModelBase) result;
	    }
	    
	    @SuppressWarnings("unchecked")
	    public Query bindParameters(Query q, Object... params) {
	        if (params == null) {
	            return q;
	        }
	        if (params.length == 1 && params[0] instanceof Map) {
	            return bindParameters(q, (Map<String, Object>) params[0]);
	        }
	        for (int i = 0; i < params.length; i++) {
	            q.setParameter(i + 1, params[i]);
	        }
	        return q;
	    }

	    public Query bindParameters(Query q, Map<String,Object> params) {
	        if (params == null) {
	            return q;
	        }
	        for (String key : params.keySet()) {
	            q.setParameter(key, params.get(key));
	        }
	        return q;
	    }
	    
	    public String createFindByQuery(String entityName, String entityClass, String query, Object... params) {
	        if (query == null || query.trim().length() == 0) {
	            return "from " + entityName;
	        }
	        if (query.matches("^by[A-Z].*$")) {
	            return "from " + entityName + " where " + findByToJPQL(query);
	        }
	        if (query.trim().toLowerCase().startsWith("select ")) {
	            return query;
	        }
	        if (query.trim().toLowerCase().startsWith("from ")) {
	            return query;
	        }
	        if (query.trim().toLowerCase().startsWith("order by ")) {
	            return "from " + entityName + " " + query;
	        }
	        if (query.trim().indexOf(" ") == -1 && query.trim().indexOf("=") == -1 && params != null && params.length == 1) {
	            query += " = ?1";
	        }
	        if (query.trim().indexOf(" ") == -1 && query.trim().indexOf("=") == -1 && params == null) {
	            query += " = null";
	        }
	        return "from " + entityName + " where " + query;
	    }
	    
	    public String findByToJPQL(String findBy) {
	        findBy = findBy.substring(2);
	        StringBuffer jpql = new StringBuffer();
	        String[] parts = findBy.split("And");
	        for (int i = 0; i < parts.length; i++) {
	            String part = parts[i];
	            if (part.endsWith("NotEqual")) {
	                String prop = extractProp(part, "NotEqual");
	                jpql.append(prop + " <> ?");
	            } else if (part.endsWith("Equal")) {
	                String prop = extractProp(part, "Equal");
	                jpql.append(prop + " = ?");
	            } else if (part.endsWith("IsNotNull")) {
	                String prop = extractProp(part, "IsNotNull");
	                jpql.append(prop + " is not null");
	            } else if (part.endsWith("IsNull")) {
	                String prop = extractProp(part, "IsNull");
	                jpql.append(prop + " is null");
	            } else if (part.endsWith("LessThan")) {
	                String prop = extractProp(part, "LessThan");
	                jpql.append(prop + " < ?");
	            } else if (part.endsWith("LessThanEquals")) {
	                String prop = extractProp(part, "LessThanEquals");
	                jpql.append(prop + " <= ?");
	            } else if (part.endsWith("GreaterThan")) {
	                String prop = extractProp(part, "GreaterThan");
	                jpql.append(prop + " > ?");
	            } else if (part.endsWith("GreaterThanEquals")) {
	                String prop = extractProp(part, "GreaterThanEquals");
	                jpql.append(prop + " >= ?");
	            } else if (part.endsWith("Between")) {
	                String prop = extractProp(part, "Between");
	                jpql.append(prop + " < ? AND " + prop + " > ?");
	            } else if (part.endsWith("Like")) {
	                String prop = extractProp(part, "Like");
	                // HSQL -> LCASE, all other dbs lower
	                if (isHSQL()) {
	                    jpql.append("LCASE(" + prop + ") like ?");
	                } else {
	                    jpql.append("LOWER(" + prop + ") like ?");
	                }
	            } else if (part.endsWith("Ilike")) {
	                String prop = extractProp(part, "Ilike");
	                 if (isHSQL()) {
	                    jpql.append("LCASE(" + prop + ") like LCASE(?)");
	                 } else {
	                    jpql.append("LOWER(" + prop + ") like LOWER(?)");
	                 }
	            } else if (part.endsWith("Elike")) {
	                String prop = extractProp(part, "Elike");
	                jpql.append(prop + " like ?");
	            } else {
	                String prop = extractProp(part, "");
	                jpql.append(prop + " = ?");
	            }
	            if (i < parts.length - 1) {
	                jpql.append(" AND ");
	            }
	        }
	        return jpql.toString();
	    }
	    
	    private boolean isHSQL() {
	        String db = Play.configuration.getProperty("db");
	        return ("mem".equals(db) || "fs".equals(db) || "org.hsqldb.jdbcDriver".equals(Play.configuration.getProperty("db.driver")));
	    }

	    protected static String extractProp(String part, String end) {
	        String prop = part.substring(0, part.length() - end.length());
	        prop = (prop.charAt(0) + "").toLowerCase() + prop.substring(1);
	        return prop;
	    }
	    
	    public static EntityQueries instance = null;
}