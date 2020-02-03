package controllers.prep.rest;

import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import models.prep.PrepAnswerVideo;
import models.prep.PrepInterview;
import models.prep.PrepQuestion;
import models.prep.PrepUser;
import play.data.binding.As;
import play.jobs.Job;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import common.utils.prep.SecurityUtil;

import controllers.prep.access.PrepRestrictedResource;
import controllers.prep.auth.PublicAction;
import data.binding.types.prep.JsonBinder;
import db.jpa.S3Blob;
import dto.prep.PrepAnswerVideoDTO;
import dto.prep.PrepVideoStatusMsgDTO;
import enums.prep.VideoStatus;

public class AnswerVideos extends PrepController{
	
	public static void create(@As(binder = JsonBinder.class) PrepAnswerVideoDTO body){
		final S3Blob blob = (body.contents == null)?null:new S3Blob(body.contents);

		if(blob == null){
			badRequest();
		}
		Boolean exists = await(new Job<Boolean>(){
			public Boolean doJobWithResult(){
				return blob.exists();
			}
		}.now());

		if(!exists){
			badRequest();
		}

		if(body.questionId == null){
			badRequest();
		}
		
		PrepUser owner = SecurityUtil.connectedUser();
		
		PrepQuestion question = PrepQuestion.find.byId(body.questionId);
		if(question == null){
			badRequest();
		}
		
		if(!question.interview.owner.equals(owner)){
			forbidden();
		}
		
		question.videoStatus = VideoStatus.AVAILABLE;
		question.save();

		
		PrepAnswerVideo video = new PrepAnswerVideo(
				question,
				owner,
				body.name,
				body.type,
				blob);
		video.save();
		renderRefinedJSON(video.toJsonObject());
	}
	
    @PublicAction
    public static void encodingCallback(@As(binder = JsonBinder.class) PrepVideoStatusMsgDTO body) {
    	
        if("SUCCESS".equals(body.status)) {
        	
        	if((body.handback == null) || (body.handback.output == null) ||
        			(body.handback.output.contents == null) || (body.handback.questionId == null)){
    			badRequest();
    		}
        	
        	final S3Blob video = new S3Blob(body.handback.output.contents);
        	
        	if(!video.isInternal()){
        		unauthorized();
        	}

            PrepQuestion question = PrepQuestion.find.byId(body.handback.questionId);
            question.videoStatus = VideoStatus.AVAILABLE;
			question.save();
            
            PrepAnswerVideo.delete("question_id = ?", question.id);

            PrepUser owner = question.interview.owner;
            String vidName = "interview" + question.interview.id + "question" + question.id;
            new PrepAnswerVideo(question, owner, vidName, body.handback.output.format, video).save();
            
        }
    }

	public static void update(@As(binder = JsonBinder.class) PrepAnswerVideoDTO body){
		final S3Blob blob = (body.contents == null)?null:new S3Blob(body.contents);
		
		if(blob == null){
			badRequest();
		}
		
		Boolean exists = await(new Job<Boolean>(){
			public Boolean doJobWithResult(){
				return blob.exists();
			}
		}.now());

		if(!exists){
			badRequest();
		}


		if((body.questionId == null) || (body.id == null)){
			badRequest();
		}
		
		PrepUser owner = SecurityUtil.connectedUser();
		
		PrepQuestion question = PrepQuestion.find.byId(body.questionId);
		if(question == null){
			badRequest();
		}
		
		if(!question.interview.owner.equals(owner)){
			forbidden();
		}

		
		PrepAnswerVideo video = PrepAnswerVideo.find.byId(body.id);
		if(video == null){
			notFound();
		}
		
		if(!question.interview.owner.equals(video.owner)){
			forbidden();
		}
		
		video.question = question;
		video.owner = owner;
		video.name = body.name;
		video.type = body.type;
		video.contents = (blob == null)?null:blob.toString();
		
		video.save();
		renderRefinedJSON(video.toJsonObject());
	}
	
	@PrepRestrictedResource(resourceClassName = "models.prep.PrepAnswerVideo")
	public static void get(final Long id){
		if(id == null){
			notFound();
		}
		PrepAnswerVideo video = PrepAnswerVideo.findById(id);
		if(video == null){
			notFound();
		}
		
		PrepAnswerVideoDTO dto = PrepAnswerVideoDTO.fromAnswerVideo(video);
		dto = (PrepAnswerVideoDTO) await(dto.loadURL());
		renderRefinedJSON(dto);
	}
	
	@PrepRestrictedResource(resourceClassName = "models.prep.PrepAnswerVideo")
	@Deprecated //Deprecated in favor of DELETE Questions/:id/video
	public static void delete(Long id){
		if(id == null){
			notFound();
		}
		
		PrepAnswerVideo video = PrepAnswerVideo.findById(id);
		if(video == null){
			notFound();
		}
		
		PrepQuestion question = video.question;
		question.videoStatus = VideoStatus.UNAVAILABLE;
		question.save();
		video.delete();
		renderRefinedJSON(video.toJsonObject());
	}

    public static void getVideosForInterview(Long interviewId) {
        PrepInterview interview = PrepInterview.find.byId(interviewId);
        List<PrepQuestion> questions = interview.questions;

        JsonArray ja = new JsonArray();

        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DATE, 1);
        Date tomorrow = cal.getTime();

        for(PrepQuestion pq : questions) {
            if(pq.answerVideo != null) {
                S3Blob videoBlob = new S3Blob(pq.answerVideo.contents);
                URL url  = videoBlob.getTemporarySignedUrl(tomorrow, "answerVideo" + pq.id);
                JsonObject videoObj = new JsonObject();
                videoObj.addProperty("questionId", pq.id);
                videoObj.addProperty("url", url.toString());
                ja.add(videoObj);
            }
        }
        JsonObject result = new JsonObject();
        result.add("videos", ja);
        renderRefinedJSON(result);
    }
}
