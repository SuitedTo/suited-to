package dto.prep;

import java.util.ArrayList;
import java.util.List;

import models.prep.PrepInterviewCategory;
import models.prep.PrepInterviewCategoryList;

import com.google.gson.Gson;

public class PrepInterviewCategoryListDTO extends PrepDTO{

	public List<PrepInterviewCategoryDTO> categories = new ArrayList<PrepInterviewCategoryDTO>();
	public Long id; 
	public String name;

	public static PrepInterviewCategoryListDTO fromPrepInterviewCategoryList(PrepInterviewCategoryList categoryList){
    	if(categoryList == null){
    		return null;
    	}
    	PrepInterviewCategoryListDTO dto = new PrepInterviewCategoryListDTO();
    	
    	if(categoryList.categories != null){
    		dto.id = categoryList.id;
    		dto.name = categoryList.name;
    		
    		List<PrepInterviewCategory> categories = categoryList.categories;
    		for(PrepInterviewCategory category : categories){
    			dto.categories.add(PrepInterviewCategoryDTO.fromPrepInterviewCategory(category));
    		}
    	}
    	
    	return dto;
    }
    
    public static PrepInterviewCategoryListDTO fromJson(String json) {
        try {
            Gson gson = new Gson();
            PrepInterviewCategoryListDTO pc = gson.fromJson(json, PrepInterviewCategoryListDTO.class);
            return pc;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
