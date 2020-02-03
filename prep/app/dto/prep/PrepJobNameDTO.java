package dto.prep;

import models.prep.PrepJobName;

public class PrepJobNameDTO extends PrepDTO {
    public Long id;
    public String name;

    public static PrepJobNameDTO fromPrepJobName(PrepJobName prepJobName) {
        if (prepJobName == null) {
            return null;
        }
        PrepJobNameDTO d = new PrepJobNameDTO();
        d.id = prepJobName.id;
        d.name = prepJobName.name;

        return d;
    }
}
