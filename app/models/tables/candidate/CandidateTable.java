package models.tables.candidate;

import javax.persistence.criteria.CriteriaBuilder;

import play.db.jpa.JPA;
import enums.RoleValue;
import models.Candidate;
import models.User;
import models.query.candidate.AccessibleCandidateHelper;
import models.tables.DefaultTableObjectTransformer;
import models.tables.FilteredCriteriaHelperTable;
import utils.CriteriaHelper.*;

/**
 * Created with IntelliJ IDEA.
 * User: perryspyropoulos
 * Date: 8/13/13
 * Time: 11:48 AM
 * To change this template use File | Settings | File Templates.
 */
public class CandidateTable extends FilteredCriteriaHelperTable {

    public CandidateTable() {
        super(new AccessibleCandidateHelper());
        
        CriteriaBuilder builder = JPA.em().getCriteriaBuilder();

        RootTableKey candidate = (RootTableKey) getSoleEntityKey(Candidate.class);
        
        TableKey candidateStatus = join(candidate, "candidateJobStatuses");
        
        TableKey job = join(candidateStatus,"job");
        
        TableKey status = join(candidateStatus,"companyJobStatus");
        
        addGroup(field(candidate, "id"));

        addColumn(candidate, "id");
        addColumn(candidate, "name");
        addColumn(candidate, "created", DefaultTableObjectTransformer.INSTANCE);
        addColumn(builder.function("group_concat", String.class, field(job,"name")));
        addColumn(builder.function("group_concat", String.class, field(status,"name")));

//        includeInSearch(field(candidate, "name", String.class));

        includeInDynamicSearch("id",field(candidate, "id", String.class));
        includeInDynamicSearch("name",field(candidate, "name", String.class));
        includeInDynamicSearch("created",field(candidate, "created", String.class));
        includeInDynamicSearch("job",field(job, "name", String.class));
        includeInDynamicSearch("jobStatus", field(status, "name", String.class));
    }

    @Override
    public boolean canAccess(User user) {
        return (user != null) && (user.company != null || user.hasRole(RoleValue.APP_ADMIN));
    }

}
