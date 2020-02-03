package dto.prep;

import enums.prep.Contribution;
import enums.prep.Difficulty;
import models.prep.PrepInterviewCategory;

public class PrepInterviewCategoryDTO extends PrepDTO{
	
	public PrepCategoryDTO category;
	
	public Difficulty difficulty;
	
	public Contribution contribution;

	public static PrepInterviewCategoryDTO fromPrepInterviewCategory(PrepInterviewCategory category) {
		if (category == null) {
			return null;
		}
		PrepInterviewCategoryDTO qd = new PrepInterviewCategoryDTO();
		qd.category = PrepCategoryDTO.fromPrepCategory(category.prepCategory);
		qd.difficulty = category.difficulty;
		qd.contribution = category.contribution;
		return qd;
	}
}
