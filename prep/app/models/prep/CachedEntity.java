package models.prep;

import exceptions.prep.CacheMiss;
import play.cache.Cache;

public class CachedEntity {
	private final String key;
	
	public CachedEntity(Class<? extends EbeanModelBase> clazz, Long id){
		this.key = new StringBuilder(clazz.getName()).append("[").append(id).append("]").toString();
	}
	
	protected Boolean _getAsBoolean(String key) throws CacheMiss{
		String cached = (String) Cache.get(this.key + "." + key);
		if(cached != null){
			return Boolean.valueOf(cached);
		}
		throw new CacheMiss();
	}
	
	protected void _setValue(String key, Object value, String duration){
		Cache.add(this.key + "." + key, String.valueOf(value), duration);
	}
	
	protected void _setValue(String key, Object value){
		Cache.add(this.key + "." + key, String.valueOf(value),"8h");
	}
	
	protected void _unsetValue(String key){
		Cache.delete(this.key + "." + key);
	}

}
