package dto.prep;

import enums.prep.VideoStatus;
import exceptions.prep.MissingTwinException;
import models.prep.PrepQuestion;

public class PrepQuestionDTO extends PrepDTO{

	public Long id;
	public String text;
	public String answers;
    public String staticAnswers;
	public String tips;
    public String videoUuid;
    public String videoStatus;
    public Long answerVideoId;

	public static PrepQuestionDTO fromPrepQuestion(PrepQuestion question){
		if(question == null){
			return null;
		}
		//question = question.sync();
		PrepQuestionDTO qd = new PrepQuestionDTO();
		qd.id = question.id;
		qd.text = question.text;
        qd.staticAnswers = question.staticAnswers;
		qd.answers = question.answers;
        qd.videoUuid = question.videoUuid;
        qd.videoStatus = (question.videoStatus == null)?
        		VideoStatus.UNAVAILABLE.toString():question.videoStatus.toString();
        qd.answerVideoId = (question.answerVideo != null)?question.answerVideo.id:null;
		qd.tips = question.tips;
		return qd;
	}
}
