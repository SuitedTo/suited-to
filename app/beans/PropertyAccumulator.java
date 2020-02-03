package beans;

import java.lang.reflect.Array;
import java.util.Collection;


/**
 * This is a bean wrapper that supports functionality associated with
 * the idea that it's properties can "fill up" or reach some target value.
 * 
 * 
 * @author joel
 *
 */
public class PropertyAccumulator extends BeanWrapper{

	public PropertyAccumulator(Object instance) {
		super(instance);
	}
	
	public Progress getProgress(String propertyKey, Object targetValue){
		throw new UnsupportedOperationException("Not implemented yet");
	}
	
	/**
	 * Get the progress of the specified property toward the given
	 * target value.
	 * 
	 * @param propertyKey
	 * @param targetValue
	 * @return
	 */
	public Progress getProgress(String propertyKey, String targetValue){
		
		return Progress.getSimpleValueProgress(getProperty(propertyKey).getType(),
				get(propertyKey),
				targetValue);
	}

}
