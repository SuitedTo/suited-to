package models.prep;

import java.lang.reflect.Type;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import modules.ebean.Finder;
import play.mvc.Http.Request;
import play.mvc.Scope.Params;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import dto.prep.PrepInterviewDTO;
import dto.prep.PrepInterviewReviewDTO;

import enums.prep.PrepInterviewReviewStatus;

import models.PrepRestrictable;

@Entity
@Table(name = "PREP_InterviewReview")
public class PrepInterviewReview extends EbeanModelBase implements PrepRestrictable {
	
	@ManyToOne
	public PrepInterview prepInterview;
	
	@Enumerated(EnumType.STRING)
	public PrepInterviewReviewStatus status;
	
	public String reviewKey;
	
	public String reviewerEmail;
	
	@OneToMany(mappedBy="prepInterviewReview")
	public List<PrepQuestionReview> questions;
	
	public static Finder<Long,PrepInterviewReview> find = new Finder<Long,PrepInterviewReview>(
            Long.class, PrepInterviewReview.class
    );

	@Override
	public boolean hasAccess(PrepUser user) {
		Params params = Request.current().params;
		String reviewKey = params.get("reviewKey");
		return (reviewKey != null) && (reviewKey.equals(this.reviewKey));
	}

	@Override
	public JsonSerializer<PrepInterviewReview> serializer() {
		return new JsonSerializer<PrepInterviewReview>(){
			public JsonElement serialize(PrepInterviewReview pi, Type type,
					JsonSerializationContext context) {

				PrepInterviewReviewDTO d = PrepInterviewReviewDTO.fromPrepInterviewReview(pi);
				JsonObject result = (JsonObject)new JsonParser().parse(d.toJson());
				return result;

			}
		};
	}

}
