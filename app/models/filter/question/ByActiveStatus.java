
package models.filter.question;

public class ByActiveStatus extends QuestionFilter<Boolean> {

    @Override
    public String getAttributeName() {
        return "active";
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
