package models.prep.mappedsuperclasses;

import java.lang.reflect.Type;

import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

import play.jobs.Job;

import models.prep.EbeanModelBase;
import models.prep.PrepUser;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import db.jpa.S3Blob;
import dto.prep.PrepFileDTO;

@MappedSuperclass
public abstract class File extends EbeanModelBase{
	
	@ManyToOne
	public PrepUser owner;

	public String name;
	
	public String type;
	
	public String contents;

	public File(PrepUser owner, String name, String type, S3Blob blob) {
		super();
		this.owner = owner;
		this.name = name;
		this.type = type;
		this.contents = (blob == null)?null:blob.toString();
	}
	
	public File delete(){
		final S3Blob blobToDelete = new S3Blob(contents);
		new Job(){
			public void doJob(){
				blobToDelete.delete();
			}
		}.now();
		
		return super.delete();
	}
	
	@Override
	public JsonSerializer serializer() {
		return new JsonSerializer<File>(){
			public JsonElement serialize(File pq, Type type,
					JsonSerializationContext context) {

				PrepFileDTO qd = PrepFileDTO.fromFile(pq);

				if(qd == null){
					return new JsonObject();
				}

				return (JsonObject)new JsonParser().parse(qd.toJson());
			}
		};
	}
}
