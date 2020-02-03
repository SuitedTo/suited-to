package models.prep;

import java.lang.reflect.Type;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import modules.ebean.Finder;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import dto.prep.PrepInterviewCategoryListBuildDTO;

@Entity
@Table(name = "PREP_InterviewCategoryListBuild")
public class PrepInterviewCategoryListBuild extends EbeanModelBase{
	
	@ManyToOne
	public PrepInterviewCategoryList categoryList;
	
	public static Finder<Long,PrepInterviewCategoryListBuild> find = new Finder<Long,PrepInterviewCategoryListBuild>(
            Long.class, PrepInterviewCategoryListBuild.class
    );

	@Override
	public JsonSerializer<PrepInterviewCategoryListBuild> serializer() {
		return new JsonSerializer<PrepInterviewCategoryListBuild>(){
			public JsonElement serialize(PrepInterviewCategoryListBuild pi, Type type,
					JsonSerializationContext context) {

				PrepInterviewCategoryListBuildDTO d = PrepInterviewCategoryListBuildDTO.fromPrepInterviewCategoryListBuild(pi);
				JsonObject result = (JsonObject)new JsonParser().parse(d.toJson());
				return result;

			}
		};
	}

}
