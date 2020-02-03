package controllers;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import controllers.deadbolt.Deadbolt;
import controllers.deadbolt.Restrict;
import enums.RoleValue;
import enums.prep.Contribution;
import enums.prep.Difficulty;
import enums.prep.SkillLevel;
import models.prep.*;

import org.apache.commons.lang.StringUtils;
import play.data.validation.Max;
import play.data.validation.Min;
import play.data.validation.Required;
import play.i18n.Messages;
import play.mvc.With;

import java.lang.Long;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

//todo: consider breaking this out into separate controllers in a prepadmin package
@With(Deadbolt.class)
@Restrict(RoleValue.APP_ADMIN_STRING)
public class PrepAdmin extends ControllerBase {

    /*************************************************
     * Static Fields                                 *
     *************************************************/

    private static final String AUTOMATION_SEARCH_STRING = "[automation]%";
    private static final JsonSerializer PREPJOB_SERIALIZER =
            new PrepJobSerializer();
    private static final JsonSerializer PREPJOBCATEGORY_SERIALIZER =
            new PrepJobCategorySerializer();
    private static final JsonSerializer PREPJOBCATEGORY_LIST_SERIALIZER
            = new PrepJobCategoryListSerializer();


    private static final Gson PREPJOB_RENDERER;
    private static final Gson PREPJOBCATEGORY_RENDERER;
    private static final Gson PREPJOBCATEGORY_LIST_RENDERER;

    private static final Type prepJobCategoryListType;

    static {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(PrepJob.class, PREPJOB_SERIALIZER);

        PREPJOB_RENDERER = builder.create();

        builder = new GsonBuilder();
        builder.registerTypeAdapter(PrepJobCategory.class, PREPJOBCATEGORY_SERIALIZER);

        PREPJOBCATEGORY_RENDERER = builder.create();

        builder = new GsonBuilder();

        prepJobCategoryListType = new TypeToken<List<PrepJobCategory>>(){}.getType();
        builder.registerTypeAdapter(prepJobCategoryListType, PREPJOBCATEGORY_LIST_SERIALIZER);

        PREPJOBCATEGORY_LIST_RENDERER = builder.create();
    }


    /*************************************************
     * Actions                                       *
     *************************************************/


    public static void admin(){
        render();
    }


    public static void listPrepJob() {
        render();
    }


    public static void showPrepJob(final Long id) {

        if(id != null) {
            PrepJob prepJob = PrepJob.findById(id);
            if(prepJob != null) {
                renderArgs.put("prepJob", prepJob);
                renderArgs.put("additionalNames", prepJobNamesCommaSeparatedExcludePrimary(prepJob));
            }
        }

        render();
    }


    public static void listPrepUsers(){
        render();
    }
    
    public static void listPrepInterviews(){
    	render();
    }

    public static void deletePrepUser(Long id){
        PrepUser user = PrepUser.findById(id);

        user.delete();
        listPrepUsers();
    }

    public static void deletePrepCoupon(Long id){
        PrepCoupon prepCoupon = PrepCoupon.findById(id);
        prepCoupon.delete();
        showPrepCoupon();
    }

    public static void showPrepCoupon() {
        render();
    }

    public static void newPrepCoupon() {
        render();
    }

    public static void createPrepCoupon(@Required String name, Integer periods, @Required @Max(100) @Min(1) Integer discount, @Required Integer max) {
        // TODO Data binding/validation - it was late, and I realized my error this morning.
        if (!validation.hasErrors()){
            PrepCoupon coupon = new PrepCoupon();
            coupon.name = name;
            coupon.discount = discount;
            coupon.maxUses = max;
            coupon.payPeriods = periods;
            coupon.save();
            showPrepCoupon();
        } else {
            flash.error("Invalid form data");
            newPrepCoupon();
        }
    }

    /*************************************************
     * AJAX Actions                                  *
     *************************************************/


    public static void getPrepJobCategories(@Required String prepJobId) {
        PrepJob prepJob = PrepJob.findById(Long.valueOf(prepJobId));
        if(prepJob == null) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("Error", "Could not find prepJob with id of " + prepJobId);
            renderJSON(jsonObject.toString());
        }

        renderJSON(PREPJOBCATEGORY_LIST_RENDERER.toJson(prepJob.prepJobCategories, prepJobCategoryListType));
    }


    private static PrepJobCategory editPrepJobCategory(@Required PrepJobCategory prepJobCategory, @Required String categoryName, @Required SkillLevel skillLevel,
                                                       @Required String primaryCategory, @Required Difficulty difficulty, @Required Contribution weight) {

        PrepCategory prepCategory = PrepCategory.find.where().eq("Name", categoryName).findUnique();
        if(prepCategory == null) {
            JsonObject response = new JsonObject();
            response.addProperty("Error", "PrepCategory with name \"" + categoryName + "\" does not exist.");
            renderJSON(response.toString());
        }
        prepJobCategory.prepCategory = prepCategory;

        prepJobCategory.prepJobLevel = skillLevel;

        prepJobCategory.primaryCategory = Boolean.parseBoolean(primaryCategory);
        prepJobCategory.difficulty = difficulty;
        prepJobCategory.weight = weight;

        return prepJobCategory;

    }


    public static void editPrepJobCategory(@Required String id, @Required String categoryName, @Required SkillLevel skillLevel,
                                           @Required String primaryCategory, @Required Difficulty difficulty, @Required Contribution weight) {
        PrepJobCategory prepJobCategory = PrepJobCategory.findById(Long.valueOf(id));
        if(prepJobCategory == null) {
            JsonObject response = new JsonObject();
            response.addProperty("Error", "PrepJobCategory with id " + id + " does not exist.");
            renderJSON(response.toString());
        }

        editPrepJobCategory(prepJobCategory, categoryName, skillLevel, primaryCategory, difficulty, weight);
        errorOnInvalidPrepJobCategory(prepJobCategory.prepJob, prepJobCategory);
        prepJobCategory.save();

        renderJSON(PREPJOBCATEGORY_RENDERER.toJson(prepJobCategory));
    }


    public static void addPrepJobCategory(@Required String prepJobId,@Required String categoryName, @Required SkillLevel skillLevel,
                                          @Required String primaryCategory, @Required Difficulty difficulty, @Required Contribution weight) {
        PrepJob prepJob = PrepJob.findById(Long.valueOf(prepJobId));
        if(prepJob == null) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("Error", "Could not find prepJob with id of " + prepJobId);
            renderJSON(jsonObject.toString());
        }
        PrepJobCategory prepJobCategory = new PrepJobCategory();
        editPrepJobCategory(prepJobCategory, categoryName, skillLevel, primaryCategory, difficulty, weight);
        errorOnInvalidPrepJobCategory(prepJob, prepJobCategory);
        prepJob.prepJobCategories.add(prepJobCategory);
        prepJobCategory.prepJob = prepJob;
        prepJob.save();

        renderJSON(PREPJOBCATEGORY_RENDERER.toJson(prepJobCategory));

    }


    public static void deletePrepJobCategory(@Required String id) {
        PrepJobCategory prepJobCategory = PrepJobCategory.findById(Long.valueOf(id));
        JsonObject jsonObject = new JsonObject();

        if(prepJobCategory == null) {
            jsonObject.addProperty("Error", "Could not find prepJobCategory with id of " + id);
            renderJSON(jsonObject.toString());
        }
        PrepJob prepJob = prepJobCategory.prepJob;
        if(prepJob.prepJobCategories.size() <= 1) {
            jsonObject.addProperty("Error", "You can't delete a prepJob's last category!");
            renderJSON(jsonObject.toString());
        }

        boolean hasPrimaryCategory = false;
        for(PrepJobCategory pjc : prepJobCategory.prepJob.prepJobCategories) {
            if(!pjc.equals(prepJobCategory) && pjc.primaryCategory) {
                hasPrimaryCategory = true;
            }
        }
        if(!hasPrimaryCategory) {
            jsonObject.addProperty("Error", "You can't delete a prepJob's last primary prepJobCategory.\n" +
                "Stuff would break and QA would yell at me.");
            renderJSON(jsonObject.toString());
        }


        prepJobCategory.delete();

        jsonObject.addProperty("Success", "Deleted prepJobCategory with id of " + id);
        renderJSON(jsonObject.toString());
    }

    public static void deletePrepJob(@Required String id) {
        PrepJob prepJob = PrepJob.findById(Long.valueOf(id));
        JsonObject jsonObject = new JsonObject();

        if(prepJob == null) {
            jsonObject.addProperty("Error", "Could not find prepJob with id of " + id);
            renderJSON(jsonObject.toString());
        }

        prepJob.delete();

        listPrepJob();
    }

    /**
     * Returns if @param prepJobCategory can be added to @param prepJob.
     * If invalid, renders a json error;
     *
     * Criteria for addition:
     * 1. Another prepJobCategory in prepJob exists with the same
     * prepCategory and skillLevel.
     * 2. The prepJob has at least one primary category
     *
     */
    private static void errorOnInvalidPrepJobCategory(PrepJob prepJob, PrepJobCategory prepJobCategory) {
        HashSet<SkillLevel> skillLevels = new HashSet<SkillLevel>();
        JsonObject jsonObject = new JsonObject();
        boolean hasPrimaryCategory = prepJobCategory.primaryCategory;

        for(PrepJobCategory pjc : prepJob.prepJobCategories) {
            if( !pjc.equals(prepJobCategory) && pjc.prepCategory.equals(prepJobCategory.prepCategory)
                    && pjc.prepJobLevel.equals(prepJobCategory.prepJobLevel) && pjc.difficulty.equals(prepJobCategory.difficulty)) {
                jsonObject.addProperty("Error", "Ruh-Roh, Shaggy! A prepJobCategory with the same category, skill level, and " +
                        "difficulty already exists");
                renderJSON(jsonObject.toString());
            }
            else if(pjc.primaryCategory) {
                hasPrimaryCategory = true;
            }
        }

        if(!hasPrimaryCategory) {
            jsonObject.addProperty("Error", "I'm sorry, Dave. I'm afraid I can't do that. Each prepJob must have at least one primary category");
            renderJSON(jsonObject.toString());
        }
        return;
    }

    /**
     * Create a new prepJob and add a new prepJobCategory
     * @param primaryName
     * @param additionalNames
     * @param categoryName
     * @param skillLevel
     * @param primaryCategory
     * @param difficulty
     * @param weight
     */

    public static void newPrepJob(@Required String primaryName, String additionalNames,
                                  @Required String categoryName, @Required SkillLevel skillLevel,
                                  @Required String primaryCategory, @Required Difficulty difficulty, @Required Contribution weight) {

        PrepCategory prepCategory = PrepCategory.find.where().eq("name", categoryName).findUnique();
        if(prepCategory == null) {
            JsonObject response = new JsonObject();
            response.addProperty("Error", "PrepCategory with name \"" + categoryName + "\" does not exist.");
            renderJSON(response.toString());
        }

        PrepJobCategory prepJobCategory = new PrepJobCategory();
        editPrepJobCategory(prepJobCategory, categoryName, skillLevel, primaryCategory, difficulty, weight);

        PrepJob prepJob = new PrepJob();
        prepJobCategory.prepJob = prepJob;
        editPrepJobNames(prepJob, primaryName, additionalNames);
        prepJob.prepJobCategories = new LinkedList<PrepJobCategory>();
        errorOnInvalidPrepJobCategory(prepJob, prepJobCategory);
        prepJob.prepJobCategories.add(prepJobCategory);

        prepJob.save();

        renderJSON(PREPJOB_RENDERER.toJson(prepJob));
    }


    public static void editPrepJobNames(String id, String primaryName, String additionalNames) {
        JsonObject jsonObject = new JsonObject();
        PrepJob prepJob = PrepJob.findById(Long.valueOf(id));
        if(prepJob == null) {
            jsonObject.addProperty("Error", "Could not find prepJob with id of " + id);
            renderJSON(jsonObject.toString());
        }
        if(primaryName == null || primaryName.isEmpty()) {
            jsonObject.addProperty("Error", "Please include a primaryName!");
            renderJSON(jsonObject.toString());
        }
        else {
            editPrepJobNames(prepJob, primaryName, additionalNames);
            prepJob.save();
        }

        renderJSON(PREPJOB_RENDERER.toJson(prepJob));
    }


    private static PrepJob editPrepJobNames(PrepJob prepJob, String primaryName, String additionalNames) {
        HashMap<String, PrepJobName> oldNames = new HashMap<String, PrepJobName>();

        if(prepJob.prepJobNames != null) {
            for(PrepJobName pjn : prepJob.prepJobNames) {
                oldNames.put(pjn.name, pjn);
            }
        }
        else {
            prepJob.prepJobNames = new ArrayList<PrepJobName>();
        }

        Set<String> newNames = new HashSet<String>();
        newNames.add(primaryName);
        if(StringUtils.isNotEmpty(additionalNames)) {
            newNames.addAll(Arrays.asList(additionalNames.split(",")));
        }

        HashMap<String, PrepJobName> finalNames = new HashMap<String, PrepJobName>();
        for(String pjn : newNames) {
            if(StringUtils.isNotBlank(pjn)) {
                pjn = pjn.trim();
                if(oldNames.containsKey(pjn)) {
                    finalNames.put(pjn, oldNames.get(pjn));
                }
                else {
                    finalNames.put(pjn, new PrepJobName(prepJob, pjn));
                }
            }
        }

        prepJob.prepJobNames.clear();
        prepJob.prepJobNames.addAll(finalNames.values());
        prepJob.primaryName = finalNames.get(primaryName);

        return prepJob;
    }


    /*************************************************
     * Private Utility Methods                       *
     *************************************************/

    /**
     * A helper method that returns a comma separated string of a prepJob's names
     * excluding the primaryName
     */
    private static String prepJobNamesCommaSeparatedExcludePrimary(PrepJob prepJob) {
        String additionalNames = "";
        for(int i = 0; i < prepJob.prepJobNames.size(); i++) {
            String name = prepJob.prepJobNames.get(i).name;

            if(!name.equals(prepJob.primaryName.name)) {
                additionalNames += name;

                if(i < prepJob.prepJobNames.size()-1) {
                    additionalNames  += ", ";
                }
            }
        }
        return additionalNames;
    }

    /*************************************************
     * Custom JSON Serializers                       *
     *************************************************/

    private static class PrepJobSerializer implements JsonSerializer<PrepJob> {

        public JsonElement serialize(PrepJob pj, Type type,
                                     JsonSerializationContext jsc) {

            JsonObject result = new JsonObject();
            if(pj != null) {
                result.addProperty("id", pj.id);
                result.addProperty("primaryName", pj.primaryName.name);
                result.addProperty("additionalNames", prepJobNamesCommaSeparatedExcludePrimary(pj));

                JsonArray categories = new JsonArray();
                for(PrepJobCategory c : pj.prepJobCategories) {
                    categories.add(PrepJobCategorySerializer.INSTANCE.serialize(c, null, null));
                }
                result.add("prepJobCategories", categories);
            }

            return result;
        }
    }

    private static class PrepJobCategorySerializer implements JsonSerializer<PrepJobCategory> {

        public static PrepJobCategorySerializer INSTANCE = new PrepJobCategorySerializer();

        public JsonElement serialize(PrepJobCategory category, Type type,
                                     JsonSerializationContext jsc) {
            JsonObject result = new JsonObject();
            if(category != null) {
                result.addProperty("id", category.id);
                result.addProperty("prepJob", category.prepJob.id);
                result.addProperty("level", category.prepJobLevel.name());
                result.addProperty("prepCategory", category.prepCategory.name);
                result.addProperty("primaryCategory", Boolean.valueOf(category.primaryCategory));
                result.addProperty("difficulty", category.difficulty.name());
                result.addProperty("weight", category.weight.name());
            }

            return result;
        }
    }

    private static class PrepJobCategoryListSerializer implements JsonSerializer<List<PrepJobCategory>> {

        public JsonElement serialize(List<PrepJobCategory> categoryList, Type type,
                                    JsonSerializationContext jsc) {
            JsonObject result = new JsonObject();
            JsonArray categories = new JsonArray();

            for(PrepJobCategory pjc : categoryList) {
                categories.add(PrepJobCategorySerializer.INSTANCE.serialize(pjc, null, null));
            }

            result.add("categories", categories);
            return result;
        }
    }
}