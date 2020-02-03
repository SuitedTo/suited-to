
package models.tables.job;

import enums.RoleValue;
import models.Category;
import models.Job;
import models.User;
import models.query.job.AccessibleJobHelper;
import models.tables.DefaultTableObjectTransformer;
import models.tables.FilteredCriteriaHelperTable;
import play.db.jpa.JPA;
import utils.CriteriaHelper.*;
import utils.ObjectTransformer;

import javax.persistence.criteria.*;
import java.util.Date;
import java.util.List;

public class JobTable extends FilteredCriteriaHelperTable {

    public JobTable() {
        super(new AccessibleJobHelper());

        RootTableKey job = (RootTableKey) getSoleEntityKey(Job.class);

        addColumn(job, "name");
        addColumn(job, "created", DefaultTableObjectTransformer.INSTANCE);
        addColumn(job, "id");

        includeInSearch(field(job, "name", String.class));
    }

    @Override
    public boolean canAccess(User u) {
        return u != null &&
                (u.company != null || u.hasRole(RoleValue.APP_ADMIN));
    }
}
