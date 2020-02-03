package controllers;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.apache.commons.lang.time.DateUtils;

import models.Alert;
import models.User;
import play.db.jpa.JPA;
import play.mvc.Util;

public class Alerts extends ControllerBase{

	public static void next(Long userId){
		User user = null;
		if(userId == null){
			user = Security.connectedUser();
		} else {
			user = User.findById(userId);
		}
		if(user == null){
			user = Security.connectedUser();
		}
		Query q = JPA.em().createQuery("select a from Alert a where a.user=:user and acknowledged=null");
		q.setParameter("user", user);
		List alerts = q.getResultList();
		if(alerts.size() > 0){
			Alert alert = (Alert)q.getResultList().get(0);
			Map<String, Object> props = new HashMap<String, Object>();
			props.put("id", alert.id);
			props.put("type", alert.type.name());
			props.put("message", alert.message);
			renderJSON(props);
		}
		renderJSON("{}");		
	}
	
	public static void ack(Long id){
		if(id != null){
			Alert alert = Alert.findById(id);
			if(alert != null){
				alert.acknowledged = new Date();
				alert.save();
			}
		}
	}

	@Util
	public static void raise(Alert alert){
		//--disabled for now
//		if((alert != null) && (!alert.hasBeenSaved())){
//			List<Alert> duplicates = Alert.find("byMessageAndUser", alert.message, alert.user).fetch();
//			Iterator it = duplicates.iterator();			
//			while(it.hasNext()){
//				Alert duplicate = (Alert) it.next();
//				if(duplicate.acknowledged == null){
//					return;
//				}
//				Date fiveMinutesAgo = DateUtils.addMinutes(new Date(), -5);
//				if(duplicate.acknowledged.before(fiveMinutesAgo)){
//					it.remove();
//					duplicate.delete();					
//				}
//			}
//			if(duplicates.isEmpty()){
//				alert.save();
//			}
//		}
	}
}
