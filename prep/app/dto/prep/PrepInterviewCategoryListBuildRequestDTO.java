package dto.prep;

import enums.prep.JobMatchType;
import enums.prep.SkillLevel;

public class PrepInterviewCategoryListBuildRequestDTO extends PrepDTO{

	public JobMatchType type;
	
	public Long entityId;
	
	public SkillLevel level;
}
