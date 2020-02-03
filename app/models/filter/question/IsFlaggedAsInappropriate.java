package models.filter.question;

/**
 * Created with IntelliJ IDEA.
 * User: michellerenert
 * Date: 8/14/12
 * Time: 9:52 AM
 * To change this template use File | Settings | File Templates.
 */
public class IsFlaggedAsInappropriate extends QuestionFilter<Boolean> {
    @Override
    public String getAttributeName() {
        return "flaggedAsInappropriate";
    }

    @Override
    protected String toString(Boolean isFlaggedAsInappropriate) {
        if (isFlaggedAsInappropriate == null) {
            return null;
        }
        return isFlaggedAsInappropriate.toString();
    }

    @Override
    public Boolean fromString(String isFlaggedAsInappropriate) {
        try {
            return new Boolean(isFlaggedAsInappropriate);
        } catch (Exception e) {
            return null;
        }
    }


}
