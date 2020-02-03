package models.prep.queries.filters.interviews;

import com.avaje.ebean.Expr;
import com.avaje.ebean.ExpressionList;
import common.utils.prep.SecurityUtil;

import play.libs.F;

import java.util.Map;

import models.prep.PrepUser;
import models.prep.PrepUser.RoleValue;
import models.prep.ewrap.QueryBuildContext;
import models.prep.ewrap.QueryFilter;


public class Accessible <T> extends QueryFilter<T> {

    @Override
    public ExpressionList<T> apply(ExpressionList<T> expressionList, QueryBuildContext context, F.Option<Map<String, Object>> params) {
    	PrepUser user = context.getConnectedUser();
    	if(user != null){
    		if(user.hasRole(RoleValue.ADMIN)){
    			return expressionList.add(TRUE);
    		}
            return expressionList.add(Expr.and(Expr.eq("owner.id", user.id), Expr.eq("valid", true)));
        }
        return expressionList.add(FALSE);
        
    }
}
