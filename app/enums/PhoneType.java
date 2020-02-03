package enums;

import play.i18n.Messages;

public enum PhoneType {
    OTHER, HOME, WORK, MOBILE;


    @Override
    public String toString() {
        return Messages.get(getClass().getName() + "." + name());
    }
}
