package enums;

import play.i18n.Messages;

public enum Difficulty {
    EASY, MEDIUM, HARD;

    public String getLabel() {
        return play.i18n.Messages.get(name());
    }

    @Override
    public String toString() {
        return Messages.get(getClass().getName() + "." + name());
    }
}
