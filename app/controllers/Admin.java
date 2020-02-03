package controllers;


import java.util.Arrays;
import java.util.List;

import enums.*;
import jobs.CategoryStatsJob;
import jobs.CleanAttachments;
import jobs.JasperReportsCompilationJob;
import jobs.UpdateUserBadges;
import jobs.UpdateUserMetrics;
import models.*;
import play.cache.Cache;
import play.data.validation.Validation;
import play.mvc.With;
import play.test.Fixtures;
import scheduler.TaskArgs;
import scheduler.TaskScheduler;
import scheduler.TaskScheduler.TaskInfo;
import tasks.StreetCredUpdateTask;
import tasks.UpdateQuestionMetricsTask;
import trigger.assessor.UserClassificationListener;
import utils.EncodingUtil;
import cache.KeyBuilder;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import controllers.deadbolt.Deadbolt;
import controllers.deadbolt.Restrict;
import controllers.deadbolt.RoleHolderPresent;

@With(Deadbolt.class)
public class Admin extends ControllerBase {


    private static final String AUTOMATION_SEARCH_STRING = "[automation]%";

    @Restrict(RoleValue.APP_ADMIN_STRING)
    public static void admin(){
        render();
    }

    @Restrict(RoleValue.APP_ADMIN_STRING)
    public static void recalculateCategoryStats() {
        new CategoryStatsJob().now();
        String result = "category";
        render("@admin", result);
    }

    @Restrict(RoleValue.APP_ADMIN_STRING)
    public static void recalculateStreetCred(){
        TaskScheduler.schedule(StreetCredUpdateTask.class, new TaskArgs(), CronTrigger.getASAPTrigger());
        String result = "streetCred";
        render("@admin", result);
    }
    
    @Restrict(RoleValue.APP_ADMIN_STRING)
    public static void cleanAttachments(){
        new CleanAttachments().now();
        String result = "clean";
        render("@admin",result);
    }
    
    @Restrict(RoleValue.APP_ADMIN_STRING)
    public static void updateQuestionMetrics(){
    	TaskScheduler.schedule(
				UpdateQuestionMetricsTask.class, 
				new TaskArgs(), 
				CronTrigger.getASAPTrigger());
    	String result = "questionMetrics";
        render("@admin",result);
    }
    
    @Restrict(RoleValue.APP_ADMIN_STRING)
    public static void updateUserMetrics(){
    	new UpdateUserMetrics().now();
    	String result = "userMetrics";
        render("@admin",result);
    }
    
    @Restrict(RoleValue.APP_ADMIN_STRING)
    public static void updateUserBadges(){
    	new UpdateUserBadges().now();
    	String result = "userBadges";
        render("@admin",result);
    }
    
    @Restrict(RoleValue.APP_ADMIN_STRING)
    public static void recompileReports(){
    	new JasperReportsCompilationJob().now();
    	String result = "jasper";
    	render("@admin", result);
    }

    @Restrict(RoleValue.APP_ADMIN_STRING)
    public static void userSwitcheroo(String userName) {
       User userToImpersonate  = User.findByUsername(userName);
       validation.required(userToImpersonate);

        if(Validation.hasErrors()) {
            render("@admin");
        }

        User user = Security.connectedUser();
        session.put("originalUser", user.email);


        // Mark user as connected
        session.put("id", userToImpersonate.id);
        session.put("username", userName);
        session.put("prettyusername", userName);

        String result = "switcheroo";
        renderArgs.put("connectedUser", userToImpersonate);
        render("@admin", result);
    }

    @RoleHolderPresent
    public static void switchBackToOriginalUser() {
        String originalUser = session.get("originalUser");
        User user = User.findByUsername(originalUser);

        session.remove("originalUser");
        session.put("id", user.id);
        session.put("username", originalUser);
        session.put("prettyusername", originalUser);
        renderArgs.put("connectedUser", user);

        Application.home();

    }

    /**
     * Runs a whole bunch of queries and displays the system stats page showing relevant information
     */
    @Restrict(RoleValue.APP_ADMIN_STRING)
    public static void stats(){
        Long companyCount = Company.count("byStatus", CompanyAccountStatus.ACTIVE);
        Long freeCompanyCount = Company.count("byStatusAndAccountType", CompanyAccountStatus.ACTIVE, AccountType.FREE);
        Long standardCompanyCount = Company.count("byStatusAndAccountType", CompanyAccountStatus.ACTIVE, AccountType.STANDARD);
        Long enterpriseCompanyCount = Company.count("byStatusAndAccountType", CompanyAccountStatus.ACTIVE, AccountType.ENTERPRISE);

        Long totalQuestionCount = Question.count("byActive", true);
        Long publicQuestionCount = Question.count("byActiveAndStatus", true, QuestionStatus.ACCEPTED);
        Long privateQuestionCount = Question.count("byActiveAndStatus", true, QuestionStatus.PRIVATE);
        Long interviewCount = Interview.count("byActive", true);

        //positional paramater required for in clause:
        //see https://groups.google.com/forum/?fromgroups#!topic/play-framework/8uYP0Kwxwgs
        Long activeUserCount = User.count("status in (?1)", UserStatus.getStatusesConsideredInUse());

        //the named entity is necessary in order to reference the reviewCategories collection
        Long reviewerCount = User.count("from User u where u.status IN (?1) AND " +
                "(u.superReviewer = ?2 OR u.reviewCategories IS NOT EMPTY)",
                UserStatus.getStatusesConsideredInUse(), true);

        Long contributingUserCount = User.count("from User u where u.status in (?1) " +
                "AND exists(select id from Question q where q.user = u and status = ?2)",
                UserStatus.getStatusesConsideredInUse(), QuestionStatus.ACCEPTED);

        Long categoryCount  = Category.count("status in (?1)",
                Arrays.asList(Category.CategoryStatus.BETA, Category.CategoryStatus.PUBLIC));


        render(companyCount, freeCompanyCount, standardCompanyCount, enterpriseCompanyCount,
                totalQuestionCount, publicQuestionCount, privateQuestionCount, interviewCount, categoryCount,
                activeUserCount, reviewerCount, contributingUserCount);

    }
    
    @Restrict(RoleValue.APP_ADMIN_STRING)
    public static void tasks(){
    	List<TaskInfo> info = TaskScheduler.getTaskInfoList();
    	render(info);
    }
    
    @Restrict(RoleValue.APP_ADMIN_STRING)
    public static void runTask(String key){
    	await(TaskScheduler.runTask(key));
    }

    @Restrict(RoleValue.APP_ADMIN_STRING)
    public static void status(){
    	render();
    }

    @Restrict(RoleValue.APP_ADMIN_STRING)
    public static void statusData(){

//    	String result = await(new play.jobs.Job<String>(){
//
//    		public String doJobWithResult(){
//    			int miss = 0;
//    			int i = 0;
//    			JsonObject json = new JsonObject();
//    			try {
//    				do{
//    					Object status = Cache.get(KeyBuilder.buildInstanceKey(i, Application.class.getName() + "status"));
//    					if(status == null){
//    						miss++;
//    					}else{
//    						miss = 0;
//    						json.add("" + i, new JsonPrimitive(EncodingUtil.encodeURIComponent(String.valueOf(status))));
//    					}
//    					i++;
//    				}while(miss < 3);
//    				return json.toString();
//    			} catch (Exception e) {
//    				e.printStackTrace();
//    				return("{}");
//    			}
//    		}
//    	}.now());
//    	renderJSON(result);

    }

    @Restrict(RoleValue.APP_ADMIN_STRING)
    public static void cleanupAutomatedTestData() {
        List<Company> automationCompanies = Company.find("byNameLike", AUTOMATION_SEARCH_STRING).fetch();
        for (Company company : automationCompanies) {
            company.delete();
        }
        Fixtures.idCache.clear();
        Fixtures.loadModels("automation-data.yml");

        String result = "obliterate";
        render("@admin", result);
    }

    @Restrict(RoleValue.APP_ADMIN_STRING)
    public static void viewProInterviewerRequests() {
        List<ProInterviewerRequest> proInterviewerRequests = ProInterviewerRequest.find("byStatus", ProInterviewerRequestStatus.SUBMITTED).fetch();

        render(proInterviewerRequests);
    }

    @Restrict(RoleValue.APP_ADMIN_STRING)
    public static void acceptProInterviewerRequest(Long id){
        ProInterviewerRequest proInterviewerRequest = ProInterviewerRequest.find("byId", id).first();
        if (proInterviewerRequest != null) {
            User user = proInterviewerRequest.user;
            for (Category category: proInterviewerRequest.categories) {
                CategoryOverride categoryOverride = CategoryOverride.find("byCategoryAndUser", category, user).first();
                if (categoryOverride != null) {
                    categoryOverride.proInterviewerAllowed = true;
                } else {
                    categoryOverride = new CategoryOverride(user, category, null, true);
                }
                categoryOverride.save();
            }
            UserClassificationListener.rebuildClassificationLists(user);
            user.save();

            proInterviewerRequest.status = ProInterviewerRequestStatus.ACCEPTED;
            proInterviewerRequest.save();
        }
        viewProInterviewerRequests();
    }

    @Restrict(RoleValue.APP_ADMIN_STRING)
    public static void rejectProInterviewerRequest(Long id){
        ProInterviewerRequest proInterviewerRequest = ProInterviewerRequest.find("byId", id).first();
        if (proInterviewerRequest != null) {
            proInterviewerRequest.status = ProInterviewerRequestStatus.DECLINED;
            proInterviewerRequest.save();
        }
        viewProInterviewerRequests();
    }
}
