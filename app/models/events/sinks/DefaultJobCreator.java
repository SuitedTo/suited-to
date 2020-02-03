package models.events.sinks;

import models.Company;
import models.Job;
import models.events.EntityCreated;
import models.events.EntityEvent;
import models.events.EntityEventSink;
import models.events.EventSinkAttributes;

public class DefaultJobCreator implements EntityEventSink{

	@Override
	public EventSinkAttributes getAttributes() {
		EventSinkAttributes attr = new EventSinkAttributes();
		attr.addEventPattern(Company.class).created();
		return attr;
	}

	@Override
	public void processEvent(EntityEvent ee) {
		final EntityCreated event = (EntityCreated)ee;
		
			new play.jobs.Job(){
				public void doJob(){
					Company company = (Company) event.getSource();
					new Job(company).save();
				}
			}.now();
	}

}
