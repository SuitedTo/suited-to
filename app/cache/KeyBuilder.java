package cache;

import play.mvc.Scope.Session;
import controllers.Application;


/**
 * 
 * @author joel
 *
 */
public class KeyBuilder{

	public enum KeyType{GLOBAL, INSTANCE, SESSION};
	
	private final KeyType type;
	private final String name;
	private final String instanceId;
	
	private KeyBuilder(KeyType type, String name, String instanceId) {
		if((type == null) || (name == null)){
			throw new IllegalArgumentException();
		}
		this.type = type;
		this.name = name;
		this.instanceId = instanceId;
	}
	
	private KeyBuilder(KeyType type, String name) {
		this(type, name, "?");
	}
	
	private KeyBuilder(String name, String instanceId) {
		this(KeyType.INSTANCE, name, instanceId);
	}
	
	public static String buildGlobalKey(String name){
		
		return new KeyBuilder(KeyType.GLOBAL, name).asString();
	}
	
	public static String buildInstanceKey(String name){
		
		return buildInstanceKey(Application.getInstanceId(),name);
	}
	
	public static String buildInstanceKey(String instanceId, String name){
		
		return new KeyBuilder(name, instanceId).asString();
	}
	
	public static String buildSessionKey(String name){
		
		return new KeyBuilder(KeyType.SESSION, name).asString();
	}
	
	private String asString(){
		StringBuilder key = new StringBuilder();
		switch(type){
			case INSTANCE:
				key.append("I").append(instanceId);
			break;
			case SESSION:
				key.append("S").append(Session.current().getId());
				break;
			default:
				key.append("G");
				break;
		}
		
		key.append(name);
		return key.toString();
	}
	
	
}
