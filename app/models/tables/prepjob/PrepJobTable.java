package models.tables.prepjob;

import enums.RoleValue;
import models.User;
import models.prep.PrepJob;
import models.prep.PrepJobName;
import models.query.prepjob.AccessiblePrepJobHelper;
import models.query.prepjob.PrepJobQueryHelper;
import models.tables.FilteredCriteriaHelperTable;
import play.db.jpa.JPA;
import utils.CriteriaHelper.*;
import utils.ObjectTransformer;

import javax.persistence.criteria.*;
import java.util.List;

public class PrepJobTable extends FilteredCriteriaHelperTable {

    public PrepJobTable() {
        super(new AccessiblePrepJobHelper());
        CriteriaBuilder builder = JPA.em().getCriteriaBuilder();
        RootTableKey prepJob = (RootTableKey) getSoleEntityKey(PrepJob.class);
        TableKey jobName = join(prepJob, "primaryName");
        
        addColumn(prepJob, "id");
        addColumn(jobName, "name");
        
        includeInSearch(field(jobName, "name", String.class));
    }

    @Override
    public boolean canAccess(User u) {
        return u != null && u.hasRole(RoleValue.APP_ADMIN);
    }

    private static class PrepJobNameToString implements ObjectTransformer {

        public static final ObjectTransformer INSTANCE = new PrepJobNameToString();

        public Object transform(Object input) {

            String result = "";

            if(input != null) {
                result += ((PrepJobName) input).name;
            }
            return result;
        }
    }

    private static class PrepJobCategoriesToString implements ObjectTransformer {

        public static final ObjectTransformer INSTANCE = new PrepJobCategoriesToString();

        // Mike Todo: finish implementing
        public Object transform(Object input) {
            return "";
        }
    }
}
