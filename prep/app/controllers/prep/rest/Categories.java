package controllers.prep.rest;

import java.util.List;

import com.google.gson.JsonArray;

import controllers.prep.delegates.CategoriesDelegate;
import dto.prep.PrepCategoryDTO;
import dto.suitedto.SuitedToCategoryDTO;
import models.prep.PrepCategory;

public class Categories extends PrepController {
	/**
	 * For now we're getting all categories from SuitedTo and adding an ID for the ones
	 * that are also in prep. So a SuitedTo Category that isn't in prep will appear in the
	 * returned list as a PrepCategory that has not been saved Id (has no id).
	 * 
	 * Obviously this isn't so efficient. Thinking about supporting a special filter/param so the
	 * client can ask for only the categories that have been saved or only the ones that have
	 * not been saved. This would allow us to optimize this method and either call on the
	 * SuitedTo API or query the prep db but not both.
	 * 
	 * 
	 * 
	 * @param filters
	 */
	public static void list(String filters){
		
		//not dealing with filters yet
		
		List<SuitedToCategoryDTO> dtos = CategoriesDelegate.list();
		if(dtos == null){
			emptyArray();
		}
		JsonArray result = new JsonArray();
		for(SuitedToCategoryDTO dto : dtos){
			
			PrepCategoryDTO pdto;
			PrepCategory pc = PrepCategory.find.where().eq("category.id", "" + dto.id).findUnique();
			if(pc != null){
				pdto = PrepCategoryDTO.fromPrepCategory(pc);
			} else {
				pdto = new PrepCategoryDTO();
				pdto.name = dto.name;
				pdto.companyName = dto.companyName;
			}
			result.add(pdto.toJsonTree());
		}
		renderRefinedJSON(result);
	}

    public static void get(Long id) {
        if (id == null) {
            emptyObject();
        }
        PrepCategory prepCategory = PrepCategory.find.byId(id);
        if (prepCategory != null) {
            renderRefinedJSON(prepCategory.toJsonObject());
        }
        emptyObject();
    }
}
