package models.prep;

import java.lang.reflect.Type;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import models.PrepRestrictable;
import models.prep.mappedsuperclasses.File;
import modules.ebean.Finder;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import db.jpa.S3Blob;
import dto.prep.PrepAnswerVideoDTO;

@Entity
@Table(name = "PREP_AnswerVideo")
public class PrepAnswerVideo extends File implements PrepRestrictable{
	
	@OneToOne
    @JoinColumn(name="question_id")
	public PrepQuestion question;

	public PrepAnswerVideo(PrepQuestion question, PrepUser owner, String name, String type, S3Blob contents) {
		super(owner, name, type, contents);
		this.question = question;
	}
	
	public static Finder<Long,PrepAnswerVideo> find = new Finder<Long,PrepAnswerVideo>(
            Long.class, PrepAnswerVideo.class
    );
	

	@Override
	public boolean hasAccess(PrepUser user) {
		return owner.equals(user);
	}

	@Override
	public JsonSerializer serializer() {
		return new JsonSerializer<PrepAnswerVideo>(){
			public JsonElement serialize(PrepAnswerVideo pq, Type type,
					JsonSerializationContext context) {

				PrepAnswerVideoDTO qd = PrepAnswerVideoDTO.fromAnswerVideo(pq);

				return (JsonObject)new JsonParser().parse(qd.toJson());
			}
		};
	}

}
