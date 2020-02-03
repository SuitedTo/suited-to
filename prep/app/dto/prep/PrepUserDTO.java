package dto.prep;

import models.prep.PrepUser;

import java.util.Date;

import play.jobs.Job;
import play.libs.F.Promise;

import enums.prep.SubscriptionType;

public class PrepUserDTO extends PrepDTO{

	public Long id;

    public String firstName;
	
	public String email;
	
	public boolean paymentRequired;

    public String lastFourCardDigits;

    public String profilePictureUrl;

    public PrepUser.AuthProvider externalAuthProvider;

    public PrepCouponDTO coupon;

    public String stripeId;

    public Boolean isSubscriber;
    
    public Integer subscriptionCost;
    
    public SubscriptionType subscriptionType;

    public Date chargeDate;
    
    public Date subscriptionExpirationDate;

    public String stripeToken;
    
    public String cardType;

    public static Promise<PrepUserDTO> fromModel(final PrepUser pu) {
    	return new Job<PrepUserDTO>(){
    		
    		public PrepUserDTO doJobWithResult(){
    			boolean hasPaid = pu.hasPaid();

    			PrepUserDTO dto = new PrepUserDTO();
    			dto.id = pu.id;
    			dto.firstName = pu.firstName;
    			dto.email = pu.email;
    			dto.paymentRequired = !hasPaid;
    			dto.profilePictureUrl = pu.profilePictureUrl;
    			dto.externalAuthProvider = pu.externalAuthProvider;
    			dto.stripeId = pu.stripeId;
    			dto.isSubscriber = hasPaid;
    			
    			if(pu.stripeId != null){
    				dto.subscriptionType = pu.getSubscriptionType();
    				dto.chargeDate = pu.getChargeDate();
    				dto.subscriptionExpirationDate = pu.getSubscriptionExpiration();
    				dto.subscriptionCost = pu.getNextChargeAmount();
    				dto.cardType = pu.getCardType();
    				dto.lastFourCardDigits = pu.getLastFour();
    			} else {
    				dto.subscriptionType = SubscriptionType.NONE;
    			}

    			return dto;
    		}
    		
    	}.now();
    }
}
