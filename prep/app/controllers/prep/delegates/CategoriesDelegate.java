package controllers.prep.delegates;

import java.util.ArrayList;
import java.util.List;

import dto.suitedto.SuitedToCategoryDTO;
import models.Category;

public class CategoriesDelegate {
	
	public static List<SuitedToCategoryDTO> list(){
		List<SuitedToCategoryDTO> results  = new ArrayList<SuitedToCategoryDTO>();
		List<Category> categories = Category.find("byIsAvailableExternally", true).fetch();
		for(Category c : categories){
			results.add(SuitedToCategoryDTO.fromSuitedToCategory(c));
		}
		return results;
	}
	
	public static SuitedToCategoryDTO get(Long id){
		
		if(id == null){
			return null;
		}
		return SuitedToCategoryDTO.fromSuitedToCategory((Category) Category.findById(id));
	}
}
