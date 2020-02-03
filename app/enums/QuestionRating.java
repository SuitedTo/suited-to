package enums;

import play.i18n.Messages;

/**
 * Created with IntelliJ IDEA.
 * User: swilly
 * Date: 2013/9/5
 * Time: 9:54
 * To change this template use File | Settings | File Templates.
 */
public enum QuestionRating {
    EXCELLENT(2), VERY_GOOD(1), GOOD(0), FAIR(-1), POOR(-2);

    private Integer rating;

    private QuestionRating(Integer rating) {
        this.rating = rating;
    }

    public String getLabel() {
        return play.i18n.Messages.get(name());
    }

    @Override
    public String toString() {
        return Messages.get(getClass().getName() + "." + name());
    }

    public Integer toInteger() {
        return rating;
    }

    public static QuestionRating fromLabel(String rating) {
        QuestionRating result = null;

        for (QuestionRating questionRating : QuestionRating.values()) {
            if (questionRating.getLabel().equals(rating)) {
                result = questionRating;
                break;
            }
        }

        return result;
    }
}
