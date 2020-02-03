
package models.filter.question;

public class ByExcludeFromPrep extends QuestionFilter<Boolean> {

    @Override
    public String getAttributeName() {
        return "excludeFromPrep";
    }

    @Override
    protected String toString(Boolean attribute) {
        return "" + attribute;
    }

    @Override
    public Boolean fromString(String attributeStr) {
        return "true".equals(attributeStr);
    }
    
}
