package newsfeed.listener;

import models.Event;
import models.ModelBase;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public abstract class NewsFeedEventListener implements PropertyChangeListener {



    protected void registerEvent(ModelBase source, Event event, PropertyChangeEvent propertyChangeEvent){
        event.propertyChangeEvent = propertyChangeEvent;
        source.eventsToCreateWhileSaving.add(event);
    }




}
