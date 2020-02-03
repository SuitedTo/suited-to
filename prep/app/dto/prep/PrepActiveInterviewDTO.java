package dto.prep;

import com.google.gson.Gson;

public class PrepActiveInterviewDTO extends PrepDTO{
	
	public String name;
	
	public static PrepActiveInterviewDTO fromJson(String json){
		try {
	        Gson gson = new Gson();
	        PrepActiveInterviewDTO sai = gson.fromJson(json, PrepActiveInterviewDTO.class);
	        return sai;
	    }
	    catch(Exception ex) {
	    	ex.printStackTrace();
	        return null;
	    }
	}
}
