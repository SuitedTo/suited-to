package models.prep;

import java.lang.reflect.Type;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import models.PrepRestrictable;
import modules.ebean.Finder;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import dto.prep.PrepQuestionReviewDTO;

@Entity
@Table(name = "PREP_QuestionReview")
public class PrepQuestionReview extends EbeanModelBase implements PrepRestrictable {
	
	@ManyToOne
	public PrepQuestion prepQuestion;
	
	@ManyToOne
	public PrepInterviewReview prepInterviewReview;
	
	public String text;
	
	public static Finder<Long,PrepQuestionReview> find = new Finder<Long,PrepQuestionReview>(
            Long.class, PrepQuestionReview.class
    );

	@Override
	public boolean hasAccess(PrepUser user) {
		return prepInterviewReview.hasAccess(user);
	}

	@Override
	public JsonSerializer<PrepQuestionReview> serializer() {
		return new JsonSerializer<PrepQuestionReview>(){
			public JsonElement serialize(PrepQuestionReview pi, Type type,
					JsonSerializationContext context) {

				PrepQuestionReviewDTO d = PrepQuestionReviewDTO.fromPrepQuestionReview(pi);
				JsonObject result = (JsonObject)new JsonParser().parse(d.toJson());
				return result;

			}
		};
	}
}
