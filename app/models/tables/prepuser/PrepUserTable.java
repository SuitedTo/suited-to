package models.tables.prepuser;

import enums.RoleValue;
import models.User;
import models.prep.PrepUser;
import models.query.prepuser.PrepUserQueryHelper;
import models.tables.FilteredCriteriaHelperTable;
import play.db.jpa.JPA;
import play.i18n.Messages;
import utils.CriteriaHelper.RootTableKey;
import utils.ObjectTransformer;

import javax.persistence.criteria.CriteriaBuilder;

/**
 * Created with IntelliJ IDEA.
 * User: phutchinson
 * Date: 3/4/13
 * Time: 10:18 AM
 * To change this template use File | Settings | File Templates.
 */
public class PrepUserTable extends FilteredCriteriaHelperTable {

    public PrepUserTable() {
        super(new PrepUserQueryHelper());

        RootTableKey prepUser = (RootTableKey) getSoleEntityKey(PrepUser.class);

        addColumn(prepUser, "id");
        addColumn(prepUser, "email");
        addColumn(prepUser, "firstName");
        addColumn(prepUser, "externalAuthProvider", EnumToString.INSTANCE);
        addColumn(prepUser, "externalAuthProviderId");
        addColumn(prepUser, "id"); //send the id again b/c datatables expects a value for every column

        includeInSearch(field(prepUser, "firstName", String.class));
        includeInSearch(field(prepUser, "email", String.class));
    }

    @Override
    public boolean canAccess(User u) {
        return (u != null) && (u.hasRole((RoleValue.APP_ADMIN)));
    }



    private static class EnumToString implements ObjectTransformer {

        public static final ObjectTransformer INSTANCE = new EnumToString();

        public Object transform(Object input) {
            String temp = "", msg="";
            if(input != null){
                temp = input.toString();
                msg = Messages.get(temp);
            }
            temp.toString();
            msg.toString();
            return input != null ? Messages.get(input.toString()) : "";
        }
    }

}


