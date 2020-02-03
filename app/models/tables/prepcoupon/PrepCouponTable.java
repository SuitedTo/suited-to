package models.tables.prepcoupon;

import models.User;
import models.prep.PrepCoupon;
import models.prep.PrepJobName;
import models.query.prepcoupon.AccessiblePrepCoupons;
import models.tables.FilteredCriteriaHelperTable;
import play.db.jpa.JPA;
import utils.CriteriaHelper.RootTableKey;
import utils.ObjectTransformer;

import javax.persistence.criteria.CriteriaBuilder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PrepCouponTable extends FilteredCriteriaHelperTable {

    public PrepCouponTable() {
        super(new AccessiblePrepCoupons());
        CriteriaBuilder builder = JPA.em().getCriteriaBuilder();
        RootTableKey root = (RootTableKey) getSoleEntityKey(PrepCoupon.class);

        addColumn(root, "id");
        addColumn(root, "name");
        addColumn(root, "payPeriods", PrepCouponCriteriaToString.INSTANCE);
        addColumn(root, "maxUses", PrepCouponCriteriaToString.INSTANCE);
        addColumn(root, "currentUses");
        addColumn(root, "discount");
        addColumn(root, "id"); //send the id again b/c datatables expects a value for every column

        includeInSearch(field(root, "name", String.class));
    }

    @Override
    public boolean canAccess(User u) {
        return u != null;
    }

    private static class PrepCouponCriteriaToString implements ObjectTransformer {

        public static final ObjectTransformer INSTANCE = new PrepCouponCriteriaToString();

        public Object transform(Object input) {

            String result = "(none)";

            if(input != null) {
                result = input.toString();
            }
            return result;
        }
    }
}
