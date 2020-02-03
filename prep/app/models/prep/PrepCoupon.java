package models.prep;


import java.lang.reflect.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import modules.ebean.Finder;
import play.data.validation.Required;
import play.libs.F.Promise;
import stripe.StripeResult;
import stripe.Stripe_PrepCoupon;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import dto.prep.PrepCouponDTO;

@Entity
@Table(name = "PREP_Coupon")
public class PrepCoupon extends EbeanModelBase {
	
	//@Unique TODO: Figure something out here...broken with the move to ebean
    @Required
    public String name;

    @Column(name="max_uses")
    public Integer maxUses;

    @Column(name="current_uses")
    public Integer currentUses = 0;

    @Column(name="pay_periods")
    public Integer payPeriods;

    @Required
    public Integer discount; // Charge * (1 - (Discount / 100)) = Final charge to card
    
    
    /*****************************************************
     * Transient Fields                   *
     *****************************************************/
    public transient String stripeId;
    private transient Stripe_PrepCoupon stripeCoupon;
    
    
    public static Finder<Long,PrepCoupon> find = new Finder<Long,PrepCoupon>(
            Long.class, PrepCoupon.class
    );
    
    @Override
    public JsonSerializer<PrepCoupon> serializer() {

        return new JsonSerializer<PrepCoupon>() {
            public JsonElement serialize(PrepCoupon pj, Type type,
                                         JsonSerializationContext context) {

            	PrepCouponDTO d = PrepCouponDTO.fromPrepCoupon(pj);
                JsonObject result = (JsonObject) new JsonParser().parse(d.toJson());
                return result;
            }
        };
    }
    
    /*****************************************************
	 * Stripe Coupon delegate methods                     *
     *****************************************************/
	private void bindToStripe(){
		if(stripeCoupon == null){
			stripeCoupon = new Stripe_PrepCoupon(this);
		}
	}
	
	public Integer getPercentOff(){
		return ((StripeResult<Integer>)getPercentOffAsPromise()).get();
	}
	
	public Promise<Integer> getPercentOffAsPromise(){
		bindToStripe();
		return stripeCoupon.getPercentOff();
	}
			
}
