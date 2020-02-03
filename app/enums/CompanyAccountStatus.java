package enums;

import play.i18n.Messages;

public enum CompanyAccountStatus {
    ACTIVE, DISABLED;

    @Override
    public String toString() {
        return Messages.get(getClass().getName() + "." + name());
    }
}
