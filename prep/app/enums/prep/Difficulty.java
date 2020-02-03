package enums.prep;

import play.i18n.Messages;


public enum Difficulty {
    EASY, MEDIUM, HARD;

    @Override
    public String toString() {
        return name();//Messages.get(getClass().getName() + "." + name());
    }
}

