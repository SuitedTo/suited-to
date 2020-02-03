package cache.prep;

import dto.suitedto.SuitedToDTO;
import play.cache.Cache;
import play.utils.Java;

public class SuitedToDTOCache {

	public static <T extends SuitedToDTO> void add(String identifier, T dto){
		Cache.add("#" + identifier, dto.getClass().getName() + "#" + dto.toJson(), "10s");
	}
	
	public static <T extends SuitedToDTO> T get(String identifier){
		String entry = (String) Cache.get("#" + identifier);
		if(entry == null){
			return null;
		}
		
		try {
			String className = entry.substring(0, entry.indexOf('#'));
			String json = entry.substring(entry.indexOf('#') + 1);
			return (T)Java.invokeStatic(Class.forName(className), "fromJson", json);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
