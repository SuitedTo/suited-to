package models;

import enums.EventType;
import models.annotations.PropertyChangeListeners;
import newsfeed.listener.UserBadgeNewsFeedListener;
import newsfeed.metadata.UserBadgeEventMetadata;
import org.apache.commons.lang.StringUtils;
import play.db.jpa.JPABase;
import play.templates.BaseTemplate;
import play.templates.TemplateLoader;
import utils.YamlUtil;

import javax.persistence.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

@Entity
@PropertyChangeListeners(UserBadgeNewsFeedListener.class)
public class UserBadge extends ModelBase {

    public static final LinkedHashMap badgeData = new LinkedHashMap();
    public static final LinkedHashMap triggers = new LinkedHashMap();
	public static final LinkedHashMap progressMessages = new LinkedHashMap();
	
	static{
		LinkedHashMap data = YamlUtil.loadYaml("badges.yml");
		Iterator<String> it = data.keySet().iterator();
		while(it.hasNext()){
			String next = it.next();
			if(next.startsWith("badge.")){
				badgeData.put(next.substring("badge.".length()),
						data.get(next));
			}else if(next.startsWith("trigger.")){
				Map<Object, Object> props = (Map<Object, Object>) data.get(next);
				String key = next.substring("trigger.".length());
				triggers.put(key,
						new BadgeTrigger(
								key,
								(String)props.get("entity"),
								(String)props.get("propertyName"), 
								BadgeTrigger.Type.valueOf((String)props.get("type")),
								String.valueOf(props.get("targetValue"))));
			}else if(next.startsWith("progressMsg.")){
				progressMessages.put(next.substring("progressMsg.".length()),
						data.get(next));
			}
		}
	}
	
	@ManyToOne
	public User user;
	
	@Temporal(TemporalType.TIMESTAMP)
	public Date earned;
	
	public String name;
	
	public int multiplier;
	
	public String subjectIds;
	
	public int progress;

	public UserBadge(String name, int multiplier, int progress, User user){
		this(name, multiplier, progress, user, null);
	}

	public UserBadge(String name, int multiplier, int progress, User user, List<Long> subjectIds){
		this.name = name;
		this.user = user;
		
		if(multiplier > 0){
			this.earned = new Date();
		}

		this.multiplier = multiplier;
		this.progress = progress;
		this.subjectIds = StringUtils.join(subjectIds, ',');
	}

    @Override
    public <T extends JPABase> T save() {
        boolean updatingExisting = this.hasBeenSaved();
        T result = super.save();
        //if we're creating a new UserBadge and the multiplier is > 0 then create an event indicating that a badge has
        //been awarded.  Updates to existing UserBadges are monitored by UserBadgeNewsFeedListener so we only need to
        //create events when saving new UserBadges
        if(!updatingExisting && multiplier > 0){
            ((UserBadge)result).buildDefaultEvent().save();
        }
        return result;
    }

    /**
     * Creates an Event that can either be saved immediately or added to the ModelBase eventsToCreateWhileSaving. This
     * is used to facilitate the creation of Event records both when creating new UserBadges and updating existing ones
     * @return Event
     */
    public Event buildDefaultEvent(){
        Event event = new Event();
        event.eventType = EventType.BADGE;
        event.relatedUser = this.user;
        //build the event metadata
        UserBadgeEventMetadata metadata = new UserBadgeEventMetadata();
        metadata.setUserBadgeId(this.id);
        event.serializeAndSetEventMetadata(metadata);
        return event;
    }
    
    public static Collection<BadgeTrigger> getTriggers(){
    	return triggers.values();
    }
    
    public static List<BadgeTrigger> getTriggers(Class<? extends ModelBase> clazz, String propertyName){
    	if((clazz == null) || (propertyName == null)){
    		return null;
    	}
    	
    	List<BadgeTrigger> result = new ArrayList<BadgeTrigger>();
    	Iterator keys = triggers.keySet().iterator();
    	while(keys.hasNext()){
    		BadgeTrigger trigger = (BadgeTrigger) triggers.get(keys.next());
    		if(trigger != null){
    			if(trigger.entity.equals(clazz.getName()) &&
    					trigger.propertyName.equals(propertyName)){
    				result.add(trigger);
    			}
    		}
    	}
    	return result;
    }

    public BadgeInfo getInfo(){
		
		Map<Object, Object> data = (Map<Object, Object>) UserBadge.badgeData.get(name);
		
		if(data == null){
			return new BadgeInfo(
					name,
					"",
					"!!! Missing static data for badge [" + name + "]",
					"",
					multiplier,
					progress,
					""
					);
		}
		
		String title = String.valueOf(data.get("title"));
		String description = String.valueOf(data.get("description"));
		
		String progressMessage = "";
		Object pmo = data.get("progressMessages");
		if(pmo != null){

			List<String> pmKeys = (List)pmo;
			for(String key : pmKeys){
				Map<Object, Object> pmMap = (Map<Object, Object>) UserBadge.progressMessages.get(key);
				if(pmMap != null){
					Object rangeObj = pmMap.get("range");
					if(rangeObj != null){
						List<Integer> range = (List)pmMap.get("range");
						if(range.size() == 1){
							if(this.progress == range.get(0)){
								progressMessage = (String) pmMap.get("text");
								break;
							}
						} else if(range.size() == 2){
							if((this.progress >= range.get(0)) && (this.progress <= range.get(1))){
								progressMessage = (String) pmMap.get("text");
								break;
							}
						}
					}
				}
			}
		}
		Map<String, Object> vars = new Hashtable<String, Object>();
		
		vars.put("badge", this);
		
		if(subjectIds != null){
			String[] ids = subjectIds.split(",");
			for(String idStr : ids){
				Long id = Long.parseLong(idStr);

				Category category = Category.findById(id);
				if(category != null){
					vars.put("category", category);
				} else{
					User user = User.findById(id);
					if(user != null){
						vars.put("user", user);
					} else{
                        Question question = Question.findById(id);
                        if(question != null){
                            vars.put("question", question);
                        }
                    }
				}
			}		
		}

		title = bindVars("badgeTitle_" + name, title, vars);
		description = bindVars("badgeDesc_" + name, description, vars);
		progressMessage = bindVars("BadgepMsg_" + name, progressMessage, vars);

		return new BadgeInfo(
				name,
				title,
				description,
				String.valueOf(data.get("icon")),
				multiplier,
				progress,
				progressMessage
				);
	}
	
	private static String bindVars(String key, String template, Map<String, Object> args){
		BaseTemplate t = (BaseTemplate) TemplateLoader.load(key, template);
		t.name = "blah.html";
		StringWriter sw = new StringWriter();
        args.put("_isInclude", true);
        args.put("out", new PrintWriter(sw));
		t.render(args);
		return sw.toString();
	}

	public static class BadgeInfo{
		public final String name;
		public final String title;
		public final String description;
		public final String icon;
		public final int multiplier;
		public final int progress;
		public final String progressMessage;
		
		public BadgeInfo(String name, String title, String description, String icon,
				int multiplier, int progress, String progressMessage) {
			this.name = name;
			this.title = title;
			this.description = description;
			this.icon = icon;
			this.multiplier = multiplier;
			this.progress = progress;
			this.progressMessage = progressMessage;
		}
	}
	
	public static class BadgeTrigger{
		public final String key;
		public final String entity;
		public final String propertyName;
		public final Type type;
		public final String targetValue;
		public BadgeTrigger(String key, String entity, String propertyName, Type type,
				String targetValue) {
			this.key = key;
			this.entity = entity;
			this.propertyName = propertyName;
			this.type = type;
			this.targetValue = targetValue;
		}
		public enum Type {STANDARD, MODULO};
	}
}
