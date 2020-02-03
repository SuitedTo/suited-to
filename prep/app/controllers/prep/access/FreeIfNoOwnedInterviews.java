package controllers.prep.access;

import com.avaje.ebean.Expr;

import models.prep.PrepInterview;
import models.prep.PrepUser;
import play.Logger;

import common.utils.prep.SecurityUtil;

import controllers.prep.access.Access.AccessManager;
import errors.prep.PrepError;
import errors.prep.PrepErrorResult;
import errors.prep.PrepErrorType;

public class FreeIfNoOwnedInterviews implements AccessManager{

	@Override
	public void checkAccess() {
		PrepUser user = SecurityUtil.connectedUser();
		if(!user.hasPaid()){
			if(PrepInterview.find.where().and(Expr.eq("owner.id", user.id),Expr.eq("valid", true)).findRowCount() > 0){
				throw new PrepErrorResult(new PrepError(PrepErrorType.ACCESS_ERROR));
			}
		}
	}

}
