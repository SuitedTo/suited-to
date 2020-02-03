package controllers.prep.rest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import models.prep.PrepCategory;
import models.prep.PrepJob;
import models.prep.PrepJobCategory;
import models.prep.PrepJobName;

import org.apache.commons.lang.StringEscapeUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dto.prep.PrepJobMatchDTO;
import enums.prep.JobMatchType;

public class JobMatches extends PrepController{
	
	/**
	 * This method returns a json array that is formatted for consumption by the
	 * twitter typeahead component. Would like to have done the formatting client
	 * side but the component doesn't seem to provide any way to access the incoming
	 * data before parsing it.
	 * 
	 * @param searchTerm
	 */
	public static void get(String searchTerm) {
		List<PrepJobMatchDTO> matches = findMatches(searchTerm);
		JsonArray result = new JsonArray();
		for(PrepJobMatchDTO match : matches){
			if(match.tokens.size() == 0){
				match.tokens.addAll(Arrays.asList(match.value.split("\\s")));
			}
			if(match.name == null){
				match.name = match.value;
			}
			result.add(new JsonParser().parse(match.toJson()));
		}
		//renderRefinedJSON(result.toString());
		renderJSON(result.toString());
	}
	
	private static String getPrimaryNameHint(PrepJob job, PrepJobName jobName){
		if((jobName != null) && job.primaryName.equals(jobName)){
			return "";
		}
		StringBuilder result = new StringBuilder("(like '");
		result
		.append(job.primaryName.name)
		.append("')");
		
		return result.toString();
	}
	
	private static List<PrepJobMatchDTO> findMatches(String searchTerm){
		List<PrepJobMatchDTO> matches = new ArrayList<PrepJobMatchDTO>();
		
		Map<Long,PrepJobMatchDTO> matchMap = new Hashtable<Long, PrepJobMatchDTO>();
		List<PrepJobName> jobNames = PrepJobName.find.where().ilike("name", "%" + searchTerm + "%").findList();
		for(PrepJobName jobName : jobNames){
			PrepJob job = jobName.prepJob;
			
			if(matchMap.get(job.id) != null){
				continue;
			}
			PrepJobMatchDTO m = new PrepJobMatchDTO();
			JsonObject data = new JsonObject();
			data.addProperty("entityId", job.id);
			data.addProperty("type", JobMatchType.PREP_JOB.toString());
			
			/*
			 * We need a category list at this time is because the skill level
			 * buttons are enabled/disabled based on the skill level.
			 */
			JsonArray categories = new JsonArray();
			List<PrepJobCategory> categoryList = job.prepJobCategories;
			for(PrepJobCategory jc : categoryList){
				JsonObject category = new JsonObject();
				category.addProperty("level", jc.prepJobLevel.toString());
				categories.add(category);
			}
			data.add("categories", categories);
			
			
			m.data = StringEscapeUtils.escapeHtml(data.toString());
			m.value = jobName.prepJob.primaryName.name;
			m.name = jobName.name;
			m.hint = getPrimaryNameHint(job, jobName);
			m.type = "JOB";
			matchMap.put(job.id, m);
		}
		matches.addAll(matchMap.values());
		
		List<PrepCategory> categories = PrepCategory.find.where().ilike("name", "%" + searchTerm + "%").findList();
		for(PrepCategory category : categories){
			PrepJobMatchDTO m = new PrepJobMatchDTO();
			JsonObject data = new JsonObject();
			data.addProperty("entityId", category.id);
			data.addProperty("type", JobMatchType.PREP_CATEGORY.toString());
			
			/*
			 * There's no skill level associated with a prep category so
			 * no need for a category list at this time.
			 */
			
			
			m.data = StringEscapeUtils.escapeHtml(data.toString());
			m.value = category.name;
			m.type = "CATEGORY";
			m.hint = "";
			matches.add(m);
			
			//TODO: join
			//There's no way to set the isSearchable flag yet so ignore it for now
			//if(category.isSearchable){
			Map<Long,PrepJobMatchDTO> matchMapVia = new Hashtable<Long, PrepJobMatchDTO>();
			List<PrepJobCategory> jobCategories = PrepJobCategory.find.where().eq("prepCategory.id", category.id).findList();
			for(PrepJobCategory jc : jobCategories){
				PrepJobMatchDTO jcMatch = new PrepJobMatchDTO();
				PrepJob job = jc.prepJob;
				
				if(matchMap.get(job.id) != null){
					continue;
				}
				
				data = new JsonObject();
				data.addProperty("entityId", job.id);
				data.addProperty("type", JobMatchType.PREP_JOB_VIA_CATEGORY.toString());
				
				/*
				 * We need a category list at this time is because the skill level
				 * buttons are enabled/disabled based on the skill level.
				 */
				JsonArray categoriesArray = new JsonArray();
				List<PrepJobCategory> categoryList = job.prepJobCategories;
				for(PrepJobCategory pjc : categoryList){
					JsonObject c = new JsonObject();
					c.addProperty("level", pjc.prepJobLevel.toString());
					categoriesArray.add(c);
				}
				data.add("categories", categoriesArray);
				
				//Inferred Job
				jcMatch.data = StringEscapeUtils.escapeHtml(data.toString());
				jcMatch.value = jc.prepCategory.name;
				jcMatch.hint = getPrimaryNameHint(job, null);
				jcMatch.type = "";
				
				matchMapVia.put(job.id, jcMatch);
				
			}
			matches.addAll(matchMapVia.values());
			//}
			
		}
		
		return matches;
		
	}
}
