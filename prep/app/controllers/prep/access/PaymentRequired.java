package controllers.prep.access;

import models.prep.PrepInterview;
import models.prep.PrepUser;
import play.Logger;

import common.utils.prep.SecurityUtil;

import controllers.prep.access.Access.AccessManager;
import errors.prep.PrepError;
import errors.prep.PrepErrorResult;
import errors.prep.PrepErrorType;

public class PaymentRequired implements AccessManager{

	@Override
	public void checkAccess() {
		PrepUser user = SecurityUtil.connectedUser();
		if(!user.hasPaid()){
			throw new PrepErrorResult(new PrepError(PrepErrorType.ACCESS_ERROR));
		}
	}

}
