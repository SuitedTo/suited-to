package dto.prep;

import enums.prep.SkillLevel;
import models.prep.PrepJobCategory;

public class PrepJobCategoryDTO extends PrepDTO {
    public Long id;
    public String prepJobLevel;
    public PrepCategoryDTO prepCategory;
    public Boolean primaryCategory;
    public String difficulty;
    public String weight;

    public static PrepJobCategoryDTO fromPrepJobCategory(PrepJobCategory prepJobCategory) {
        if (prepJobCategory == null) {
            return null;
        }
        PrepJobCategoryDTO d = new PrepJobCategoryDTO();
        d.id = prepJobCategory.id;
        d.prepCategory = PrepCategoryDTO.fromPrepCategory(prepJobCategory.prepCategory);
        d.primaryCategory = prepJobCategory.primaryCategory;
        d.difficulty = prepJobCategory.difficulty.toString();
        d.weight = prepJobCategory.weight.toString();
        d.prepJobLevel = prepJobCategory.prepJobLevel.toString();

        return d;
    }
}
