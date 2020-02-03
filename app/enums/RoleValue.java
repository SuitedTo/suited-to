package enums;

import models.deadbolt.Role;
import play.i18n.Messages;

public enum RoleValue implements Role {
    APP_ADMIN, COMPANY_ADMIN, QUESTION_ENTRY, QUESTION_APPROVER, PREP, CONTRIBUTOR;


    @Override
    public String getRoleName() {
        return name();
    }

    public String getMessageKey() {
        return getClass().getName() + "." + name();
    }
    
    public static final String APP_ADMIN_STRING = "APP_ADMIN";
    public static final String COMPANY_ADMIN_STRING = "COMPANY_ADMIN";
    // A non-admin, standard user that belongs to a company
    public static final String QUESTION_ENTRY_STRING = "QUESTION_ENTRY";
    public static final String QUESTION_APPROVER_STRING = "QUESTION_APPROVER";
    public static final String PREP_STRING = "PREP";
    public static final String CONTRIBUTOR_STRING = "CONTRIBUTOR";

}
