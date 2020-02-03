package models.filter.question;

import enums.Timing;

public class ByTime extends QuestionFilter<Timing> {

    @Override
    public String getAttributeName() {
        return "time";
    }

    @Override
    protected String toString(Timing attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name();
    }

    @Override
    public Timing fromString(String attributeStr) {
        try {
            return Timing.valueOf(attributeStr.toUpperCase());
        } catch (Exception e) {
        }

        return null;
    }

}
