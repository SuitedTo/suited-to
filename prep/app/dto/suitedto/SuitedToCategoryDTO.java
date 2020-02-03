package dto.suitedto;

import models.Category;

import com.google.gson.Gson;

public class SuitedToCategoryDTO extends SuitedToDTO {

	public Long id;
	
	public String name;
	
	public String companyName;
	
	public static SuitedToCategoryDTO fromJson(String json){
		try {
	        Gson gson = new Gson();
	        SuitedToCategoryDTO sc = gson.fromJson(json, SuitedToCategoryDTO.class);
	        return sc;
	    }
	    catch(Exception ex) {
	    	ex.printStackTrace();
	        return null;
	    }
	}
	
	public static SuitedToCategoryDTO fromSuitedToCategory(Category category){
		if(category == null){
			return null;
		}
		SuitedToCategoryDTO sc = new SuitedToCategoryDTO();
		sc.id = category.id;
		sc.companyName = category.companyName;
		sc.name = category.name;
		return sc;
	}
	
}
