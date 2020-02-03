package beans;

import beans.Progress;

public interface ProgressEvaluator {
	/**
	 * @param bean
	 * @param propertyName
	 * @param targetValue
	 * @param value (optional) If not null, the evaluator may use this value instead of trying
	 * to extract the value from the bean.
	 * 
	 * @return
	 */
	public Progress evaluate(Object bean, String propertyName, String targetValue, Object value);
}
