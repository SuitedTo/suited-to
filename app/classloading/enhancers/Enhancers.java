package classloading.enhancers;

import models.query.EntityQueries;
import play.Play;
import play.PlayPlugin;
import play.classloading.ApplicationClasses.ApplicationClass;

/**
 * 
 * Plugin to call our enhancers. Doing it the way they do it
 * in the JPA plugin.
 * 
 * 
 * @author joel
 *
 */
public class Enhancers extends PlayPlugin{

	@Override
    public void onApplicationStart() {
		EntityQueries.instance = new EntityQueries();
	}
	
	/* (non-Javadoc)
	 * @see play.PlayPlugin#enhance(play.classloading.ApplicationClasses.ApplicationClass)
	 */
	@Override
    public void enhance(ApplicationClass applicationClass) throws Exception {
		String thisPackage = getClass().getName().substring(0, getClass().getName().indexOf(getClass().getSimpleName()));
		if((applicationClass != null) && (applicationClass.name.startsWith(thisPackage))){
			return;
		}
		
		//enhance models...
		new ModelEnhancer().enhanceThisClass(applicationClass);
		
		new ControllerEnhancer().enhanceThisClass(applicationClass);
		
		//enhance listeners...
		new PropertyChangeListenerEnhancer().enhanceThisClass(applicationClass);
		
		new PublisherEnhancer().enhanceThisClass(applicationClass);
	}
}
