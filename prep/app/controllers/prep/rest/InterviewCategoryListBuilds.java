package controllers.prep.rest;

import java.util.ArrayList;
import java.util.List;

import models.prep.PrepCategory;
import models.prep.PrepInterview;
import models.prep.PrepInterviewCategory;
import models.prep.PrepInterviewCategoryList;
import models.prep.PrepInterviewCategoryListBuild;
import models.prep.PrepJob;
import models.prep.PrepJobCategory;
import models.prep.PrepUser;

import org.apache.commons.lang.StringUtils;

import com.avaje.ebean.Expr;

import play.Logger;
import play.data.binding.As;
import play.data.validation.Required;
import play.db.jpa.JPAPlugin;
import play.jobs.Job;
import play.mvc.With;

import common.utils.prep.SecurityUtil;

import controllers.prep.access.Access;
import controllers.prep.access.FreeIfNoOwnedInterviews;
import controllers.prep.access.RestrictAccess;
import data.binding.types.prep.JsonBinder;
import dto.prep.PrepInterviewCategoryListBuildDTO;
import dto.prep.PrepInterviewCategoryListBuildRequestDTO;
import enums.prep.Contribution;
import enums.prep.Difficulty;
import enums.prep.JobMatchType;
import enums.prep.SkillLevel;

@With(Access.class)
public class InterviewCategoryListBuilds extends PrepController{

	/**
	 * Kick off a build and return it. The client is responsible for polling
	 * the build to figure out when it is finished. When it includes a categoryListId
	 * it is finished.
	 * 
	 * The thing that is built here is a list of "interview categories". An "interview category"
	 * is an object that contains a category along with other information that is required by
	 * SuitedTo's interview builder. So we're really building an interview builder request.
	 * 
	 * A PrepInterviewCategoryListBuild will be created and saved as a result of this call.
	 * 
	 * @param body
	 */
	@RestrictAccess(FreeIfNoOwnedInterviews.class)
	public static void create(@Required @As(binder = JsonBinder.class) PrepInterviewCategoryListBuildRequestDTO body) {

		PrepInterviewCategoryListBuild newBuild = new PrepInterviewCategoryListBuild();
		newBuild.save();
		
		PrepUser user = SecurityUtil.connectedUser();
		
		JPAPlugin.closeTx(false);

		new BuildJob(newBuild.id, user, body).now();

		renderRefinedJSON(PrepInterviewCategoryListBuildDTO.fromPrepInterviewCategoryListBuild(newBuild).toJsonTree());
	}

	public static void get(Long id) {

		PrepInterviewCategoryListBuild build = PrepInterviewCategoryListBuild.find.byId(id);

		if(build == null){
			notFound();
		}

		renderRefinedJSON(PrepInterviewCategoryListBuildDTO.fromPrepInterviewCategoryListBuild(build).toJsonTree());
	}
	
	private static String appendSuffix(PrepUser user, String name){
		int suffix = 2;
		boolean available = PrepInterview.find.where().and(Expr.eq("name", name), Expr.eq("owner.id", user.id)).findRowCount() == 0;
		final String prefix = name;
		String next = prefix;
		while(!available){
			next = prefix + " (" + suffix + ")";
			available = PrepInterview.find.where().and(Expr.eq("name", next), Expr.eq("owner.id", user.id)).findRowCount() == 0;
			++suffix;
		}
		return next + " interview";
	}
	
	private static String getName(PrepUser owner, PrepJob job){
		return appendSuffix(owner,job.primaryName.name);
	}
	
	private static String getName(PrepUser owner, List<PrepInterviewCategory> categories){
		List<String> categoryNames = new ArrayList<String>();
		final int limit = 3;
		int index = 0;
		for(PrepInterviewCategory category : categories){
			if(index < limit){
				categoryNames.add(category.prepCategory.name);
			} else {
				break;
			}
			index++;
		}
		String name = StringUtils.join(categoryNames, ',');
		
		return appendSuffix(owner, name);
	}


	public static class BuildJob extends Job<PrepInterviewCategoryList>{
		private final Long id;
		private final PrepUser owner;
		private final PrepInterviewCategoryListBuildRequestDTO buildRequest;

		public BuildJob(Long id, PrepUser owner, PrepInterviewCategoryListBuildRequestDTO buildRequest){
			this.id = id;
			this.owner = owner;
			this.buildRequest = buildRequest;
		}
		public PrepInterviewCategoryList doJobWithResult(){

			PrepInterviewCategoryList categoryList = new PrepInterviewCategoryList();
			if(buildRequest.type.equals(JobMatchType.PREP_JOB) ||
					buildRequest.type.equals(JobMatchType.PREP_JOB_VIA_CATEGORY)){
				PrepJob job = PrepJob.find.byId(buildRequest.entityId);
				if(job != null){
					List<PrepJobCategory> jobCategories = job.prepJobCategories;
					for(PrepJobCategory jobCategory : jobCategories){

						if((buildRequest.level != null) && (jobCategory.prepJobLevel != buildRequest.level)){
							continue;
						}

						PrepInterviewCategory interviewCategory = new PrepInterviewCategory();

						interviewCategory.prepCategory = jobCategory.prepCategory;

						interviewCategory.difficulty = jobCategory.difficulty;

						interviewCategory.contribution = jobCategory.weight;

						interviewCategory.save();
						categoryList.categories.add(interviewCategory);
					}
					categoryList.name = getName(owner, job);
					categoryList.save();
				} else {
					Logger.error("PrepJob[%s] not found", buildRequest.entityId);
				}
			} else {
				PrepCategory pc = PrepCategory.find.byId(buildRequest.entityId);
				if(pc != null){
					PrepInterviewCategory interviewCategory = new PrepInterviewCategory();
					interviewCategory.prepCategory = pc;
					interviewCategory.contribution = Contribution.LARGE;

					if (SkillLevel.SENIOR.equals(buildRequest.level)) {
						interviewCategory.difficulty = Difficulty.HARD;
					} else if (SkillLevel.MID.equals(buildRequest.level)) {
						interviewCategory.difficulty = Difficulty.MEDIUM;
					}  else {
						interviewCategory.difficulty = Difficulty.EASY;
					}
					interviewCategory.save();
					categoryList.categories.add(interviewCategory);
					categoryList.name = getName(owner, categoryList.categories);
					categoryList.save();

				} else {
					Logger.error("PrepCategory[%s] not found", buildRequest.entityId);
				}
			}
			
			if(id != null){
				PrepInterviewCategoryListBuild build = PrepInterviewCategoryListBuild.
					find.byId(id);
				if(build != null){
					build.categoryList = categoryList;
					build.save();
				} else {
				Logger.error("PrepInterviewCategoryListBuild[%s] was not found", id);
				}
			}
			
			return categoryList;

		}
	}
}
