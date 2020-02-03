package models.filter.user;

import enums.UserStatus;

public class ByStatus extends UserFilter<UserStatus> {

    @Override
    public String getAttributeName() {
        return "status";
    }

    @Override
    protected String toString(UserStatus status) {
        if (status == null) {
            return null;
        }
        return status.name();
    }

    @Override
    public UserStatus fromString(String status) {
        try {
            return UserStatus.valueOf(status.toUpperCase());
        } catch (Exception e) {
        }

        return null;
    }

}
