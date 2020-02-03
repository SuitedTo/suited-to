package cache;

import java.util.Properties;

import org.hibernate.cache.Cache;
import org.hibernate.cache.CacheException;


/**
 * Cache provider - works for us until we move up to a
 * newer version of hibernate.
 * 
 * @author joel
 *
 */
public class HibernateCacheProvider implements org.hibernate.cache.CacheProvider{
	private final String region = "default";
	private HibernateCache cache;

	public HibernateCacheProvider(){
		cache = new HibernateCache(region);
	}
	
	public Cache buildCache(String regionName, Properties properties)
			throws CacheException {
		return new HibernateCache(region);
	}

	public boolean isMinimalPutsEnabledByDefault() {
		return true;
	}

	public long nextTimestamp() {
		return cache.nextTimestamp();
	}

	public void start(Properties arg0) throws CacheException {
		
	}

	public void stop() {
		
	}

	public String toString() {
		return "play";
	}

}
