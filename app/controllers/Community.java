package controllers;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import controllers.deadbolt.Unrestricted;
import enums.QuestionStatus;
import models.*;
import play.data.validation.Required;
import org.apache.commons.lang.StringUtils;
import play.mvc.Router;
import scheduler.TaskArgs;
import scheduler.TaskScheduler;
import tasks.StreetCredUpdateTask;
import tasks.UpdateQuestionMetricsTask;
import tasks.UpdateUserBadgeMetricsTask;

import java.lang.reflect.Type;
import java.util.*;

public class Community extends ControllerBase {

    private static final JsonSerializer QUESTION_LIST_SERIALIZER =
            new QuestionListSerializer();
    private static final JsonSerializer CATEGORY_LIST_SERIALIZER =
            new categoryListSerializer();
    private static final JsonSerializer STRING_LIST_SERIALIZER =
            new stringListSerializer();

    private static final Gson QUESTION_AS_SCORE_TEXT_URL_RENDERER;
    private static final Gson STRING_LIST_RENDERER;

    static {

        // Create/init Gson serializers
        GsonBuilder builder = new GsonBuilder();

        Type questionListType = new TypeToken<List<Question>>(){}.getType();
        builder.registerTypeAdapter(questionListType, QUESTION_LIST_SERIALIZER);

        QUESTION_AS_SCORE_TEXT_URL_RENDERER = builder.create();

        builder = new GsonBuilder();

        Type stringListType = new TypeToken<List<String>>(){}.getType();
        builder.registerTypeAdapter(stringListType, STRING_LIST_SERIALIZER);

        STRING_LIST_RENDERER = builder.create();

        /**
         * Crate UserBadgeMetrics if this is the first time
         * Play! has been run since the evo script where table
         * UserBadgeMetrics was created. This is necessary due
         * not being able to get all the necessary data out of
         * UserBadge.getInfo() while in SQLland;
         */

//        long badgeMetricsCount = UserBadgeMetrics.count();
//        if(badgeMetricsCount < 1) {
//            TaskScheduler.schedule(UpdateUserBadgeMetricsTask.class, new TaskArgs(), CronTrigger.getASAPTrigger());
//        }
    }

    public static void index() {
        if(!Security.isConnected()){
            String homeUrl = System.getenv("HOME_URL");
            if(StringUtils.isEmpty(homeUrl)) {
                render(); //we don't have a home url defined so just use the built-in one
            } else {
                redirect(homeUrl);
            }

            redirect("http://www.suitedto.com");
        } else {
            Application.home();
        }
    }

    public static void community(){
        User user = Security.connectedUser();

        render(user);
    }

    public static void badges(){
        User user = Security.connectedUser();
        List<UserBadgeMetrics> badgeMetrics = UserBadgeMetrics.findAll();

        render(user, badgeMetrics);
    }

    /**
     * Public profile referenced in community
     *
     * @param id User id to display
     */
    public static void profile(@Required Long id) {
        User user = User.findById(id);

        validateCommunityUser(user);
        
        String photoUrl = "";
        if (user.isProfilePicturePublic()){
            photoUrl = user.getPublicPictureUrl();
        }
        String displayName = user.displayName;
        Long streetCred = user.streetCred;
        Long publicQuestionsCount = user.getAcceptedPublicQuestionCount();
        int reviewedQuestionsCount = user.getNumberOfQuestionsReviewed();
        Long userId = user.id;


        List<UserBadge> badges = user.getEarnedBadges(Integer.MAX_VALUE);
        int badgesCount = badges.size();

        Boolean isReviewer = false;
        Boolean proInterviewer = false;
        Boolean available = false;
        if (user.isUserStatusLevelPublic()){
            isReviewer = user.hasReviewCapability();
            proInterviewer = user.isProInterviewer();
            //Pro interviewer availability not yet implemented
            available = false;
        }


        Boolean isCategorySetPublic = user.isCategorySetPublic();
        Boolean isBadgeCollectionPublic = user.isBadgeCollectionPublic();
        Boolean isSubmittedQuestionListPublic = user.isSubmittedQuestionListPublic();
        Boolean isReviewdQuestionListPublic = user.isReviewdQuestionListPublic();
        Boolean privacyLockdown = user.privacyLockdown;

        render(photoUrl, displayName, streetCred, badges, badgesCount, isReviewer,
                proInterviewer, available, publicQuestionsCount, reviewedQuestionsCount,
                userId, isCategorySetPublic, isBadgeCollectionPublic, isSubmittedQuestionListPublic,
                isReviewdQuestionListPublic, privacyLockdown);
    }

    /**
     * Generates Json data of a user's top rated questions;
     * @param userId
     * @param page
     * @param runLength
     */
    @Unrestricted
    public static void getTopAcceptedQuestionsForCommunityUser (@Required Long userId, @Required Integer page,
                                                                @Required Integer runLength) {

        User user = User.findById(userId);
        validatePagedCommunityInputs(user, page, runLength);
        if(!user.isSubmittedQuestionListPublic()) {
            forbidden();
        }

        List<Question> topQuestions = user.getAcceptedPublicQuestions(page, runLength);
        Type questionListType = new TypeToken<List<Question>>(){}.getType();

        renderJSON(QUESTION_AS_SCORE_TEXT_URL_RENDERER.toJson(topQuestions, questionListType));
    }

    @Unrestricted
    public static void getTopReviewCategoriesForCommunityUser (@Required Long userId, @Required Integer max) {

        User user = User.findById(userId);
        validateCommunityUser(user);
        if(!user.isCategorySetPublic()) {
            forbidden();
        }

        List<String> topCategoryNames = user.getTopReviewCategoryNames(max);
        Type stringListType = new TypeToken<List<String>>(){}.getType();

        renderJSON(STRING_LIST_RENDERER.toJson(topCategoryNames, stringListType));
    }

    @Unrestricted
    public static void getTopReviewerQuestionsForCommunityUser (@Required Long userId, @Required Integer page,
                                                                @Required Integer runLength) {

        User user = User.findById(userId);
        validatePagedCommunityInputs(user, page, runLength);
        if(!user.isReviewdQuestionListPublic()) {
            forbidden();
        }

        // All accepted questions this user reviewed
        List<Question> reviewedQuestions =
                Question.find("select distinct q  " +
                        "from Question q " +
                        "inner join q.workflows as qw " +
                        "where qw.user = :user " +
                        "and q.user <> :user " +
                        "and q.status = :acceptedStatus " +
                        "and q.flaggedAsInappropriate = false " +
                        "order by q.standardScore desc")
                        .setParameter("user", user)
                        .setParameter("acceptedStatus", QuestionStatus.ACCEPTED)
                        .fetch(page, runLength);

        Type questionListType = new TypeToken<List<Question>>(){}.getType();
        renderJSON(QUESTION_AS_SCORE_TEXT_URL_RENDERER.toJson(reviewedQuestions, questionListType));
    }

    /**
     * Returns a json string containing the names of a
     * user's top categories calculated by
     *
     * 1) Count of questions where user is the question
     * submitter and question is accepted (group by category)
     * 2) Count of questions where user is a reviewer (question
     * can be in any status, group by category)
     *
     * and ordered in descending relevance
     * @return
     */
    public static void getTopCategoriesForCommunityUser(@Required Long userId, @Required Integer max) {

        User user = User.findById(userId);
        validateCommunityUser(user);
        if(!user.isCategorySetPublic()) {
            forbidden();
        }

        List<String> categoryNames = user.getTopCategoryNames(max);
        Type stringListType = new TypeToken<List<String>>(){}.getType();

        renderJSON(STRING_LIST_RENDERER.toJson(categoryNames, stringListType));
    }

    private static void validatePagedCommunityInputs(User user, Integer page, Integer runLength) {

        validateCommunityUser(user);

        final int MAX_RESULTS = 20;
        String errorMessage = "Error: ";
        // Negative runLengths and pages are a nonsensical
        if(runLength < 0) {
            errorMessage = "runLength was less than zero";
        }
        if(page < 1) {
            errorMessage = "pageLength was less than one";
        }
        // Restrict results to prevent question scraping via this controller
        if(page * runLength > MAX_RESULTS) {
            errorMessage = "page * runLength = " +
                    page + " * " + runLength + " + " + " = " +  page * runLength +
                    "the maximum result depth of " + MAX_RESULTS;
        }
        if(!errorMessage.equals("Error: ")) {
            renderJSON(new Gson().toJson(new JsonPrimitive(errorMessage)));
        }
    }

    private static void validateCommunityUser(User user) {
        if(user == null || !user.isCommunityMember()) {
            forbidden();
        }
    }

    /**
     * Serializes each question in a list into a JS object containing the question's
     * standardScore, text, and (ONLY if a user is logged in) url.
     */
    private static class QuestionListSerializer implements JsonSerializer<ArrayList<Question>> {

        public JsonElement serialize(ArrayList<Question> questionList, Type type,
                                     JsonSerializationContext jsc) {
            JsonArray result = new JsonArray();
            boolean userLoggedIn = Security.connectedUser() != null ? true : false;
            Map<String, Object> routerParam = new Hashtable<String, Object>();

            for(Question q : questionList) {
                JsonObject serializedQuestion = new JsonObject();

                serializedQuestion.addProperty("standardScore", q.standardScore);
                serializedQuestion.addProperty("text", q.text);

                if(userLoggedIn) {
                    routerParam.put("id", q.id);
                    String url = (request.secure) ? "https://" : "http://";
                    url += request.host;
                    url += Router.reverse("Questions.view", routerParam).toString();

                    serializedQuestion.addProperty("url", url);
                }
                result.add(serializedQuestion);
            }
            return result;
        }
    }


    private static class categoryListSerializer implements JsonSerializer<List<Category>> {

        public JsonElement serialize(List<Category> categoryList, Type type,
                                     JsonSerializationContext jsc) {
            JsonArray result = new JsonArray();
            for(Category c : categoryList) {
                result.add(new JsonPrimitive(c.name));
            }
            return result;
        }
    }

    private static class stringListSerializer implements JsonSerializer<List<String>> {

        public JsonElement serialize(List<String> nameList, Type type,
                                     JsonSerializationContext jsc) {
            JsonArray result = new JsonArray();
            for(String s : nameList) {
                result.add(new JsonPrimitive(s));
            }
            return result;
        }
    }
}
