package models.events.channels.userBadge;

import java.util.Collection;
import java.util.List;

import models.ModelBase;
import models.UserBadge;
import models.UserBadge.BadgeTrigger;
import models.events.EntityEvent;
import models.events.EventSinkAttributes;
import models.events.ModelDrivenEventChannel;
import models.events.subscribers.userBadge.UserBadgeManager;
import play.Logger;
import utils.publishSubcribe.Subscribers;

@Subscribers(UserBadgeManager.class)
public class BadgeProgressChannel extends ModelDrivenEventChannel<BadgeProgressEvent>{
	
	private BadgeProgressEventBuilder transformerDelegate = new BadgeProgressEventBuilder();

	@Override
	public EventSinkAttributes buildAttributes() {
		EventSinkAttributes attr = new EventSinkAttributes();
		Collection<BadgeTrigger> triggers = UserBadge.getTriggers();
		for(BadgeTrigger trigger : triggers){
			try {
				attr.addEventPattern((Class<? extends ModelBase>)Class.forName(trigger.entity)).updated(trigger.propertyName);
			} catch (ClassNotFoundException e) {
				Logger.error("Unable to add pattern. %s not found.", trigger.entity);
			}
		}
		return attr;
	}

	@Override
	public List<BadgeProgressEvent> transformEvent(EntityEvent event) {
		return transformerDelegate.transformEvent(event);
	}
}
