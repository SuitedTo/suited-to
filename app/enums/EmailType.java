package enums;

import play.i18n.Messages;

public enum EmailType {
    OTHER, HOME, WORK;

    @Override
    public String toString() {
        return Messages.get(getClass().getName() + "." + name());
    }
}
