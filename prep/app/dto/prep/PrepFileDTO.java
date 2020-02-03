package dto.prep;

import play.jobs.Job;
import play.libs.F.Promise;
import db.jpa.S3Blob;
import models.prep.mappedsuperclasses.File;

public class PrepFileDTO extends PrepDTO{
	
	public Long id;
	
	public Long ownerId;

	public String name;
	
	public String type;
	
	public String contents;
	
	public String url;
	
	public Promise<PrepFileDTO> loadURL(){
		final PrepFileDTO result = this;
		return new Job<PrepFileDTO>(){
			public PrepFileDTO doJobWithResult(){
				result.url = new S3Blob(contents).getTemporarySignedUrl(null).toString();
				return result;
			}
		}.now();
		
	}
	
	public static PrepFileDTO fromFile(File file, PrepFileDTO dto){
		if(file != null){
			dto.id = file.id;
			dto.ownerId = file.owner.id;
			dto.name = file.name;
			dto.type = file.type;
			dto.contents = file.contents.toString();
		}
		return dto;
	}
	
	public static PrepFileDTO fromFile(File file){
		return PrepFileDTO.fromFile(file, new PrepFileDTO());
	}
}
