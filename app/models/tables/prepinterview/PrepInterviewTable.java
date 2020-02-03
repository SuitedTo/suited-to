package models.tables.prepinterview;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaBuilder.Coalesce;

import play.db.jpa.JPA;
import models.User;
import models.prep.PrepInterview;
import models.query.prepinterview.PrepInterviewQueryHelper;
import models.query.prepjob.AccessiblePrepJobHelper;
import models.tables.DefaultTableObjectTransformer;
import models.tables.FilteredCriteriaHelperTable;
import utils.CriteriaHelper.RootTableKey;
import utils.CriteriaHelper.TableKey;
import enums.RoleValue;

public class PrepInterviewTable extends FilteredCriteriaHelperTable {

    public PrepInterviewTable() {
        super(new PrepInterviewQueryHelper());
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        RootTableKey prepInterview = (RootTableKey) getSoleEntityKey(PrepInterview.class);
        
        TableKey owner = join(prepInterview, "owner");
        
        TableKey question = join(prepInterview, "questions");
        
        addGroup(field(prepInterview,"id"));
        
        addColumn(prepInterview, "id");
        addColumn(prepInterview, "name");
        
        Coalesce<String> ownerKey = cb.coalesce();
        ownerKey.value(field(owner, "email", String.class))
        		.value(field(owner, "firstName", String.class));
        
        addColumn(ownerKey);
        
        addColumn(cb.count(field(question, "id", String.class)));
        
        addColumn(cb.function("distinct_group_concat", String.class, field(question,"categoryName")));
        
        addColumn(prepInterview, "created", DefaultTableObjectTransformer.INSTANCE);
        
        includeInSearch(field(prepInterview, "name", String.class));
        includeInSearch(ownerKey);
        includeInSearch(field(question,"categoryName", String.class));
    }

    @Override
    public boolean canAccess(User u) {
        return u != null && u.hasRole(RoleValue.APP_ADMIN);
    }
}
