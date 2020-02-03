package enums;

import play.i18n.Messages;

import java.util.Arrays;
import java.util.List;

public enum QuestionStatus {
    OUT_FOR_REVIEW, RETURNED_TO_SUBMITTER, ACCEPTED, WITHDRAWN, PRIVATE, QUICK,
    AWAITING_CATEGORY, AWAITING_COMPLETION;

    @Override
    public String toString() {
        return Messages.get(getClass().getName() + "." + name());
    }

    /**
     * Indicates that Questions with this status are either ACCEPTED public questions or are in some phase of the
     * Public question approval process. It doesn't necessarily mean that every user of the system will be able to
     * see the question but it does mean that the creator's intent is that this question should become part of the
     * public pool once the review process is complete
     * @return whether this QuestionStatus indicates a question that is publicly visible
     */
    public boolean isPubliclyVisible(){
        return publiclyVisibleStatuses().contains(this);
    }

    public static List<QuestionStatus> publiclyVisibleStatuses(){
        return Arrays.asList(OUT_FOR_REVIEW, RETURNED_TO_SUBMITTER, ACCEPTED, WITHDRAWN, AWAITING_COMPLETION, AWAITING_CATEGORY);
    }
}
