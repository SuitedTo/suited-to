package cache;

import java.util.Map;

import play.cache.CacheImpl;

/**
 * Dummy cache for dev
 * 
 * @author joel
 *
 */
public class NoCache implements CacheImpl{

	@Override
	public void add(String key, Object value, int expiration) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean safeAdd(String key, Object value, int expiration) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void set(String key, Object value, int expiration) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean safeSet(String key, Object value, int expiration) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void replace(String key, Object value, int expiration) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean safeReplace(String key, Object value, int expiration) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Object get(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Object> get(String[] keys) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long incr(String key, int by) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long decr(String key, int by) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void delete(String key) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean safeDelete(String key) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		
	}

}
