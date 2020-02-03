package beans;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.List;

/**
 * This class represents progress toward some target value.
 * 
 * @author joel
 *
 */
public class Progress {

	private long current;
	private long targetValue;
	
	public static final Progress NONE = new Progress(0,1);
	public static final Progress FINISHED = new Progress(1,1);
	
	public Progress(long current, long goal) {
		super();
		this.current = current;
		this.targetValue = goal;
	}

	public long getCurrent() {
		return current;
	}

	public long getTargetValue() {
		return targetValue;
	}
	
	public double asRatio(){
		return (((double)current) / ((double)targetValue));
	}
	
	public int asPercentage(){
		if(targetValue == 0){
			return 0;
		}
		if(current > targetValue){
			return 100;
		}
		return 100  * ((int) (((float)current) / ((float)targetValue)));
	}
	
	/**
	 * 
	 * Get the progress of a value toward some taget value. The target
	 * value here is a string that represents a simple value that will be
	 * converted into a type that makes sense given the clazz.
	 * 
	 * @param clazz
	 * @param value
	 * @param targetValue
	 * @return
	 */
	public static Progress getSimpleValueProgress(Class clazz, Object value, String targetValue){
		if(targetValue == null){
			return Progress.NONE;
		}
		
		if(value == null){
			return Progress.NONE;
		}
		
		if(clazz.equals(int.class) || clazz.equals(Integer.class) ||
				clazz.equals(long.class) || clazz.equals(Long.class)){
			if(isIndexed(clazz)){
				if(Collection.class.isAssignableFrom(clazz)){
					Collection c = (Collection)value;
					return new Progress(c.size(), Long.valueOf(targetValue));
				}
				if(clazz.isArray()){
					return new Progress(Array.getLength(value), Long.valueOf(targetValue));
				}
			}else{
				return new Progress( Long.valueOf(value.toString()), Long.valueOf(targetValue));
			}
			
		}
		
		if(clazz.isEnum()){
			int current = 0;
			try{
				current = Enum.valueOf(clazz, targetValue)
					.equals(value)?1:0;
			}catch(Exception e){
				
			}
			return new Progress(current, 1);
		}
		
		if(clazz.equals(boolean.class) || clazz.isInstance(Boolean.class)){
			int current = ((Boolean)value).equals(Boolean.valueOf(targetValue))?1:0;
			return new Progress(current, 1);
		}
		
		return Progress.NONE;
	}
	
	private static boolean isIndexed(Class clazz) {

        if (clazz == null) {
            return (false);
        } else if (clazz.isArray()) {
            return (true);
        } else if (List.class.isAssignableFrom(clazz)) {
            return (true);
        } else {
            return (false);
        }

    }
}
