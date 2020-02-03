package models.cache;

import java.io.Serializable;

import models.ModelBase;

/**
 * Represents a cached entity. Allows for a context object to be cached along
 * with the entity object.
 * 
 * @author joel
 *
 * @param <T> Context type
 */
public class CachedEntity<M extends ModelBase, T extends CachedEntity.CachedEntityContext> implements Serializable {
	
	private final M entity;
	private final T context;
	
	public CachedEntity(M entity, T context) {
		super();
		this.context = context;
		this.entity = entity;
	}

	public M getEntity() {
		return entity;
	}


	public T getContext() {
		return context;
	}
	

	public static class CachedEntityContext{}
	
}
