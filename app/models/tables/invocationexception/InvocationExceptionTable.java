package models.tables.invocationexception;

import models.InvocationException;
import models.User;
import models.query.invocationexception.InvocationExceptionQueryHelper;
import models.tables.FilteredCriteriaHelperTable;
import utils.CriteriaHelper.RootTableKey;
import enums.RoleValue;

public class InvocationExceptionTable  extends FilteredCriteriaHelperTable {

    public InvocationExceptionTable() {
        super(new InvocationExceptionQueryHelper());
        RootTableKey invocationException = (RootTableKey) getSoleEntityKey(InvocationException.class);
        
        addColumn(invocationException, "created");
        addColumn(invocationException, "identifier");
        
        
        includeInSearch(field(invocationException, "identifier", String.class));
    }

    @Override
    public boolean canAccess(User u) {
        return u != null && u.hasRole(RoleValue.APP_ADMIN);
    }
}
