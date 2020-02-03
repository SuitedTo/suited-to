package dto.suitedto;

import java.util.List;

import com.google.gson.Gson;

import enums.prep.Contribution;
import enums.prep.Difficulty;

public class SuitedToInterviewCategoryDTO extends SuitedToDTO {
	
	public SuitedToCategoryDTO category;
	
	public List<Difficulty> difficulties;
	
	public Contribution contribution;

	public static SuitedToInterviewCategoryDTO fromJson(String json){
		try {
	        Gson gson = new Gson();
	        SuitedToInterviewCategoryDTO ic = gson.fromJson(json, SuitedToInterviewCategoryDTO.class);
	        return ic;
	    }
	    catch(Exception ex) {
	    	ex.printStackTrace();
	        return null;
	    }
	}
}
