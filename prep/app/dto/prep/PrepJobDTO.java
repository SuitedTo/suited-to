package dto.prep;

import models.prep.PrepJob;
import models.prep.PrepJobCategory;
import models.prep.PrepJobName;

import java.util.ArrayList;
import java.util.List;

public class PrepJobDTO extends PrepDTO {
    public Long id;
    public PrepJobNameDTO primaryName;
    public List<PrepJobNameDTO> prepJobNames;
    public List<PrepJobCategoryDTO> prepJobCategories;

    public static PrepJobDTO fromPrepJob(PrepJob prepJob) {
        if (prepJob == null) {
            return null;
        }
        PrepJobDTO d = new PrepJobDTO();
        d.id = prepJob.id;
        d.primaryName = PrepJobNameDTO.fromPrepJobName(prepJob.primaryName);

        if(prepJob.prepJobNames != null){
            d.prepJobNames = new ArrayList<PrepJobNameDTO>();
            for(PrepJobName p : prepJob.prepJobNames){
                PrepJobNameDTO pjnd = PrepJobNameDTO.fromPrepJobName(p);
                if(pjnd != null){
                    d.prepJobNames.add(pjnd);
                }
            }
        }

        if(prepJob.prepJobCategories != null){
            d.prepJobCategories = new ArrayList<PrepJobCategoryDTO>();
            for(PrepJobCategory p : prepJob.prepJobCategories){
                PrepJobCategoryDTO pjcd = PrepJobCategoryDTO.fromPrepJobCategory(p);
                if(pjcd != null){
                    d.prepJobCategories.add(pjcd);
                }
            }
        }

        return d;
    }
}
