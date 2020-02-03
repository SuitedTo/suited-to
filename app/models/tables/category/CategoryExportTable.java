package models.tables.category;

import javax.persistence.criteria.CriteriaBuilder;

import models.User;
import models.query.category.AccessibleCategories;
import models.query.category.ExternallyAvailableCategories;
import models.tables.QueryBaseTable;
import models.tables.CriteriaHelperTable.ObjectToMessage;
import play.db.jpa.JPA;
import utils.CriteriaHelper.RootTableKey;

public class CategoryExportTable  extends QueryBaseTable {

    public CategoryExportTable(){
    	super(new ExternallyAvailableCategories());
    	CriteriaBuilder builder = JPA.em().getCriteriaBuilder();
    	RootTableKey root = getQueryBaseRootKey();
    	
    	addColumn(root, "id");
        addColumn(root, "name");
        addColumn(root, "exportedToPrep");
        
        includeInSearch(field(root, "name", String.class));
    }

	@Override
	public boolean canAccess(User u) {
		return u != null;
	}

}
