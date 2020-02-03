package dto.prep;

import models.prep.PrepCategory;

public class PrepCategoryDTO extends PrepDTO {

	public Long id;
	public String name;
	public String companyName;

	public static PrepCategoryDTO fromPrepCategory(PrepCategory category) {
		if (category == null) {
			return null;
		}
		PrepCategoryDTO qd = new PrepCategoryDTO();
		qd.id = category.id;
		qd.name = category.name;
		qd.companyName = category.companyName;
		return qd;
	}
}
