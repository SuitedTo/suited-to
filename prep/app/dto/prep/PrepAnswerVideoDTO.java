package dto.prep;

import java.util.Calendar;
import java.util.Date;

import models.prep.PrepAnswerVideo;
import db.jpa.S3Blob;

public class PrepAnswerVideoDTO extends PrepFileDTO{

	public Long questionId;
	public String getUrl;
	
	public static PrepAnswerVideoDTO fromAnswerVideo(PrepAnswerVideo video){
		PrepAnswerVideoDTO dto =  (PrepAnswerVideoDTO) PrepFileDTO.fromFile(video, new PrepAnswerVideoDTO());
		dto.questionId = video.question.id;
		
		Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DATE, 1);
        Date tomorrow = cal.getTime();
        
		S3Blob videoBlob = new S3Blob(video.contents);
        dto.getUrl  = videoBlob.getTemporarySignedUrl(tomorrow, "answerVideo" + video.id).toString();
		return dto;
	}
}
