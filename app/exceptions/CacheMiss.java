package exceptions;

/**
 * Forces us all to keep in mind the fact that we can never assume that an object is
 * in the cache.
 * 
 * @author joel
 *
 */
public class CacheMiss extends Exception{

	public CacheMiss(String description) {
		super(description);
	}
}
