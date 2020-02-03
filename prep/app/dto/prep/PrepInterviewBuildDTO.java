package dto.prep;

import models.prep.PrepInterviewBuild;

import com.google.gson.Gson;


public class PrepInterviewBuildDTO extends PrepDTO{
	public Long id;
	
	public Long interviewId;
    
    
    public static PrepInterviewBuildDTO fromPrepInterviewBuild(PrepInterviewBuild build){
    	if(build == null){
    		return null;
    	}
    	PrepInterviewBuildDTO dto = new PrepInterviewBuildDTO();
    	
    	dto.id = build.id;
    	
    	if(build.interview != null){
    		dto.interviewId = build.interview.id;
    	}
    	
    	return dto;
    }
    
    public static PrepInterviewBuildDTO fromJson(String json) {
        try {
            Gson gson = new Gson();
            PrepInterviewBuildDTO pc = gson.fromJson(json, PrepInterviewBuildDTO.class);
            return pc;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
