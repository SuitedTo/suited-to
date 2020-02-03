package dto.prep;

import enums.prep.SubscriptionType;

public class PrepSubscriptionDTO extends PrepDTO{
	public String stripeToken;
	public String coupon;
	public SubscriptionType type;
}
