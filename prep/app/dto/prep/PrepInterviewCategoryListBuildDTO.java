package dto.prep;

import models.prep.PrepInterviewCategory;
import models.prep.PrepInterviewCategoryList;
import models.prep.PrepInterviewCategoryListBuild;

import com.google.gson.Gson;

public class PrepInterviewCategoryListBuildDTO extends PrepDTO{
	
	public Long id;

	public Long categoryListId; 

	public static PrepInterviewCategoryListBuildDTO fromPrepInterviewCategoryListBuild(PrepInterviewCategoryListBuild build){
		if(build == null){
			return null;
		}
		PrepInterviewCategoryListBuildDTO dto = new PrepInterviewCategoryListBuildDTO();
		
		dto.id = build.id;

		PrepInterviewCategoryList categoryList = build.categoryList;
		if(categoryList != null){
			dto.categoryListId = categoryList.id;
		}
		return dto;
	}

	public static PrepInterviewCategoryListBuildDTO fromJson(String json) {
		try {
			Gson gson = new Gson();
			PrepInterviewCategoryListBuildDTO pc = gson.fromJson(json, PrepInterviewCategoryListBuildDTO.class);
			return pc;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
}
