package enums.prep;

import play.i18n.Messages;

public enum SkillLevel {
    ENTRY, MID, SENIOR;

    @Override
    public String toString() {
    	return name();//Messages.get(getClass().getName() + "." + name());
    }
}
