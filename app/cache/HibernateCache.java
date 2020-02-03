package cache;

import java.io.IOException;
import java.util.Map;

import org.hibernate.cache.Cache;
import org.hibernate.cache.CacheException;

import com.googlecode.hibernate.memcached.KeyStrategy;
import com.googlecode.hibernate.memcached.Sha1KeyStrategy;
import com.jamonapi.utils.Logger;

import play.Play;
import play.cache.MemcachedImpl;

/**
 * Minor tweakage to the source from com.googlecode.hibernate.memcached.MemcachedCache
 * to allow us to use the same mc client for everything.
 *
 *
 * @author joel
 *
 */
public class HibernateCache implements Cache{

	private final String regionName;
	private play.cache.CacheImpl cache;
	private final String clearIndexKey;
	private int cacheTimeSeconds = 300;
	private boolean clearSupported = false;
	private KeyStrategy keyStrategy = new Sha1KeyStrategy();
	private boolean dogpilePreventionEnabled = false;
	private double dogpilePreventionExpirationFactor = 2;

	public static final Integer DOGPILE_TOKEN = 0;

	public HibernateCache(String regionName) {
		this.regionName = (regionName != null) ? regionName : "default";
		if(Play.mode.isDev()){
			cache = new NoCache();
		}else{
			try {
				this.cache = MemcachedImpl.getInstance();
			} catch (Exception e) {
				play.Logger.error("Unable to get memcached client. Running without Hibernate caching", e.getMessage());
				cache = new NoCache();
			}
		}
		clearIndexKey = this.regionName.replaceAll("\\s", "") + ":index_key";
	}

	public int getCacheTimeSeconds() {
		return cacheTimeSeconds;
	}

	public void setCacheTimeSeconds(int cacheTimeSeconds) {
		this.cacheTimeSeconds = cacheTimeSeconds;
	}

	public boolean isClearSupported() {
		return clearSupported;
	}

	public void setClearSupported(boolean clearSupported) {
		this.clearSupported = clearSupported;
	}

	public boolean isDogpilePreventionEnabled() {
		return dogpilePreventionEnabled;
	}

	public void setDogpilePreventionEnabled(boolean dogpilePreventionEnabled) {
		this.dogpilePreventionEnabled = dogpilePreventionEnabled;
	}

	public double getDogpilePreventionExpirationFactor() {
		return dogpilePreventionExpirationFactor;
	}

	public void setDogpilePreventionExpirationFactor(double dogpilePreventionExpirationFactor) {
		if (dogpilePreventionExpirationFactor < 1.0) {
			throw new IllegalArgumentException("dogpilePreventionExpirationFactor must be greater than 1.0");
		}
		this.dogpilePreventionExpirationFactor = dogpilePreventionExpirationFactor;
	}

	private String dogpileTokenKey(String objectKey) {
		return objectKey + ".dogpileTokenKey";
	}

	private Object memcacheGet(Object key) {
		String objectKey = toKey(key);

		return cache.get(objectKey);
	}

	private void memcacheSet(Object key, Object o) {
		String objectKey = toKey(key);

		int cacheTime = cacheTimeSeconds;

		if (dogpilePreventionEnabled) {
			String dogpileKey = dogpileTokenKey(objectKey);
			cache.set(dogpileKey, cacheTimeSeconds, DOGPILE_TOKEN);
			cacheTime = (int) (cacheTimeSeconds * dogpilePreventionExpirationFactor);
		}


		cache.set(objectKey, o, cacheTime);
	}

	private String toKey(Object key) {
		return keyStrategy.toKey(regionName, getClearIndex(), key);
	}

	public Object read(Object key) throws CacheException {
		return memcacheGet(key);
	}

	public Object get(Object key) throws CacheException {
		return memcacheGet(key);
	}

	public void put(Object key, Object value) throws CacheException {
		memcacheSet(key, value);
	}

	public void update(Object key, Object value) throws CacheException {
		put(key, value);
	}

	public void remove(Object key) throws CacheException {
		cache.delete(toKey(key));
	}

	/**
	 * Clear functionality is disabled by default.
	 * Read this class's javadoc for more detail.
	 *
	 * @throws CacheException
	 * @see com.googlecode.hibernate.memcached.MemcachedCache
	 */
	public void clear() throws CacheException {
		if (clearSupported) {
			cache.incr(clearIndexKey, 1);
		}
	}

	public void destroy() throws CacheException {
		//the client is shared by default with all cache instances, so don't shut it down.
	}

	public void lock(Object key) throws CacheException {
	}

	public void unlock(Object key) throws CacheException {
	}

	public long nextTimestamp() {
		return System.currentTimeMillis() / 100;
	}

	public int getTimeout() {
		return cacheTimeSeconds;
	}

	public String getRegionName() {
		return regionName;
	}

	public long getSizeInMemory() {
		return -1;
	}

	public long getElementCountInMemory() {
		return -1;
	}

	public long getElementCountOnDisk() {
		return -1;
	}

	public Map<?,?> toMap() {
		throw new UnsupportedOperationException();
	}

	public String toString() {
		return "Memcached (" + regionName + ")";
	}

	private long getClearIndex() {
		Long index = null;

		if (clearSupported) {
			Object value = cache.get(clearIndexKey);
			if (value != null) {
				if (value instanceof String) {
					index = Long.valueOf((String) value);
				} else if (value instanceof Long) {
					index = (Long) value;
				} else {
					throw new IllegalArgumentException(
							"Unsupported type [" + value.getClass() + "] found for clear index at cache key [" + clearIndexKey + "]");
				}
			}

			if (index != null) {
				return index;
			}
		}

		return 0L;
	}

	public KeyStrategy getKeyStrategy() {
		return keyStrategy;
	}

	public void setKeyStrategy(KeyStrategy keyStrategy) {
		this.keyStrategy = keyStrategy;
	}

}