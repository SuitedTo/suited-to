package scheduler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import controllers.Application;
import play.Logger;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Callable;

public abstract class Task<V> implements Callable<V>{
	protected final TaskExecutionContext context;
	protected List<String> timeZones = new ArrayList<String>();
    private static final Gson JSON_SERIALIZER_FOR_LOGS = new GsonBuilder().setPrettyPrinting().create();

    public Task(){
		context = null;
	}
	
	public Task(TaskExecutionContext context){
		this.context = context;
		List<Calendar> executionTimes = context.getExecutionTimes();
		if(executionTimes != null){
			for(Calendar c : executionTimes){
				timeZones.add(c.getTimeZone().getID());
			}
		}
	}
	
	V doTaskWithResult(){
		doTask();
		return null;
	}
	
	public void doTask(){
	    logTaskExecution();
	}
	
	protected boolean timeZoneMatch(String timeZone){
		return timeZones.contains(timeZone);
	}
	
	protected Object getArg(String name){
		TaskArgs args = context.getTriggerDetail().getTaskArgs();
		if(args == null){
			return null;
		}
		return args.getArg(name);
	}

	@Override
	public final V call() throws Exception {
		return doTaskWithResult();
	}

    public void logTaskExecution(){
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("Running ")
        .append(this.getClass().getName());
        TaskArgs args = context.getTriggerDetail().getTaskArgs();
        if(args != null){
        	logBuilder.append(":")
        	.append(args.toXML().hashCode());
        }
        logBuilder.append(" on Instance: ")
        .append(Application.getInstanceId());
        try {
        	logBuilder.append(" [")
        	.append(InetAddress.getLocalHost().getHostAddress())
        	.append("]");
		} catch (UnknownHostException e) {
		}
//        logBuilder.append(" with arguments: ")
//        .append("\n")
//        .append(JSON_SERIALIZER_FOR_LOGS.toJson(this.context.getTriggerDetail().getTaskArgs()));

        Logger.info(logBuilder.toString());
    }

}
