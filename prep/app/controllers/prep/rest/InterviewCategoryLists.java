package controllers.prep.rest;

import models.prep.PrepCategory;
import models.prep.PrepInterviewCategoryList;

public class InterviewCategoryLists extends PrepController{

	public static void get(Long id) {
        if (id == null) {
            emptyObject();
        }
        PrepInterviewCategoryList categoryList = PrepInterviewCategoryList.find.byId(id);
        if (categoryList != null) {
            renderRefinedJSON(categoryList.toJsonObject());
        }
        emptyObject();
    }
}
