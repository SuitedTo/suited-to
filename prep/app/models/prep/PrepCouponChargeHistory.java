package models.prep;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import modules.ebean.Finder;

import com.avaje.ebean.annotation.NamedUpdates;
import com.avaje.ebean.annotation.NamedUpdate;
import com.google.gson.JsonSerializer;

@NamedUpdates(value = {
        @NamedUpdate(
                name = "deleteForUser",
                update = "delete from PrepCouponChargeHistory where prep_user_id = :user_id"
        )
})
@Entity
@Table(name = "PrepUser_currentCouponChargeHistory")
public class PrepCouponChargeHistory extends EbeanModelBase{

	@ManyToOne
	public PrepUser prepUser;
	
	public Date currentCouponChargeHistory;
	
	public static Finder<Long,PrepCouponChargeHistory> find = new Finder<Long,PrepCouponChargeHistory>(
            Long.class, PrepCouponChargeHistory.class
    );

	@Override
	public JsonSerializer serializer() {
		// TODO Auto-generated method stub
		return null;
	}

	public PrepCouponChargeHistory(PrepUser user,
			Date currentCouponChargeHistory) {
		super();
		this.prepUser = user;
		this.currentCouponChargeHistory = currentCouponChargeHistory;
	}

}
