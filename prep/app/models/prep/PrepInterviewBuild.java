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

import dto.prep.PrepInterviewBuildDTO;

@Entity
@Table(name = "PREP_InterviewBuild")
public class PrepInterviewBuild extends EbeanModelBase implements PrepRestrictable {
	
	@ManyToOne
	public PrepInterview interview; 
	
	public static Finder<Long,PrepInterviewBuild> find = new Finder<Long,PrepInterviewBuild>(
            Long.class, PrepInterviewBuild.class
    );

	@Override
	public JsonSerializer<PrepInterviewBuild> serializer() {
		return new JsonSerializer<PrepInterviewBuild>(){
			public JsonElement serialize(PrepInterviewBuild pi, Type type,
					JsonSerializationContext context) {

				PrepInterviewBuildDTO d = PrepInterviewBuildDTO.fromPrepInterviewBuild(pi);
				JsonObject result = (JsonObject)new JsonParser().parse(d.toJson());
				return result;

			}
		};
	}

    @Override
    public boolean hasAccess(PrepUser user) {
        if(interview == null){
            return true; //handle the case where the interview is in the process of being built but is not ready yet
        }

        return interview.hasAccess(user);
    }
}
