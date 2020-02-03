package models.prep;

import java.lang.reflect.Type;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import models.PrepRestrictable;
import modules.ebean.Finder;

import org.codehaus.jackson.annotate.JsonIgnore;

import play.cache.Cache;
import play.jobs.Job;
import play.jobs.On;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import controllers.prep.delegates.QuestionsDelegate;
import dto.prep.PrepQuestionDTO;
import dto.suitedto.SuitedToQuestionDTO;
import enums.prep.VideoStatus;
import exceptions.prep.MissingTwinException;

@Entity
@Table(name = "PREP_Question")
public class PrepQuestion extends EbeanModelBase implements PrepRestrictable{

	/**
	 * The suitedTo question id
	 */
	@Column(name="question_id")
	public String questionId;

	public String text;

	@Column(name="static_answers")
	public String staticAnswers;

	public String tips;
	
	@Column(name="category_name")
	public String categoryName;

    public String answers;

    @Column(name="video_uuid")
    public String videoUuid;
    
    @Enumerated(EnumType.STRING)
    @Column(name="video_status")
    public VideoStatus videoStatus;
    
    @OneToOne(mappedBy = "question", cascade= CascadeType.ALL)
    public PrepAnswerVideo answerVideo;
    
    @OneToMany(mappedBy="prepQuestion", cascade= CascadeType.REMOVE)
	private List<PrepQuestionReview> reviews;

    @JsonIgnore
	@ManyToOne
	public PrepInterview interview;
	
	public static Finder<Long,PrepQuestion> find = new Finder<Long,PrepQuestion>(
            Long.class, PrepQuestion.class
    );
	
	public JsonSerializer<PrepQuestion> serializer(){

		return new JsonSerializer<PrepQuestion>(){
			public JsonElement serialize(PrepQuestion pq, Type type,
					JsonSerializationContext context) {

				PrepQuestionDTO qd = PrepQuestionDTO.fromPrepQuestion(pq);

				if(qd == null){
					return new JsonObject();
				}

				return (JsonObject)new JsonParser().parse(qd.toJson());
			}
		};
	}
	
	public PrepQuestion sync() throws MissingTwinException{
		SuitedToQuestionDTO sq = QuestionsDelegate.get(Long.valueOf(questionId));
		if(sq == null){
			throw new MissingTwinException();
		}

		text = sq.text;
        categoryName = sq.categoryName;

        // default staticAnswers and tips to null
        staticAnswers = null;
		tips = null;

        // set staticAnswers and tips accordingly
        if(sq.prepAnswers != null && !sq.prepAnswers.equals("")) {
            staticAnswers = sq.prepAnswers;
        } else {
            if(sq.answers != null && !sq.answers.equals("")) {
                staticAnswers = sq.answers;
            }
            if(sq.tips != null && !sq.tips.equals("")) {
                tips = sq.tips;
            }
        }

		save();

		return this;
	}

	@On("cron.PrepQuestionSync")
	public static class Sync extends Job{
		public void doJob(){
			if(Cache.safeAdd("PrepQuestion.Sync", -1, "5s")){
				List<PrepQuestion> questions = PrepQuestion.findAll();
				//TODO: break this up into smaller chunks
				for(PrepQuestion q : questions){
					try {
						q.sync();
					} catch (MissingTwinException e) {
						//Logger.error("No such question[" + q.questionId + "] found within SuitedTo");
						// TODO What do we want to do when we find that a suitedto question
						//has disappeared? Do we want to flag it somehow and let the user know
						//or just make it disappear for the prep user as well?
					}
				}

			}
		}
	}

	@Override
	public boolean hasAccess(PrepUser user) {
		return interview.hasAccess(user);
	}
}

