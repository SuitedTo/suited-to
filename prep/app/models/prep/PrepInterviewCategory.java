package models.prep;

import java.lang.reflect.Type;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import modules.ebean.Finder;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import dto.prep.PrepInterviewCategoryDTO;
import enums.prep.Contribution;
import enums.prep.Difficulty;

@Entity
@Table(name = "PREP_InterviewCategory")
public class PrepInterviewCategory extends EbeanModelBase{
	
	@ManyToOne
	public PrepCategory prepCategory;

	@Enumerated(EnumType.STRING)
	public Difficulty difficulty;
	
	@Enumerated(EnumType.STRING)
	public Contribution contribution;
	
	public static Finder<Long,PrepInterviewCategory> find = new Finder<Long,PrepInterviewCategory>(
            Long.class, PrepInterviewCategory.class
    );
	
	
	@Override
	public JsonSerializer<PrepInterviewCategory> serializer() {
		return new JsonSerializer<PrepInterviewCategory>(){
			public JsonElement serialize(PrepInterviewCategory pi, Type type,
					JsonSerializationContext context) {

				PrepInterviewCategoryDTO d = PrepInterviewCategoryDTO.fromPrepInterviewCategory(pi);
				JsonObject result = (JsonObject)new JsonParser().parse(d.toJson());
				return result;

			}
		};
	}


}
