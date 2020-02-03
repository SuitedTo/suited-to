package enums;

import play.i18n.Messages;

public enum ProInterviewerRequestStatus {
    SUBMITTED, ACCEPTED, DECLINED;

    @Override
    public String toString() {
        return Messages.get(getClass().getName() + "." + name());
    }
}
