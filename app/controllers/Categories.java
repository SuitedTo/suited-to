package controllers;

import cache.KeyBuilder;

import com.google.gson.*;
import controllers.deadbolt.Deadbolt;
import controllers.deadbolt.Restrict;
import controllers.deadbolt.RestrictedResource;
import controllers.deadbolt.RoleHolderPresent;
import data.binding.types.FilterBinder;
import data.binding.types.QueryBinder;
import enums.RoleValue;
import java.lang.reflect.Type;
import models.Category;
import models.User;
import models.filter.category.*;
import models.query.QueryBase;
import models.query.QueryFilterListBinder;
import models.query.QueryResult;
import models.query.SearchQuery;
import models.query.category.AccessibleCategories;
import models.query.category.CategoryQuery;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import play.data.binding.As;
import play.data.validation.Required;
import play.jobs.Job;
import play.mvc.Util;
import play.mvc.With;
import play.utils.FastRuntimeException;
import utils.SafeStringArrayList;

import java.util.*;
import utils.EscapeUtils;

@With(Deadbolt.class)
@RoleHolderPresent
@RestrictedResource(name="")
public class Categories extends ControllerBase {
    
    private final static Gson INTERVIEW_BUILDER_SERIALIZER;
    
    static {
        GsonBuilder b = new GsonBuilder();
        b.registerTypeAdapter(Category.class, 
                new InterviewBuilderCategorySerializer());
        
        INTERVIEW_BUILDER_SERIALIZER = b.create();
    }
    
    /*##########################################################################
     * This is a block of stuff that should really go into Category, but a Play!
     * bug in Fixture causes a RuntimeException.  See https://play.lighthouseapp.com/projects/57987/tickets/1533-fixturesloadmodel-not-respecting-transient-javautilmaps.
     */
   
    public final static Map<String, Category.CategoryStatus> STRING_TO_CATEGORY =
            new HashMap<String, Category.CategoryStatus>();
 
    public final static Map<Category.CategoryStatus, List<Category.CategoryStatus>>
            VALID_STATUS_TRANSFORMATIONS =
                new EnumMap<Category.CategoryStatus, List<Category.CategoryStatus>>(
                    Category.CategoryStatus.class);
    
    public final static String VALID_STATUS_TRANSFORMATIONS_JSON;
    
    static {
        for (Category.CategoryStatus s : Category.CategoryStatus.values()) {
            STRING_TO_CATEGORY.put(s.toString(), s);
            
            VALID_STATUS_TRANSFORMATIONS.put(s, s.getValidChangeTargets());
        }
        
        //We do this manually rather than with gson because of the circular
        //nature of VALID_STATUS_TRANSFORMATIONS
        StringBuilder b = new StringBuilder();
        b.append("{");
        
        boolean firstCategory = true;
        for (Map.Entry<Category.CategoryStatus, List<Category.CategoryStatus>> category : 
                VALID_STATUS_TRANSFORMATIONS.entrySet()) {
            
            if (firstCategory) {
                firstCategory = false;
            }
            else {
                b.append(",");
            }
            
            b.append("\"" + category.getKey().toString() + "\":[");
            
            boolean firstTarget = true;
            for (Category.CategoryStatus validTarget : category.getValue()) {
                
                if (firstTarget) {
                    firstTarget = false;
                }
                else {
                    b.append(",");
                }
                
                b.append("\"" + validTarget.toString() + "\"");
            }
            
            b.append("]");
        }
        
        b.append("}");
        
        VALID_STATUS_TRANSFORMATIONS_JSON = b.toString();
    }
    
    /**
    * <p>A shortcut to checking <code>VALID_STATUS_TRANSFORMATIONS</code>.
    * Returns <code>true</code> <strong>iff</strong> <code>end</code>
    * appears in <code>start</code>'s list of valid change targets.</p>
    * @param start
    * @param end
    * @return 
    */
    public static boolean statusChangePossible(Category.CategoryStatus start, 
            Category.CategoryStatus end) {

        return (VALID_STATUS_TRANSFORMATIONS.get(start).contains(end));
    }
    
    /*
     * End stuff that should be in Category 
     *########################################################################*/
    
    /**
     * Parses a String of the format ["category1", "category2"] and builds a list of Category entities matching the
     * given Category Names.  Duplicates will be ignored.  New categories will be created (but not persisted) if no
     * matching category is found.
     * @param categories String containing Category names.
     * @return a List of Categories, may be an empty list but should not be null
     */
    public static List<Category> categoriesFromStandardRequestParam(String categories) {
        return categoriesFromStandardRequestParam(categories, true);
    }

    public static List<Category> categoriesFromStandardRequestParam(String categories, boolean createNew) {
    	List<Category> result = new ArrayList<Category>();
    	if (StringUtils.isNotEmpty(categories) && !"[]".equals(categories)) {
    		String[] categoryNames = categories.split(",");
    		for (String categoryName : categoryNames) {
    			
    			Category category = categoryFromName(categoryName, createNew);
    			if (category != null && !result.contains(category)) {
    				result.add(category);
    			}
    		}
    	}

    	return result;
    }
    
    public static Category categoryFromName(String categoryName, boolean createNew){
        Category category = null;

        if (StringUtils.isNotBlank(categoryName)) {
        	categoryName = StringUtils.replaceChars(categoryName, "\"[]", null).trim();
            category = getAccessibleCategoryByCaseInsensitiveExactName(categoryName);

            if (category == null && createNew) {
                category = new Category(categoryName);
                category.creator = Security.connectedUser();
                category.companyName = category.creator.company != null ? category.creator.company.name : null;
            }
        }
        return category;
    }

    /**
     * Looks for a single category matching the exact name but ignoring case.
     * @param categoryName Category Name to search for
     * @return A single category or null if none found that exactly matches the given name.
     */
    private static Category getAccessibleCategoryByCaseInsensitiveExactName(final String categoryName) {
        AccessibleCategories query = new AccessibleCategories();


        CategoryFilter filter = new ByExactName();
        filter.include(categoryName);
        List<CategoryFilter> queryFilters = new ArrayList<CategoryFilter>();
        queryFilters.add(filter);

        QueryFilterListBinder filteredQuery = new QueryFilterListBinder(query, queryFilters);

        SearchQuery search = new SearchQuery<Category>(filteredQuery, categoryName);
        search.init(null, null, 0, 1);

        QueryResult<Category> results = search.addFilter(new ByExactName()).executeQuery();
        if (results.getList().isEmpty()) {
            return null;
        } else {
            return results.getList().get(0);
        }
    }

    public static void getCategoryStatuses(String categories) {
        List<Category> categoryObjects = 
                categoriesFromStandardRequestParam(categories, false);
        
        Map<String, Object> statuses = new HashMap<String, Object>();
        for (Category category : categoryObjects) {
            statuses.put(category.name, category.status);
        }
        
        renderJSON(new Gson().toJson(statuses));
    }

    /**
     * A comma separated String of Category Names from a Collection of Categories
     * @param categories a Collection of Categories
     * @return comma separated String of just the names of the Categories ie. "categoryName1, categoryName2"
     */
    public static String getCommaSeparatedCategoryNames(final List<Category> categories){
        List<String> categoryNames = new ArrayList<String>(categories.size());
        for (int i = 0; i < categories.size(); i++) {
            Category category = categories.get(i);
            categoryNames.add(category.name);
        }

        return StringUtils.join(categoryNames, ", ");
    }

    /**
     * Gets a list of categories that are accessible to the currently logged in user filtering out any categories that
     * are already present in an input named "categories" and optionally searching on a partially matching search
     * term.
     * @param term Optional string to match Category names to.
     */
    public static void getCategoryList(String term) {
        AccessibleCategories query = new AccessibleCategories();
        
        //TODO : The only place this method is called 
        //       (views/tags/categories.html) there is no "categories" input
        //       being passed along via POST or parameter.  Once we have one,
        //       we might once again try and filter out "already selected"
        //       categories, as per the method comment.

        List<CategoryFilter> queryFilters = new ArrayList<CategoryFilter>();

        QueryFilterListBinder filteredQuery = new QueryFilterListBinder(query, queryFilters);

        String[] searchTerms = term.split(",");

        for (int i = 0; i < searchTerms.length; i++) {
            searchTerms[i] = EscapeUtils.safeSQLLikeString(searchTerms[i]);
        }

        if (searchTerms.length == 1) {
            term = searchTerms[0];
        }
        
        SearchQuery search = new SearchQuery<Category>(filteredQuery, searchTerms.length > 1 ? searchTerms[searchTerms.length -1] : term);
        search.init(null, null, 0, 10);

        QueryResult<Category> results = search.addFilter(new ByName()).executeQuery();

        /*filter out the User field from the JSON serialization.  It's causing an error in the serialization and it's
        just extra overhead here anyway*/
        Gson gson = new GsonBuilder().addSerializationExclusionStrategy(new ExclusionStrategy() {
           @Override
           public boolean shouldSkipField(FieldAttributes fieldAttributes) {
               String fieldName = fieldAttributes.getName();
               return fieldName.equals("creator");
           }

           @Override
           public boolean shouldSkipClass(Class<?> aClass) {
               return false;
           }
       }).create();
       renderJSON(gson.toJson(CategoryListAutoCompleteResult.buildResultList(results.getList())));

    }

    /**
     * Looks up categories by the input search string and returns a maximum of 10 results in JSON format
     * @param q text to filter on
     */
    public static void findCategoriesJSON(String q){
        
        q = EscapeUtils.safeSQLLikeString(q);
        
        List<Category> categories = Category.find("byNameIlike", "%" + q + "%").fetch(10);
        renderText(getCategoryNamesJSON(categories));
    }

    public static void findCategoriesJSONFullObject(String q){
        
        q = EscapeUtils.safeSQLLikeString(q);
        
        List<Category> categories = Category.find("byNameIlike",  "%" + q + "%").fetch(10);
        renderJSON(INTERVIEW_BUILDER_SERIALIZER.toJson(categories));
    }

    /**
     * A JSON-formatted String of Category Names from a Collection of Categories
     * @param categories a Collection of Categories
     * @return JSON-formatted String of just the names of the Categories ie. ["categoryName1", "categoryName2"]
     */
    @Util
    public static String getCategoryNamesJSON(List<Category> categories){
        List<String> categoryNames = new ArrayList<String>(categories.size());
        for (Category category : categories) {
            categoryNames.add(category.name);
        }
        return new Gson().toJson(categoryNames);

    }
    
    @Util
	@Deprecated
    public static void clearCachedNames(){
    	play.cache.Cache.delete(KeyBuilder.buildGlobalKey(Categories.class.getName() + ".allAsJSON"));
    }
    @Deprecated
    private static Job<String> updateCategoryNamesJob(){
    	return new Job<String>(){
    		public String doJobWithResult(){
    			List<Category> categories = Category.findAll();
    	        if(categories == null){
    	            categories = new ArrayList<Category>();
    	        }
    	        return Categories.getCategoryNamesJSON(categories);
    		}
    	};
    }

    /**
     * A JSON-formatted String of all Category Names
     * @return JSON-formatted String of just the names of the Categories ie. ["categoryName1", "categoryName2"]
     */
    @Deprecated
    public static String getCategoryNamesJSON(){
    	final String key = KeyBuilder.buildGlobalKey(Categories.class.getName() + ".allAsJSON");
    	Object cached = play.cache.Cache.get(key);
        if(cached == null){
        	try {
				String json = updateCategoryNamesJob().doJobWithResult();
				play.cache.Cache.set(key, json);
				return json;
			} catch (Exception e) {
				throw new FastRuntimeException(e.getMessage());
			}
        }else{
        	return String.valueOf(cached);
        }

    }

    /**
     * Checks to see if a category of the given name exists (case insensitive) and is accessible to the currently
     * logged in user.
     * @param name category name to check for.
     */
    public static void isExistingCategory(final String name) {
        renderText(StringUtils.isNotEmpty(name) && getAccessibleCategoryByCaseInsensitiveExactName(name) != null ? "true" : "false");
    }



    public static void add(@Required String name){
        Category category = Category.find("byName", name).first();

        if(category == null){
            category = new Category(name);
        }


        if (validation.hasErrors()) {
           response.status = 400;
           //todo: this is kindof hackish, we're just assuming the error was due to a missing name, but it should work
           //for now
           renderText("Category Name is required");
        }
        //the name must not be empty and must not match an existing category
        category.save();
    }

    public static void getStatusChangeOptions() {
        renderText(VALID_STATUS_TRANSFORMATIONS_JSON);
    }

    @Restrict(RoleValue.APP_ADMIN_STRING)
    public static void changeCategoryStatus(@Required Long categoryId,
                                            @Required String newStatusName) {

        //Note that the client interface should prevent us from passing in any
        //bad data, so we just output a helpful error message directly--it
        //doesn't need to be pretty.

        Category category = Category.findById(categoryId);

        if (category == null) {
            renderText("Null category id.");
        }

        Category.CategoryStatus newStatus = STRING_TO_CATEGORY.get(newStatusName);

        if (newStatus != null) {
            try {
                category.changeStatus(newStatus);
            }
            catch (IllegalArgumentException iae) {
                renderText("Illegal status change.  Cannot change from " +
                        category.status.toString() + " to " +
                        newStatusName +".");
            }
        }
        else {
            renderText("Bad status name: " + newStatusName);
        }

        list(null);
    }
    
    @Restrict(RoleValue.APP_ADMIN_STRING)
    @RestrictedResource(name = {"models.Category"}, staticFallback = true)
    public static void list(final Long categoryBeingMarkedAsDuplicateId) {
        Category categoryBeingMarkedAsDuplicate = null;
        String validChanges = VALID_STATUS_TRANSFORMATIONS_JSON;
        
        if(categoryBeingMarkedAsDuplicateId != null) {
            categoryBeingMarkedAsDuplicate = Category.findById(categoryBeingMarkedAsDuplicateId);
        }

        render(validChanges, categoryBeingMarkedAsDuplicate);
    }
    
    @Restrict(RoleValue.APP_ADMIN_STRING)
    public static void export() {
        render();
    }

    @Restrict(RoleValue.APP_ADMIN_STRING)
    @RestrictedResource(name = {"models.Category"}, staticFallback = true)
    public static void show(final Long id){

        if(id != null) {
            Category category = Category.findById(id);
            render(category);
        } else {
            render(new Category());
        }

    }

    @Restrict(RoleValue.APP_ADMIN_STRING)
    public static void delete(Long id){

        if(id != null) {
            Category category = Category.findById(id);
            if(category != null && category.questions.size() == 0) {
                category.delete();
            }
        }

        list(null);
    }

    @Restrict(RoleValue.APP_ADMIN_STRING)
    public static void save(Long id, @Required String categoryName){

        User user = Security.connectedUser();

        Category category = null;
        if(id != null) {
            category = Category.findById(id);
            category.name = categoryName;
        } else {
            category = new Category(categoryName);
            category.creator = user;
        }

        try {
            category.save();
        } catch(Exception e) {}

        list(null);

    }
    
    @Restrict(RoleValue.APP_ADMIN_STRING)
    @RestrictedResource(name = {"models.Category"}, staticFallback = true)
    public static void externalizeCategory(@Required Long id) {
    	if(validation.hasErrors()){
    		badRequest();
    	}
    	Category c = Category.findById(id);
    	if(c != null){
    		c.isAvailableExternally = true;
    		c.save();
    		ok();
    	}
    	notFound();
    }
    
    @Restrict(RoleValue.APP_ADMIN_STRING)
    @RestrictedResource(name = {"models.Category"}, staticFallback = true)
    public static void internalizeCategory(@Required Long id) {
    	if(validation.hasErrors()){
    		badRequest();
    	}
    	Category c = Category.findById(id);
    	if(c != null){
    		c.isAvailableExternally = false;
    		c.save();
    		ok();
    	}
    	notFound();
    }
    
    @Restrict(RoleValue.APP_ADMIN_STRING)
    @RestrictedResource(name = {"models.Category"}, staticFallback = true)
    public static void exportCategory(@Required Long id) {
    	if(validation.hasErrors()){
    		badRequest();
    	}
    	Category c = Category.findById(id);
    	if(c != null){
    		c.exportToPrep();
            ok();
    	}
    	notFound();
    }
    
    @Restrict(RoleValue.APP_ADMIN_STRING)
    @RestrictedResource(name = {"models.Category"}, staticFallback = true)
    public static void retractCategory(@Required Long id) {
    	if(validation.hasErrors()){
    		badRequest();
    	}
    	Category c = Category.findById(id);
    	if(c != null){
    		c.deleteFromPrep();
    		ok();
    	}
    	notFound();
    }
    
    /**
     * Combines the two categories by moving all references over to the categoryToKeep and deletes the other category.
     * @param categoryToDeleteId primary key for the Category to be deleted.
     * @param categoryToKeepId primary key for the Category that should remain.
     */
    @Restrict(RoleValue.APP_ADMIN_STRING)
    @RestrictedResource(name = {"models.Category"}, staticFallback = true)
    public static void consolidateCategories(final Long categoryToDeleteId, final Long categoryToKeepId) {
        Category categoryToKeep = Category.findById(categoryToKeepId);
        Category categoryToDelete = Category.findById(categoryToDeleteId);

        Category.consolidateCategories(categoryToKeep, categoryToDelete);

        list(null);
    }

    /**
     * Retrieves the list of Categories for the data tables JQuery plugin.
     * @param sSearch search term to use
     * @param sEcho used by Datatables plugin just echo back in the response
     * @param iSortCol_0 the column to sort by (optional)
     * @param sSortDir_0 the direction 'ASC' or 'DESC' to sort by
     * @param iDisplayStart equivalent to setFirstResult() in JPA
     * @param iDisplayLength equivalent to setMaxResults() in JPA
     */
    public static void categoryTableData(final String sSearch,
                                         final Long sEcho,
                                         final Integer iSortingCols,
                                         final Integer iSortCol_0,
                                         final String sSortDir_0,
                                         final Integer iDisplayStart,
                                         final Integer iDisplayLength,
                                         @As(binder=QueryBinder.class) CategoryQuery query,
                             			@As(value=",", binder=FilterBinder.class) List<CategoryFilter> filters) {

    	QueryBase toExecute = (query == null)?CategoryQuery.getDefaultQuery():query;

    	if(filters != null){
    		toExecute = new QueryFilterListBinder<Category,CategoryFilter>(toExecute,filters);			
    	}

    	if((sSearch != null) && (sSearch.length() > 0)){	
    		SearchQuery search = new SearchQuery<Category>(toExecute, sSearch);
    		search.init(iSortCol_0, sSortDir_0, iDisplayStart, iDisplayLength);

    		QueryResult<Category> searchResults = search
    				.addFilter(new ById())
    				.addFilter(new ByName())
    				.addFilter(new ByStatus())
                    .addFilter(new ByCompanyName())
    				.executeQuery();

    		renderJSON(new Gson().toJson(new DataTablesSource(searchResults.getList(), searchResults.getTotal(), sEcho)));
    	}else{

    		toExecute.init(iSortCol_0, sSortDir_0, iDisplayStart, iDisplayLength);
    		QueryResult<Category> results = toExecute.executeQuery();

    		renderJSON(new DataTablesSource(results.getList(), results.getTotal(), sEcho).toJson());
    	}

    }

    private static class InterviewBuilderCategorySerializer 
                implements JsonSerializer<Category> {

        public JsonElement serialize(Category t, Type type, 
                JsonSerializationContext jsc) {
            JsonObject result = new JsonObject();
            result.add("id", new JsonPrimitive(t.id));
            result.add("name", new JsonPrimitive(t.name));
            result.add("questionCount", new JsonPrimitive(t.questionCount));
            return result;
        }
        
    }
    
    /**
     * A representation of a collection of Categories designed to be returned as a JSON string to the Datatables plugin.
     */
    private static class DataTablesSource extends utils.DataTablesSource {

        DataTablesSource(final List<Category> categories, Long iTotalDisplayRecords, Long sEcho) {
            super(Category.count(), iTotalDisplayRecords, sEcho);

            for (Category category : categories) {
                SafeStringArrayList data = new SafeStringArrayList();
                data.add(category.id);
                data.add(StringEscapeUtils.escapeHtml(category.name));
                data.add(dateFormat.format(category.created));
                data.add(category.questionCount);

                if((category.status.equals(Category.CategoryStatus.PRIVATE) || category.status.equals(Category.CategoryStatus.NEW))
                    && category.creator != null && category.creator.company != null){
                    data.add(category.companyName);
                }
                else {
                    data.add("");
                }
                data.add(category.status.toString());
                data.add(category.isAvailableExternally);
                data.add(null);
                aaData.add(data);
            }
        }
    }

    /**
     * Used to build custom JSON objects to be rendered by JQuery UI autocomplete functions
     */
    private static class CategoryListAutoCompleteResult {
    	private long id;
        private String label;
        private long questionCount;


        private CategoryListAutoCompleteResult(Category category) {
        	this.id = category.id;
            this.label = category.name;
            this.questionCount = category.questionCount;

        }

        public static List<CategoryListAutoCompleteResult> buildResultList(List<Category> categories){
            List<CategoryListAutoCompleteResult> results = new ArrayList<CategoryListAutoCompleteResult>();
            for (Category category : categories) {
                results.add(new CategoryListAutoCompleteResult(category));
            }
            return results;
        }

    }


}
