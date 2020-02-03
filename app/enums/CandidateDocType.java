package enums;

import play.i18n.Messages;

public enum CandidateDocType {
	RESUME,
	PORTFOLIO,
	PROJECT,
	OTHER;
	
    @Override
    public String toString() {
        return Messages.get(getClass().getName() + "." + name());
    }
}
