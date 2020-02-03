package models.tables.user;

import controllers.Security;
import enums.RoleValue;
import java.util.ArrayList;
import java.util.List;
import models.Company;
import models.User;
import models.filter.user.ByCompanyName;
import models.query.user.AccessibleUsersHelper;
import models.tables.FilteredCriteriaHelperTable;
import org.apache.commons.lang.StringUtils;
import play.i18n.Messages;
import utils.CriteriaHelper.RootTableKey;
import utils.CriteriaHelper.TableKey;
import utils.ObjectTransformer;

public class UserTable extends FilteredCriteriaHelperTable {

    public UserTable() {
        super(new AccessibleUsersHelper());
        
        RootTableKey root = (RootTableKey) getSoleEntityKey(User.class);
        TableKey company = join(root, "company");
        TableKey role = join(root, "roles");
        
        addColumn(root, "email");
        addColumn(root, "fullName", ObjectToMessage.INSTANCE);
        addColumn(root, "displayName");
        addColumn(root, "status", ObjectToString.INSTANCE);
        addColumn(root, "created", DateToFormattedDateString.INSTANCE);
        addColumn(role, RoleFormatter.INSTANCE);
        addColumn(company, CompanyNameOrNothing.INSTANCE);
        addColumn(company, "name");
        addColumn(root, "id");

        includeInSearch(field(root, "fullName", String.class));
        includeInSearch(field(root, "email", String.class));
        includeInSearch(field(root, "displayName", String.class));


        if (Security.connectedUser().hasRole(enums.RoleValue.APP_ADMIN)) {
            includeInSearch(field(company, "name", String.class));
        }
    }
    
    @Override
    public boolean canAccess(User u) {
        return (u != null) && 
                ((u.hasRole(RoleValue.COMPANY_ADMIN)) ||
                    u.hasRole(RoleValue.APP_ADMIN));
    }
    
    private static class RoleFormatter implements ObjectTransformer {
        
        public static final RoleFormatter INSTANCE = new RoleFormatter();

        public Object transform(Object input) {
            String result;
            
            if (input == null) {
                result = "";
            }
            else {
                RoleValue value = (RoleValue) input;

                result = Messages.get(value.getMessageKey());
            }
            
            return result;
        }
    }
    
    private static class CompanyNameOrNothing implements ObjectTransformer {
        
        public static final ObjectTransformer INSTANCE = 
                new CompanyNameOrNothing();
        
        public Object transform(Object input) {
            String result;
            
            if (input == null) {
                result = "";
            }
            else {
                result = ((Company) input).name;
            }
            
            return result;
        }
    }
}
