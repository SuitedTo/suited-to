package utils;

import play.db.jpa.JPA;

public class StatusUtil {

	private StatusUtil(){}
	
	public static String getDBStatus(){
		StringBuilder rtn = new StringBuilder();
		Object o = JPA.em().createNativeQuery("SHOW ENGINE INNODB STATUS;").getSingleResult();
		if(o != null){
			Object[] results = (Object[])o;
			for(Object result : results){
				rtn.append(String.valueOf(result)).append("\n");
			}
		}
		return rtn.toString();
	}
}
