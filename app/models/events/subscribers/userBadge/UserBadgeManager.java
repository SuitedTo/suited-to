package models.events.subscribers.userBadge;

import jobs.RobustJob;
import models.*;
import models.events.channels.userBadge.BadgeProgressEvent;
import utils.publishSubcribe.Subscriber;

import java.util.ArrayList;
import java.util.List;

public class UserBadgeManager implements Subscriber<BadgeProgressEvent>{

	private final List<UserBadge> record = new ArrayList<UserBadge>();

	@Override
	public void handlePublication(final BadgeProgressEvent event){
		new RobustJob(){
			public void tryJob(){
				updateBadges(event);
			}
		}.now();
	}

	public void updateBadges(final BadgeProgressEvent event){
		ModelBase eventSource = event.getSource();
		
		List<Long> associatedEntityIds = new ArrayList<Long>();
		User user = null;
		if(eventSource instanceof UserMetrics){
			user = ((UserMetrics) eventSource).user;
			associatedEntityIds.add(user.id);
		} else if(eventSource instanceof User){
			user = (User) eventSource;
			if(event.getPropertyName().equals("reviewCategory")){
				Category category = (Category)event.getNewValue();
				associatedEntityIds.add(category.id);
			}
		} else if(eventSource instanceof Question){
			Question question = ((Question) eventSource);
			user = question.user;
            associatedEntityIds.add(question.id);
		} else if(eventSource instanceof Category){
			Category category = ((Category) eventSource);
			user = category.creator;
			associatedEntityIds.add(category.id);
		}

		if(user == null){
			return;
		}
		
		user = User.findById(user.id);
		
		if(user == null){
			return;
		}

		UserBadge badge = user.addOrUpdateBadge(event.getTrigger().key,
				event.getMultiplier(),
				(int)(event.getProgress() * 100),
				associatedEntityIds);

		badge.save();
		record.remove(badge);
		record.add(badge);
	}

	public List<UserBadge> getRecord(){
		List<UserBadge> result = new ArrayList<UserBadge>(record.size());
		for(UserBadge badge : record){
			result.add(badge);
		}
		return result;
	}
}
