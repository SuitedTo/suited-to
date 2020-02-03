package models;

import javax.persistence.Entity;
import javax.persistence.MappedSuperclass;

import db.jpa.S3Blob;

@MappedSuperclass
public abstract class File extends ModelBase implements Restrictable{

	public String name;
	
	public String type;
	
	public S3Blob contents;

	public File(String name, String type, S3Blob contents) {
		super();
		this.name = name;
		this.type = type;
		this.contents = contents;
	}
}
