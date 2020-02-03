package exceptions;

import java.io.PrintWriter;
import java.io.StringWriter;

import controllers.Security;
import models.InvocationException;
import models.User;
import play.PlayPlugin;
import play.exceptions.JavaExecutionException;
import play.jobs.Job;

public class InvocationExceptionLogger extends PlayPlugin{

	/**
     * Called if an exception occured during the invocation.
     * @param e The catched exception.
     */
    public void onInvocationException(Throwable e) {
    	if(e instanceof JavaExecutionException){
    		final JavaExecutionException jee = (JavaExecutionException)e;
    		
    		new Job(){
    			public void doJob(){
    				InvocationException ie = new models.InvocationException();
    	    		ie.identifier = jee.getId();
    	    		ie.message = jee.getErrorDescription() + "<br>";
    	    		if(jee.getCause() != null){
    	    			ie.message += "-----------------------<br>";
    	    			StringWriter sw = new StringWriter();
    	    	        PrintWriter out = new PrintWriter(sw);
    	    			jee.getCause().printStackTrace(out);
    	    			ie.message += sw.toString().replaceAll("\n", "<br>");
    	    		}
    	    		
    				ie.save();
    			}
    		}.now();
    		
    	}
    }
}
