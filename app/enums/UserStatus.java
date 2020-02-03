package enums;

import play.i18n.Messages;

import java.util.Arrays;
import java.util.List;

public enum UserStatus {
    PENDING(false), ACTIVE(true), DEACTIVATED(false), INVITATION_WITHDRAWN(false);

    public boolean isAllowedToLogin;

    private UserStatus(boolean allowedToLogin) {
        isAllowedToLogin = allowedToLogin;
    }

    @Override
    public String toString() {
        return Messages.get(getClass().getName() + "." + name());
    }

    /**
     * A list of the statuses that are considered to be "in use".  Currently that includes ACTIVE and PENDING
     * @return List of UserStatus
     */
    public static List<UserStatus> getStatusesConsideredInUse(){
        return Arrays.asList(ACTIVE, PENDING);
    }
}
