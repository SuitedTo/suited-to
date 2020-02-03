package controllers;

import controllers.deadbolt.Deadbolt;
import controllers.deadbolt.RestrictedResource;
import data.binding.types.ImageBinder;
import data.validation.Image;
import db.jpa.S3Blob;
import enums.ImageSize;
import models.User;
import play.Logger;
import play.data.binding.As;
import play.db.jpa.Blob;
import play.libs.MimeTypes;
import play.mvc.Before;
import play.mvc.With;
import utils.ImageUtil;

import java.io.FileInputStream;

@With(Deadbolt.class)
public class Accounts extends ControllerBase {
	
	@Before
	public static void addUser(){
		renderArgs.put("user", Security.connectedUser());
	}

	public static void show() {

		User user = Security.connectedUser();
		
    	render(user.getRoles());
    }
	
	public static void cancelEdit() {
		Application.home();
    }
	
	/**
	 * Upload a profile picture for the connected user
	 * todo: ideally this would be taking a File argument instead of blob, but I haven't modified the binder yet
	 * @param picture The picture.
	 */
	@RestrictedResource(name = {"models.User"}, staticFallback = true)
	public static void uploadPicture(
			@As(binder=ImageBinder.class)
			@Image(alias="Profile picture",typeCheckOnly=true)
			Blob picture) {

		if(validation.hasErrors()){
			renderTemplate("@show");
		}
        User user = Security.connectedUser();

        Blob thumbnail = new Blob();
        thumbnail.set(ImageUtil.scaleAndCrop(picture.get(), picture.type(), ImageSize.THUMBNAIL), picture.type());
        picture.set(ImageUtil.scale(picture.get(), picture.type(), ImageSize.PROFILE), picture.type());

        try {
            S3Blob s3ProfilePicBlob = new S3Blob(true);
            S3Blob s3ThumbnailBlob = new S3Blob(true);
            await(s3ProfilePicBlob.setAsJob(new FileInputStream(picture.getFile()),
                    MimeTypes.getContentType(picture.getFile().getName())).now());
            await(s3ThumbnailBlob.setAsJob(new FileInputStream(thumbnail.getFile()),
                    MimeTypes.getContentType(thumbnail.getFile().getName())).now());

            user.picture = s3ProfilePicBlob;
            user.thumbnail = s3ThumbnailBlob;
            user.save();

        } catch (Exception e) {
            flash.error("picture.upload.error");
            Logger.error("Unable to upload picture. %s", e.getMessage());
        }

		renderArgs.put("user", user);
		renderTemplate("@show");
	}
	
	/**
	 * Delete the profile picture for the connected user.  Removes the picture from S3 bucket and then sets the
     * reference on the User object to null.
	 */
	@RestrictedResource(name = {"models.User"}, staticFallback = true)
	public static void deletePicture() {
		User user = Security.connectedUser();
        if(user.hasPicture()){
            user.picture.delete();
        }
		user.picture = null;
		
		user.save();
    	
		renderTemplate("@show");
    }
	
	public static void getPicture(){
		User user = Security.connectedUser();
        notFoundIfNull(user);

		
		if(user.hasPicture()){
			S3Blob picture = user.picture;
	        response.setContentTypeIfNotSet(picture.type());
	        renderBinary(picture.get());
		}
	}
}
