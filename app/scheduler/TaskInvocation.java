package scheduler;

import play.Invoker;
import play.Invoker.InvocationContext;


/**
 * An Invocation in something to run in a Play! context
 */
public abstract class TaskInvocation extends Invoker.Invocation{

    protected final TaskExecutionContext context;
	
	
	public TaskInvocation(TaskExecutionContext context){
		this.context = context;
	}
    
    public InvocationContext getInvocationContext(){
    	return new InvocationContext("Scheduled Task", this.getClass().getAnnotations());
    }

    /**
     * Things to do after an Invocation.
     * (if the Invocation code has not thrown any exception)
     */
    public void after() {
        super.after();
        finished(true);
        
    }
    
    public void finished(boolean success){
    	if(context != null){
        	//Context execution times will be null if the task is run directly via TaskScheduler.runTask and
            //in this case we don't set previousExecution
        	if(context.getExecutionTimes() != null){
        		context.getTriggerDetail().getTrigger().previousExecution = context.getExecutionTimes().get(0).getTime();
        	}
        	
        	//If the task is run directly via TaskScheduler.runTask then the task was injected directly into the
        	//execution pool and there's no scheduler
        	if(context.getScheduler() != null){
        		context.getScheduler().finished(context.getTriggerDetail(), success);
        	}
        }
    }
}
