package models.tables.userBadge;

import controllers.Security;
import enums.RoleValue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import models.Company;
import models.User;
import models.UserBadge;
import models.filter.user.ByCompanyName;
import models.query.user.AccessibleUsersHelper;
import models.query.userBadge.AccessibleBadges;
import models.tables.CriteriaQueryPaginator;
import models.tables.FilteredCriteriaHelperTable;
import models.tables.Paginator;
import models.tables.QueryBaseTable;

import org.apache.commons.lang.StringUtils;
import org.hibernate.ejb.criteria.path.SingularAttributePath;

import play.i18n.Messages;
import utils.CriteriaHelper.RootTableKey;
import utils.CriteriaHelper.TableKey;
import utils.ObjectTransformer;
import utils.Transformer;
import utils.YamlUtil;

public class UserBadgeTable extends QueryBaseTable {

    public UserBadgeTable() {
        super(new AccessibleBadges());
        
        RootTableKey root = getQueryBaseRootKey();

        addColumn(root, "multiplier");
        addColumn(root, "name");
        addColumn(root, "id");
    }
    
    @Override
    public boolean canAccess(User u) {
        return u != null;
    }
    
    @Override
    public Object[] getColumnOrder() {
    	Object[] orig = super.getColumnOrder();
    	Object[] result = Arrays.copyOf(orig, orig.length + 2);
    	result[result.length - 1] = "icon";
    	result[result.length - 2] = "description";
    	return result;
    }
    
    @Override
    public Paginator getPaginatableData(String searchString) {
       
    	return ((CriteriaQueryPaginator)super.getPaginatableData(searchString))
    		.setResultTransformer(addStaticProperties());
    }
    
    private static Transformer <Map<Object, Object>> addStaticProperties(){
    	return new Transformer<Map<Object, Object>> (){

			@Override
			public Map<Object, Object> transform(Map<Object, Object> input) {
				if(input == null){
					return null;
				}
				Object nameObj = null;
				Set keys = input.keySet();
				for(Object key : keys){
					
					if(((SingularAttributePath)key).getAttribute().getName().equals("name")){
						nameObj = input.get(key);
					}
				}
				
				if(nameObj != null){
					String name = String.valueOf(nameObj);
					Map<Object, Object> data = (Map<Object, Object>) UserBadge.badgeData.get(name);
					
					if(data != null){
						input.putAll(data);
					}
				}
				return input;
			}
    	};
    }
}
