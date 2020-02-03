package models.events.channels.userBadge;

import beans.BeanWrapper;
import beans.Progress;
import beans.ProgressEvaluator;
import beans.PropertyAccumulator;
import play.Logger;
import enums.TriggerType;

public class DefaultProgressEvaluator implements ProgressEvaluator{

	public Progress evaluate(
			Object bean, String propertyName, String targetValue, Object value){
		
		if(value != null){
			Class clazz = new BeanWrapper(bean, propertyName).getProperty(propertyName).getType();
			return Progress.getSimpleValueProgress(clazz, value, targetValue);
		}
		return new PropertyAccumulator(bean).getProgress(
				propertyName, targetValue);
	}
}