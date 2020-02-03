package enums.prep;

import play.i18n.Messages;

public enum Contribution {
	SMALL, MEDIUM, LARGE;

    @Override
    public String toString() {
    	return name();//Messages.get(getClass().getName() + "." + name());
    }
}