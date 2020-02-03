package logic.interviews;

import java.lang.reflect.Type;
import java.util.List;

import com.google.gson.*;
import controllers.api.serialization.CandidateSerializer;
import controllers.api.serialization.CategorySerializer;
import enums.AccessGroup;
import enums.Difficulty;

import models.Candidate;
import models.Category;
import models.Question;

/**
 * Contains the details that represent characteristics of a desired interview.
 * 
 * @author joel
 *
 */
public class InterviewRequest {
	
	private final AccessGroup accessGroup;
	
	private Candidate targetCandidate;
	
	private List<Question> assumed;
	
	private List<Question> blackList;
	
	/**
	 * Desired interview duration in minutes
	 */
	private int duration;
	
	private List<InterviewCategory> interviewCategory;

	
	/**
	 * @param targetCandidate The candidate for which the interview will be built
	 * @param assumed A list of questions that are assumed to already be in the resulting
	 * interview (typically these represent the current state of the interview).
	 * @param blackList A list of questions that are not allowed in the interview.
	 * @param duration The desired total time of the interview in minutes.
	 * @param interviewCategory Information about participating categories.
	 */
	public InterviewRequest(
			AccessGroup accessGroup,
			Candidate targetCandidate,
			final List<Question> questions,
			final List<Question> blackList,
			final int duration,
			final List<InterviewCategory> interviewCategory) {
		super();
		this.accessGroup = accessGroup;
		this.targetCandidate = targetCandidate;
		this.assumed = questions;
		this.blackList = blackList;
		this.duration = duration;
		this.interviewCategory = interviewCategory;
	}
	
	public final int getActualDuration() {
		
		int duration = 0;
		if(assumed != null){
			for(Question question : assumed){
				duration += question.getDuration();
			}
		}
		return duration;
	}

	public final int getDuration() {
		return duration;
	}

	public final void setDuration(final int duration) {
		this.duration = duration;
	}

	public final List<InterviewCategory> getInterviewCategory() {
		return interviewCategory;
	}

	public final void setInterviewCategory(final List<InterviewCategory> interviewCategory) {
		this.interviewCategory = interviewCategory;
	}

    /**
     * Outputs the InterviewRequest to JSON. very useful for debugging purposes
     * @return
     */
    public String toJson(){
//        return new GsonBuilder()
//                .setPrettyPrinting()
//                .serializeNulls()
//                .registerTypeAdapter(Category.class, new JsonSerializer<Category>(){
//                    @Override
//                    public JsonElement serialize(Category category, Type type, JsonSerializationContext jsonSerializationContext) {
//                        JsonObject result = new JsonObject();
//                        result.addProperty("name", category.name);
//
//                        return result;
//                    }
//                })
//                .registerTypeAdapter(Candidate.class, new JsonSerializer<Candidate>() {
//                    @Override
//                    public JsonElement serialize(Candidate candidate, Type type, JsonSerializationContext jsonSerializationContext) {
//                        JsonObject result = new JsonObject();
//                        result.addProperty("id", candidate.id);
//
//                        return result;
//                    }
//                })
//                .create().toJson(this);

        return "{TEMPORARILY REMOVED JSON LOGGING}";
    }
	
	/**
	 * Contains the details associated with one category of
	 * the desired interview.
	 * 
	 * @author joel
	 *
	 */
	public static class InterviewCategory implements Comparable<InterviewCategory>{

		
		/**
		 * The defining category
		 */
		private Category category;
		
		/**
		 * The percentage of the total time that
		 * this category should contribute to the interview
		 */
		private float percentage;
		
		/**
		 * A list of difficulties that should be included
		 */
		private List<Difficulty> difficulties;

		public InterviewCategory(final Category category, final float percentage,
				final List<Difficulty> difficulties) {
			super();
			this.category = category;
			this.percentage = percentage;
			this.difficulties = difficulties;
		}

		public final Category getCategory() {
			return category;
		}

		public final void setCategory(final Category category) {
			this.category = category;
		}

		public final float getPercentage() {
			return percentage;
		}

		public final void setPercentage(final int percentage) {
			this.percentage = percentage;
		}

		public final List<Difficulty> getDifficulties() {
			return difficulties;
		}

		public final void setDifficulties(final List<Difficulty> difficulties) {
			this.difficulties = difficulties;
		}
		
		@Override
		public final int compareTo(final InterviewCategory ic) {
			if(ic == null){
				return 1;
			}
			return (int) (ic.percentage - percentage);
		}
	}

	public Candidate getTargetCandidate() {
		return targetCandidate;
	}
	
	public void setTargetCandidate(Candidate candidate) {
		targetCandidate = candidate;
	}
	
	public void setAssumedQuestions(List<Question> questions) {
		this.assumed = questions;
	}

	public List<Question> getAssumedQuestions() {
		return assumed;
	}
	
	public List<Question> getBlackList() {
		return blackList;
	}

	public AccessGroup getAccessGroup() {
		return accessGroup;
	}
}
