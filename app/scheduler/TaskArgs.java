package scheduler;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

public class TaskArgs {
	private static XStream xstream = new XStream(new StaxDriver());
	Map<String, Object> args = new Hashtable<String, Object>();
	
	public TaskArgs(){}
	
	public TaskArgs(Map<String, Object> args){
		this.args = args;
	}

	public Object getArg(String name){
		return args.get(name);
	}
	
	public void add(String name, Object value){
		args.put(name,value);
	}
	
	public String toXML(){
		return xstream.toXML(args);
	}
	
	public static TaskArgs fromXML(String xml){
		Object args = xstream.fromXML(xml);
		if(args != null){
			return new TaskArgs((Map)args);
		}
		return null;
	}
}
