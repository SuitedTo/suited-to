package controllers.prep.rest;

import play.data.binding.As;
import data.binding.types.prep.JsonBinder;
import db.jpa.S3Blob;
import dto.prep.PrepS3UploadDTO;

public class S3Uploads extends PrepController{

	public static void create(@As(binder = JsonBinder.class) PrepS3UploadDTO body){
		
		renderJSON(S3Blob.getTemporarySignedPutRequest(null, body.ext, body.contentType));
	}
}
