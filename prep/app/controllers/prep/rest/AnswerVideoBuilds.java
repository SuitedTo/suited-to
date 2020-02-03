package controllers.prep.rest;

import java.util.Calendar;
import java.util.Date;

import models.prep.PrepQuestion;
import play.Play;
import play.data.binding.As;
import play.jobs.Job;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import data.binding.types.prep.JsonBinder;
import db.jpa.S3Blob;
import dto.prep.PrepAnswerVideoPartsDTO;
import enums.prep.VideoStatus;

public class AnswerVideoBuilds extends PrepController{
	
	public static void create(@As(binder = JsonBinder.class) PrepAnswerVideoPartsDTO body){
		
/*
 * 
 * The goal here is to take a PrepAnswerVideoPartsDTO that looks something like this:
{
	questionId: 3,
	audio: ['<bucket>|<key>', '<bucket>|<key>', ...],
	video: ['<bucket>|<key>', '<bucket>|<key>, ...'],
}
 *
 *
 * ...use it build a job that looks something like this:
{
	'handback': {'questionId': 1, 'output': { 'format': 'mp4', 'contents': '<bucket>|<key>' }},
	'request': {merge: {'inputs' :[ concat: {'inputs': [ { 'getUrl' : 'https:// … ', 'format' : 'webm', 'stream': 0}, … ]}, concat: {'inputs': [ { 'getUrl' : 'https:// … ', 'format' : 'wav', 'stream': 0}, … ]} ], 'format' : 'webm'} },
	'output': { 'putUrl': 'https:// …', 'contents': '<bucket>|<key>' }
}
 *
 *
 * ...and then send that job out to the SQS queue for processing.
	 
*/
		final String sqsUrl = Play.configuration.getProperty("sqs.url");
	    final String accessKey = Play.configuration.getProperty("sqs.access.key");
	    final String secretKey = Play.configuration.getProperty("sqs.secret.key");
	    
	    /**
	     * Firefox supports webm for video
	     * Firefox doesn't support mp4
	     * Firefox doesn't support webp so can't record with firefox
	     * 
	     * Media Stream Recording API has not been implemented in chrome yet which
	     * means that we can record audio in wav
	     * 
	     * Net result: we can only support the chrome browser and we're pretty
	     * much stuck with huge audio files for now.
	     * 
	     */
		final String OUTPUT_FORMAT = "mp4";
		
		if((body == null) || (body.questionId == null)){
			badRequest();
		}
		final PrepQuestion question = PrepQuestion.find.byId(body.questionId);
		if(question == null){
			badRequest();
		}
		
		
        
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.MINUTE, 10);
        Date tenMinutesFromNow = cal.getTime();
        
        //create the job
        final JsonObject jobToQueue = new JsonObject();
        
        //add the request to the job
        //JsonArray videoStreamAccessors = new JsonArray();
        JsonArray audioStreamAccessors = new JsonArray();
        for(int i = 0; i < body.audio.length; ++i){
            // commenting out lots of the unneeded video code until RecordRTC fixes their issues
        	//JsonObject videoStreamAccessor = new JsonObject();
        	//videoStreamAccessor.addProperty("getUrl",
        			//new S3Blob(body.video[i]).getTemporarySignedUrl(tenMinutesFromNow, null).toString());
        	//videoStreamAccessor.addProperty("format", "webm");
        	//videoStreamAccessor.addProperty("stream", 0);
        	//videoStreamAccessors.add(videoStreamAccessor);
        	
        	
        	JsonObject audioStreamAccessor = new JsonObject();
        	audioStreamAccessor.addProperty("getUrl",
        			new S3Blob(body.audio[i]).getTemporarySignedUrl(tenMinutesFromNow, null).toString());
        	audioStreamAccessor.addProperty("format", "wav");
        	audioStreamAccessor.addProperty("stream", 0);
        	audioStreamAccessors.add(audioStreamAccessor);
        }
        
        //JsonObject concatVideo = new JsonObject();
        
        //JsonObject concatVArgs = new JsonObject();
        //concatVArgs.add("inputs",videoStreamAccessors);
        //concatVideo.add("concat", concatVArgs);
        
        
        JsonObject concatAudio = new JsonObject();
        JsonObject concatAArgs = new JsonObject();
        concatAArgs.add("inputs",audioStreamAccessors);
        concatAudio.add("concat", concatAArgs);
        
        JsonArray staticLogoArray = new JsonArray();
        //staticLogoArray.add(concatVideo);
        staticLogoArray.add(concatAudio);
        
        JsonObject staticLogoArgs = new JsonObject();
        staticLogoArgs.add("inputs", staticLogoArray);
        staticLogoArgs.addProperty("format", OUTPUT_FORMAT);
        
        JsonObject staticLogo = new JsonObject();
        staticLogo.add("staticLogo", staticLogoArgs);
        
        jobToQueue.add("request", staticLogo);
        
        
        //add the handback to the job
        S3Blob.S3Request putRequest =  S3Blob.getTemporarySignedPutRequest(tenMinutesFromNow,
        		"." + OUTPUT_FORMAT, "video/" + OUTPUT_FORMAT);
        
        JsonObject handback = new JsonObject();
        handback.addProperty("questionId", body.questionId);
        
        JsonObject handbackOutput = new JsonObject();
        handbackOutput.addProperty("format", OUTPUT_FORMAT);
        handbackOutput.addProperty("contents", putRequest.contents);
        handback.add("output", handbackOutput);
        
        jobToQueue.add("handback", handback);
        
        
        //add the output to the job
        JsonObject output = new JsonObject();
        output.addProperty("putUrl", putRequest.url);
        output.addProperty("contents", putRequest.contents);
        
        jobToQueue.add("output", output);
        
        
//        System.out.println(jobToQueue.toString());
        
        question.videoStatus = VideoStatus.PENDING;
		question.save();
        
        new Job(){
        	public void doJob(){
        		try{
        			//send the job out for processing
        			AmazonSQSClient sqs = new AmazonSQSClient(new BasicAWSCredentials(accessKey, secretKey));
        			sqs.sendMessage(new SendMessageRequest(sqsUrl, jobToQueue.toString()));
        			
        		} catch(AmazonClientException e){
        			e.printStackTrace();
        		}
        	}
        }.now();
	}

}
