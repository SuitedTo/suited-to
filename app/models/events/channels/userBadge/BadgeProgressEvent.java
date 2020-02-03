package models.events.channels.userBadge;

import models.ModelBase;
import models.UserBadge.BadgeTrigger;

public class BadgeProgressEvent {
	private BadgeTrigger trigger;
	private ModelBase source;
	private String propertyName;
	private Object newValue;
	private int multiplier;
	private int previousMultiplier;
	private double progress;
	private double previousProgress;
	
	public BadgeProgressEvent(BadgeTrigger trigger,
			ModelBase source,
			String propertyName,
			Object newValue,
			int previousMultiplier,
			int multiplier,
			double previousPogress,
			double progress) {
		super();
		this.trigger = trigger;
		this.source = source;
		this.propertyName = propertyName;
		this.newValue = newValue;
		this.previousMultiplier = previousMultiplier;
		this.multiplier = multiplier;
		this.progress = progress;
		this.previousProgress = previousProgress;
	}
	public BadgeTrigger getTrigger() {
		return trigger;
	}
	public void setTrigger(BadgeTrigger trigger) {
		this.trigger = trigger;
	}
	public ModelBase getSource() {
		return source;
	}
	public void setSource(ModelBase source) {
		this.source = source;
	}
	public String getPropertyName() {
		return propertyName;
	}
	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}
	public Object getNewValue() {
		return newValue;
	}
	public void setNewValue(Object newValue) {
		this.newValue = newValue;
	}
	public int getMultiplier() {
		return multiplier;
	}
	public void setMultiplier(int multiplier) {
		this.multiplier = multiplier;
	}
	public double getProgress() {
		return progress;
	}
	public void setProgress(double progress) {
		this.progress = progress;
	}
	public int getPreviousMultiplier() {
		return previousMultiplier;
	}
	public void setPreviousMultiplier(int previousMultiplier) {
		this.previousMultiplier = previousMultiplier;
	}
	public double getPreviousProgress() {
		return previousProgress;
	}
	public void setPreviousPogress(double previousProgress) {
		this.previousProgress = previousProgress;
	}
}

