package models.prep;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import models.PrepRestrictable;
import modules.ebean.Finder;
import org.codehaus.jackson.annotate.JsonBackReference;
import play.db.jpa.JPA;
import play.mvc.Http.Request;

import com.avaje.ebean.Expr;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import common.utils.prep.SecurityUtil;

import dto.prep.PrepInterviewDTO;


@Entity
@Table(name = "PREP_Interview")
public class PrepInterview extends EbeanModelBase implements PrepRestrictable {
	
	@ManyToOne
    @JsonBackReference
	public PrepUser owner;
	
	public String name;

    /**
     * Keeps track of the most recent question viewed by the user. Value of -1 indicates a completed interview
     */
    public int currentQuestion = 0;
    
    public boolean valid;

	@OneToMany(mappedBy="interview", cascade= CascadeType.ALL)
	public List<PrepQuestion> questions = new ArrayList<PrepQuestion>();
	
	@OneToMany(mappedBy="prepInterview", cascade= CascadeType.REMOVE)
	private List<PrepInterviewReview> reviews;
	
	@OneToMany(mappedBy="interview", cascade= CascadeType.REMOVE)
	private List<PrepInterviewBuild> builds;
	
	public static Finder<Long,PrepInterview> find = new Finder<Long,PrepInterview>(
            Long.class, PrepInterview.class
    );

	@Override
	public JsonSerializer<PrepInterview> serializer(){
		
		return new JsonSerializer<PrepInterview>(){
			public JsonElement serialize(PrepInterview pi, Type type,
					JsonSerializationContext context) {

				PrepInterviewDTO d = PrepInterviewDTO.fromPrepInterview(pi);
				JsonObject result = (JsonObject)new JsonParser().parse(d.toJson());
				return result;

			}
		};
	}
	
	/**
	 * Users who are not up to date with their payment will have access to
	 * one free practice intervew. This method is used to get "the freebie"
	 * if one has already been created.
	 * @return The freebie or null if it doesn't exist.
	 */
	public static PrepInterview getFreebie(){

        PrepUser user = SecurityUtil.connectedUser();

        List<PrepInterview> interviews = getInterviewsByUser(user);

		if (interviews.size() == 0){
			return null;
		}
		return interviews.get(0);
	}

	/**
	 * Users who are not up to date with their payment will have access to
	 * one free practice intervew. This method is used to determine whether
	 * this interview is "the freebie"
	 * @return True id this interview is free.
	 */
	public boolean isFreebie(){
		

		List<PrepInterview> interviews = getInterviewsByUser(this.owner);
		return (interviews.size() >= 0) && ((long)interviews.get(0).id == (long)id);
	}

    public static List<PrepInterview> getInterviewsByUser(PrepUser user) {

        List<PrepInterview> interviews = null;

        if(user != null) {
            interviews =  PrepInterview.find.where().and(Expr.eq("owner.id", user.id), Expr.eq("valid", true))
            		.orderBy("created ASC").findList();
        }

        return interviews;
    }

    @Override
    public boolean hasAccess(PrepUser user) {
    	if(user == null){
    		String reviewKey = Request.current().params.get("reviewKey");
    		if(reviewKey != null){
    			return 0 != PrepInterviewReview.find.where().
    					and(Expr.eq("prepInterview.id", this.id),Expr.eq("reviewKey", reviewKey)).findRowCount();
    		}
    		return false;
    	}
        if(user.equals(owner)){
        	if(user.hasPaid()){
        		return true;
        	}
        	return (getFreebie() == null) || isFreebie();
        }
        
        String reviewKey = Request.current().params.get("reviewKey");
		if(reviewKey != null){
			return 0 != PrepInterviewReview.find.where().and(Expr.eq("prepInterview.id", this.id),Expr.eq("reviewKey", reviewKey)).findRowCount();
		}
		
        return false;
    }
}
