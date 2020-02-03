package dto.suitedto;

import models.ActiveInterview;

import com.google.gson.Gson;

public class SuitedToActiveInterviewDTO extends SuitedToDTO {
	
	public long id;
	
	public String name;
	
	public static SuitedToActiveInterviewDTO fromJson(String json){
		try {
	        Gson gson = new Gson();
	        SuitedToActiveInterviewDTO sai = gson.fromJson(json, SuitedToActiveInterviewDTO.class);
	        return sai;
	    }
	    catch(Exception ex) {
	    	ex.printStackTrace();
	        return null;
	    }
	}
	
	public static SuitedToActiveInterviewDTO fromSuitedToActiveInterview(ActiveInterview interview){
		if(interview == null){
			return null;
		}
		SuitedToActiveInterviewDTO dto = new SuitedToActiveInterviewDTO();
		dto.id = interview.id;
		dto.name = interview.name;
		return dto;
		
	}

}
