package models.events.channels.userBadge;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import models.ModelBase;
import models.UserBadge;
import models.UserBadge.BadgeTrigger;
import models.events.EntityEvent;
import models.events.EntityUpdated;
import models.events.channels.userBadge.PropertyChangeTransformer.PropertyChange;
import play.Logger;
import beans.BeanProperty;
import beans.BeanWrapper;
import beans.Progress;
import beans.ProgressEvaluator;

public class BadgeProgressEventBuilder{

	private Map<String, ProgressEvaluator> customEvaluators =
			new Hashtable<String, ProgressEvaluator>();
	private Map<String, PropertyChangeTransformer> propertyChangeTransformers =
			new Hashtable<String, PropertyChangeTransformer>();


	public BadgeProgressEventBuilder(){
		registerCustomEvaluator("reviewer", new ProgressEvaluator(){

			@Override
			public Progress evaluate(Object bean, String propertyName,String targetValue, Object value) {
				return Progress.FINISHED;
			}

		});
		registerPropertyChangeTransformer("reviewer",
				new PropertyChangeTransformer(){

			@Override
			public List<PropertyChange> transform(PropertyChange property) {
				if(property.getValue() == null){
					return null;
				}

				List previousValues = (List)property.getPreviousValue();
				List<PropertyChange> result = new ArrayList<PropertyChange>();
				List values = (List)property.getValue();
				for(Object value : values){
					Object previousValue = null;
					if((previousValues != null) && (previousValues.contains(value))){
						previousValue = value;
					}
					result.add(new PropertyChange("reviewCategory",previousValue, value));
				}
				return result;
			}
		});
	}

	protected void registerCustomEvaluator(String key, ProgressEvaluator evaluator){
		customEvaluators.put(key, evaluator);
	}

	protected void registerPropertyChangeTransformer(String key, PropertyChangeTransformer transformer){
		propertyChangeTransformers.put(key, transformer);
	}

	private ProgressEvaluator getProgressEvaluator(String key){
		ProgressEvaluator pe = customEvaluators.get(key);
		if(pe == null){
			return new DefaultProgressEvaluator();
		}
		return pe;
	}

	protected List<BadgeProgressEvent> transformEvent(EntityEvent event) {
		List<BadgeProgressEvent> result = new ArrayList<BadgeProgressEvent>();
		EntityUpdated evt = (EntityUpdated)event;
		List<BadgeTrigger> triggers = UserBadge.getTriggers((Class<? extends ModelBase>) evt.getSource().getClass(), evt.getPropertyName());
		if(triggers == null){
			return result;
		}

		for(BadgeTrigger trigger : triggers){
			PropertyChange originalProp = new PropertyChange(evt.getPropertyName(), evt.getOldValue(),evt.getNewValue());
			List<PropertyChange> props;
			PropertyChangeTransformer transformer = propertyChangeTransformers.get(trigger.key);
			if(transformer != null){
				props = transformer.transform(originalProp);
				if(props == null){
					return result;
				}
			}else{
				props = new ArrayList<PropertyChange>();
				props.add(originalProp);
			}

			for(PropertyChange p : props){

				ProgressEvaluator evaluator = getProgressEvaluator(trigger.key);
				Progress progress = evaluator.evaluate(evt.getSource(), p.getName(), trigger.targetValue, p.getValue());
				int multiplier = (int) progress.asRatio();
				double dProgress = progress.asRatio() - multiplier;				
				if (trigger.type.equals(UserBadge.BadgeTrigger.Type.STANDARD) && (multiplier >= 1)) {
					multiplier = 1;
					dProgress = 0d;
				}

				Progress previousProgress = evaluator.evaluate(evt.getSource(), p.getName(), trigger.targetValue, p.getPreviousValue());
				int previousMultiplier = (int) previousProgress.asRatio();				
				double dPreviousProgress = previousProgress.asRatio() - previousMultiplier;
				if (trigger.type.equals(UserBadge.BadgeTrigger.Type.STANDARD) && (previousMultiplier >= 1)) {
					previousMultiplier = 1;
					dPreviousProgress = 0d;
				}

				result.add(new BadgeProgressEvent(
						trigger,
						evt.getSource(),
						p.getName(),
						p.getValue(),
						previousMultiplier,
						multiplier,
						dPreviousProgress,
						dProgress)
						);
			}
		}
		return result;
	}

	public static List<BadgeProgressEvent> assessEntity(ModelBase bean){
		List<BadgeProgressEvent> result = new ArrayList<BadgeProgressEvent>();
		BadgeProgressEventBuilder transformer = new BadgeProgressEventBuilder();
		if(bean == null){
			return result;
		}
		BeanWrapper umWrapped = new BeanWrapper(bean);
		Collection<BeanProperty> props = umWrapped.getWrappers();
		for(BeanProperty prop : props){
			Object value = prop.getValue(bean);
			EntityUpdated event =
					new EntityUpdated(bean, prop.getName(), value, value);
			result.addAll(transformer.transformEvent(event));
		}
		return result;
	}

}
