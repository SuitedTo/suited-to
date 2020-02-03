package models.filter.question;

import enums.QuestionStatus;
import play.i18n.Messages;

import java.util.ArrayList;
import java.util.List;

/**
 * Filter questions by review status.
 *
 * @author joel
 */
public class ByStatus extends QuestionFilter<QuestionStatus> {

    @Override
    public String getAttributeName() {
        return "status";
    }

    protected List<String> interpret(String key) {
        List<String> result = new ArrayList<String>();

        QuestionStatus[] values = QuestionStatus.values();
        for (QuestionStatus value : values) {
            String msg = Messages.get(QuestionStatus.class.getName() + "." + value.name());
            if ((msg != null) && msg.equalsIgnoreCase(key)) {
                result.add(value.name());
            }
        }
        return result;
    }

    @Override
    protected String toString(QuestionStatus rs) {
        if (rs == null) {
            return null;
        }
        return rs.name();
    }

    @Override
    public QuestionStatus fromString(String rs) {
        try {
            return QuestionStatus.valueOf(rs.toUpperCase());
        } catch (Exception e) {
        }

        return null;
    }

}
