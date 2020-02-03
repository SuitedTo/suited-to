package models.tables.company;

import enums.RoleValue;
import enums.UserStatus;
import models.Company;
import models.User;
import models.query.company.CompanyQueryHelper;
import models.tables.FilteredCriteriaHelperTable;
import play.db.jpa.JPA;
import play.i18n.Messages;
import utils.CriteriaHelper.TableKey;
import utils.CriteriaHelper.RootTableKey;
import utils.ObjectTransformer;

import javax.persistence.criteria.CriteriaBuilder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Created with IntelliJ IDEA.
 * User: Mike Havens
 * Date: 7/26/12
 * Time: 4:10 PM
 * To change this template use File | Settings | File Templates.
 */
public class CompanyTable extends FilteredCriteriaHelperTable {

    private static final DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");

    public CompanyTable() {
        super(new CompanyQueryHelper());

        RootTableKey company = (RootTableKey) getSoleEntityKey(Company.class);
        TableKey users = join(company, "users");

        CriteriaBuilder builder = JPA.em().getCriteriaBuilder();
        getHelper().addCondition(builder.equal(field(users, "status"), UserStatus.ACTIVE));

        addGroup(field(users, "company"));

        addColumn(company, "id");
        addColumn(company, "name");
        addColumn(company, "contactName");
        addColumn(company, "accountType", EnumToString.INSTANCE);
        addColumn(company, "previousAccountType", EnumToString.INSTANCE);
        addColumn(company, "lastAccountChangeDate", DateToStringHandleNull.INSTANCE);
        addColumn(company, "created", DateToStringHandleNull.INSTANCE);
        addColumn(company, "status", EnumToString.INSTANCE);
        addColumn(company, "deactivationDate", DateToStringHandleNull.INSTANCE);
        addColumn(builder.count(field(users, "id")));

        includeInSearch(field(company, "name", String.class));
        includeInSearch(field(company, "contactName", String.class));
    }

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

    private static class DateToStringHandleNull implements ObjectTransformer {

        public static final ObjectTransformer INSTANCE = new DateToStringHandleNull();
        private static final DateToFormattedDateString dateToStr = new DateToFormattedDateString();

        public Object transform(Object input) {
            return input != null ? dateToStr.transform(input) : "";
        }

    }
}
