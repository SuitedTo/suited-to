package models.cache;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import exceptions.CacheMiss;

import models.ModelBase;
import models.cache.CachedEntity.CachedEntityContext;

import play.cache.Cache;
import play.data.binding.ParamNode;
import play.db.jpa.GenericModel;
import play.db.jpa.JPA;
import play.db.jpa.JPABase;
import play.mvc.Scope.Session;

/**
 * Cached entity object
 *
 * @param <M>
 * @author joel
 */
public class EntityCache {

    private EntityCache() {
    }
    
    private static String getCacheKey(Class<? extends ModelBase> clazz) {
        StringBuilder keyBuilder = new StringBuilder(Session.current().getId());
        keyBuilder.append("_");
        keyBuilder.append(clazz.getName());
        return keyBuilder.toString();
    }

    private static String getCacheKey(long id, Class<? extends ModelBase> clazz) {
        StringBuilder keyBuilder = new StringBuilder(clazz.getName());
        keyBuilder.append("_");
        keyBuilder.append(id);
        return keyBuilder.toString();
    }

    private static String getCacheKey(ModelBase entity) {
        if (entity.id == null) {
            return getCacheKey(entity.getClass());
        }
        return getCacheKey(entity.id, entity.getClass());
    }

    /**
     * Get a privately cached entity.
     * 
     * @param clazz
     * @return
     * @throws CacheMiss
     */
    static CachedEntity get(Class<? extends ModelBase> clazz) throws CacheMiss {
        Object result = Cache.get(getCacheKey(clazz));
        if (result == null) {
            throw new CacheMiss(getCacheKey(clazz));
        }
        return (CachedEntity) result;
    }

    static CachedEntity get(long id, Class<? extends ModelBase> clazz) throws CacheMiss{
        Object result = Cache.get(getCacheKey(id, clazz));
        if (result == null) {
        	throw new CacheMiss(getCacheKey(id, clazz));
        }
        return (CachedEntity) result;
    }

    static CachedEntity save(ModelBase entity) {
        if (entity == null) {
            return null;
        }
        
        CachedEntity ce = new CachedEntity(entity, null);
        
        Cache.safeSet(getCacheKey(entity), ce, "24h");
        return ce;
    }

    /**
     * An entity that is privately cached is one that is identified by the connected user's session
     * id rather than by the entity id.
     * 
     * @param entity
     * @return
     */
    static <M extends ModelBase> M savePrivate(M entity) {
        
    	 return (M) savePrivate((ModelBase) entity, (Class<ModelBase>) entity.getClass());
    }
    
    static <N extends ModelBase, M extends N> M savePrivate(M entity, 
                Class<N> asClass) {
        
        if (entity == null) {
            return null;
        }

        CachedEntity ce = new CachedEntity(entity, null);

        Cache.safeSet(getCacheKey(asClass), ce, "24h");
        return (M) ce.getEntity();
    }
    
    /**
     * An entity that is privately cached is one that is identified by the connected user's session
     * id rather than by the entity id.
     * 
     * @param entity
     * @param context
     * @return
     */
    static <M extends ModelBase, T extends CachedEntity.CachedEntityContext>
    	CachedEntity<M,T> savePrivate(M entity, T context) {
    	
        return (CachedEntity<M, T>) EntityCache.savePrivate((ModelBase) entity, context, (Class<ModelBase>)entity.getClass());
    }
    
    /**
     * An entity that is privately cached is one that is identified by the connected user's session
     * id rather than by the entity id.
     * 
     * @param entity
     * @param context
     * @return
     */
    static <N extends ModelBase, M extends N, T extends CachedEntity.CachedEntityContext>
    	CachedEntity<M,T> savePrivate(M entity, T context, Class<N> asClass) {
    	
        if (entity == null) {
            return null;
        }
        
        CachedEntity ce = new CachedEntity(entity, context);
        
        Cache.safeSet(getCacheKey(asClass), ce, "24h");
        return ce;
    }
    

    static CachedEntity delete(ModelBase entity) throws CacheMiss {
        if (entity == null) {
            return null;
        }
        
        Object result = Cache.get(getCacheKey(entity));
        if(result != null){
        	CachedEntity ce = (CachedEntity)result;
        	Cache.delete(getCacheKey(entity));
        	return ce;
        }
        throw new CacheMiss(getCacheKey(entity));
    }

    /**
     * Remove the cached private entity
     * 
     * @param entity
     * @return
     * @throws CacheMiss
     */
    static CachedEntity deletePrivate(Class<? extends ModelBase> clazz) throws CacheMiss {
        
        Object result = Cache.get(getCacheKey(clazz));
        if(result != null){
        	CachedEntity ce = (CachedEntity)result;
        	Cache.delete(getCacheKey(clazz));
        	return ce;
        }
        throw new CacheMiss(getCacheKey(clazz));
    }
}
