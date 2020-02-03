package models;

import controllers.Categories;
import enums.QuestionStatus;
import models.annotations.PropertyChangeListeners;
import models.prep.PrepCategory;
import newsfeed.listener.PublicCategoryNewsFeedListener;
import notifiers.Mails;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import play.data.validation.Required;
import play.db.jpa.JPABase;
import play.i18n.Messages;
import scheduler.TaskArgs;
import scheduler.TaskScheduler;
import tasks.StreetCredUpdateTask;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 * A grouping for Questions.
 * Created by IntelliJ IDEA.
 * User: rphutchinson
 * Date: 1/19/12
 * Time: 10:59 AM
 * To change this template use File | Settings | File Templates.
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@PropertyChangeListeners({PublicCategoryNewsFeedListener.class})
public class Category extends ModelBase implements Comparable, Restrictable {

    /**
     * The name of the category.
     */
    @Required
    public String name;

    /**
     * The number of questions that have been assigned this category.  This field exists to improve read performance
     * when displaying categories with the number of questions instead of having to Join to the Question entity
     * every time. Having this here also allows us to easily sort categories by number of questions. The tradeoff is
     * that this field must be explicitly updated whenever Questions are saved or deleted. Defined as a primitive type
     * to automatically default to 0 instead of null.
     */
    @Column(name = "question_count")
    public long questionCount;

    /**
     * The user that created this category. This may be null and should really only come into play when determining
     * who has access to private categories.
     */
    @ManyToOne
    @JoinColumn(name = "creator_id")
    public User creator;

    /**
     * The company of this category's creator. This information is redundant, as it can
     * be accessed through the creator. However, company needs to be a column in this table so that categories can be
     * sorted by the Datatables plugin.
     */
    public String companyName;

    public Boolean isAvailableExternally = false;
    
    
    /**
     * Keeping track of whether or not we have exported this category to
     * prep.
     */
    public Boolean exportedToPrep = false;

    /**
     * Holds a history of this category's workflows.
     */
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    @OrderBy("created DESC")
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    public List<CategoryWorkflow> workflows = new ArrayList<CategoryWorkflow>();

    @OneToMany(cascade = CascadeType.REMOVE)
    @JoinTable(name = "QUESTION_CATEGORY",
            joinColumns = {@JoinColumn(name = "categories_id", unique = true)},
            inverseJoinColumns = {@JoinColumn(name = "Question_id")})
    public List<Question> questions = new ArrayList<Question>();

    /**
     * The status of this Category
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    //Defaults NEW
    public CategoryStatus status = CategoryStatus.NEW;

    /**
     * Defines possible statuses for categories.
     */
    public enum CategoryStatus {
        /**
         * A Category that is new.
         */
        NEW {
            @Override
            public void prepareChangeTo(Category c, CategoryStatus newStatus) {
                if (!Categories.statusChangePossible(this, newStatus)) {
                    throw new IllegalArgumentException();
                }
            }

            @Override
            public List<CategoryStatus> getValidChangeTargets() {
                CategoryStatus[] targets = {NEW, PRIVATE, BETA, PUBLIC};
                return Collections.unmodifiableList(Arrays.asList(targets));
            }
        },

        /**
         * A Category that all users of the system should be able to see.
         */
        PUBLIC {
            @Override
            public void prepareChangeTo(Category c, CategoryStatus newStatus) {
                if (!Categories.statusChangePossible(this, newStatus)) {
                    throw new IllegalArgumentException();
                }
            }

            @Override
            public List<CategoryStatus> getValidChangeTargets() {
                CategoryStatus[] targets = {BETA, PUBLIC};
                return Collections.unmodifiableList(Arrays.asList(targets));
            }
        },

        /**
         * A Category with some restrictions or disclaimers and a modified review process
         * todo: update this comment when requirements have been finalized
         */
        BETA {
            @Override
            public void prepareChangeTo(Category c, CategoryStatus newStatus) {
                if (!Categories.statusChangePossible(this, newStatus)) {
                    throw new IllegalArgumentException();
                }
            }

            @Override
            public List<CategoryStatus> getValidChangeTargets() {
                CategoryStatus[] targets = {BETA, PUBLIC};
                return Collections.unmodifiableList(Arrays.asList(targets));
            }
        },

        /**
         * A category that only users in the same company as the user who
         * created the category can see.
         */
        PRIVATE {
            @Override
            public void prepareChangeTo(Category c, CategoryStatus newStatus) {
                if (!Categories.statusChangePossible(this, newStatus)) {
                    throw new IllegalArgumentException();
                }
            }

            @Override
            public List<CategoryStatus> getValidChangeTargets() {
                CategoryStatus[] targets = {NEW, PRIVATE, BETA, PUBLIC};
                return Collections.unmodifiableList(Arrays.asList(targets));
            }
        };

        @Override
        public String toString() {
            return Messages.get(getClass().getName() + "." + name());
        }

        /**
         * <p>Performs the necessary checks and any related database changes
         * required to change <code>category</code> from its current status,
         * which must be <code>this</code>, to <code>newStatus</code>. There are
         * some status transformations that are not permitted.  If this status
         * cannot be updated to <code>newStatus</code>, this method throws an
         * <code>IllegalArgumentException</code>.</p>
         * <p/>
         * <p>Note that this method does not actually change the status of
         * <code>category</code>, nor {@link #save() save() this category.</p>
         *
         * @param category  The category to change to the new status, required to
         *                  currently be of <code>this</code> status.
         * @param newStatus The new status for the category.
         * @throw IllegalArgumentException If a category cannot be changed from
         * <code>this</code> status to <code>newStatus</code>.
         */
        public abstract void prepareChangeTo(Category category,
                                             CategoryStatus newStatus);

        /**
         * <p>Returns an immutable list of <code>CategoryStatus</code>s that
         * this category status can be changed to.</p>
         */
        public abstract List<CategoryStatus> getValidChangeTargets();
    }

    /**
     * Empty constructor for test purposes.
     */
    public Category(){
    }

    /**
     * Creates a new Category with the given name
     *
     * @param name Name of the category.
     */
    public Category(String name) {
        this.name = name;
    }

    /**
     * Creates a new Category with the given name and the
     * creator's company.
     *
     * @param name Name of the category.
     * @param creator User to grab the category's company from
     */
    public Category(String name, User creator) {
        this.name = name;
        companyName = creator.company != null ? creator.company.name : null;
    }

    /**
     * Gets a list of all the Questions associated with this Category.
     *
     * @return A list of Questions with with the given Category.
     */
    public List<Question> getQuestions() {
    	JPAQuery query = Question.find("byCategory", this);
    	query.query.setHint("org.hibernate.cacheable", true);
        return query.fetch();
    }

    /**
     * Gets a list of all the Users who are able to review Questions in this Category.
     *
     * @return A list of Users with with the given Category.
     */
    public List<User> getReviewers() {
        Query query = em().createQuery("select u from User u where :category in elements(u.reviewCategories)");
        query.setParameter("category", this);
        query.setHint("org.hibernate.cacheable", true);
        return query.getResultList();
    }

    public List<User> getProInterviewers(){
        Query query = em().createQuery("select u from User u where :category in elements(u.proInterviewerCategories)");
        query.setParameter("category", this);
        query.setHint("org.hibernate.cacheable", true);
        return query.getResultList();
    }


    /**
     * Categories displayed by Name field.
     *
     * @return The name of the Catetory.
     */
    @Override
    public String toString() {
        return name + " (" + questionCount + ")";
    }

    /**
     * Defines how Categories should be sorted (by Name).
     *
     * @param c Category to compare to
     * @return int
     */
    @Override
    public int compareTo(final Object c) {
        if (c == null) {
            return 0;
        }

        return name.compareTo(((Category) c).name);
    }

    public void updateQuestionCount() {
        this.questionCount = Question.count("category = ? and active = true and status = ?", this, QuestionStatus.ACCEPTED);

    }

    /**
     * <p>Changes this category from its current status to a new one.  Some
     * status transformations are not permitted.  If it is not possible to
     * change the category to the requested status, throws an
     * <code>IllegalArgumentException</code>.</p>
     *
     * @param newStatus The requested new status.
     * @throw IllegalArgumentException If a category cannot be changed from
     * this category's current status to <code>newStatus</code>.
     */
    public void changeStatus(CategoryStatus newStatus) {
        status.prepareChangeTo(this, newStatus);
        CategoryStatus oldStatus = status;
        status = newStatus;
        if(creator != null) {
            Mails.categoryChangeNotification(this, oldStatus, newStatus);
            updateWorkflow(newStatus);
        }
        save();
    }

    /**
     * Creates a new CategoryWorkflow for this question with the given data and adds that workflow object to the category's
     * list of workflows.  Sets the question's categoryStatus field to the value of the status argument.  Does not perform
     * any persistence operations but returns the Workflow object back to the caller in case the caller needs to save
     * the workflow independently of cascading from a save to the Category itself.
     */
    public CategoryWorkflow updateWorkflow(CategoryStatus status){
       CategoryStatus statusFrom = null;
        if (this.hasBeenSaved()) {
            CategoryWorkflow previousWorkflow = workflows.isEmpty() ? null : workflows.get(0);
            if (previousWorkflow != null) {
                statusFrom = previousWorkflow.status;
            }
        }

        CategoryWorkflow workflow = new CategoryWorkflow(this);
        workflow.statusFrom = statusFrom;
        workflow.status = status;
        this.workflows.add(workflow);

        return workflow;
    }

    /**
     * Combines two categories by re-assigning all Questions and reviewer privileges from categoryToDelete to
     * categoryToKeep and deletes the categoryToDelete.  This method handles all persistence itself.
     *
     * @param categoryToKeep   The Category to keep after the consolidation.
     * @param categoryToDelete The category to delete after the consolidation
     */
    public static void consolidateCategories(final Category categoryToKeep, 
                final Category categoryToDelete) {

        /*update all the category overrides. This is how manual reviewer and pro interviewer is assigned. If an override
        * does not already exist for the categoryToKeep and user then update the category on the override to the
        * categoryToKeep otherwise just delete this override.  This logic isn't Perfect since the individual settings on
        * the two existing overrides might conflict but the impact of just deleting the duplicate one should be minimal.*/
        List<CategoryOverride> overrides = CategoryOverride.find("byCategory", categoryToDelete).fetch();
        for (CategoryOverride override : overrides) {
            boolean existing = CategoryOverride.count("byCategoryAndUser", categoryToKeep, override.user) > 0;
            if(existing){
                override.delete();
            } else {
                override.category = categoryToKeep;
                override.save();
            }
        }

        //update all the questions
        List<Question> questionsToUpdate = categoryToDelete.getQuestions();
        for (Question question : questionsToUpdate) {
            question.category = categoryToKeep;
            question.save();
        }

        //update all reviewers
        List<User> usersToUpdate = categoryToDelete.getReviewers();
        for (User user : usersToUpdate) {
            if (!user.reviewCategories.contains(categoryToKeep)) {
                user.reviewCategories.add(categoryToKeep);
            }
            
            user.reviewCategories.remove(categoryToDelete);
            user.save();
        }

        //update pro interviewers
        List<User> proInterviewerUsersToUpdate = categoryToDelete.getProInterviewers();
        for (User user : proInterviewerUsersToUpdate) {
            if (!user.proInterviewerCategories.contains(categoryToKeep)) {
                user.proInterviewerCategories.add(categoryToKeep);
            }

            user.proInterviewerCategories.remove(categoryToDelete);
            user.save();
        }

        /*Delete any badges associated with this category. Assumes that Ids are unique across the database which at this
        * point in time is an accurate assumption. Even if not we're just deleting badges not affecting functionality
        * that many (if any) users actually care about.*/
        List<UserBadge> badgesToDelete = UserBadge.find("bySubjectIdsLike", "%" + categoryToDelete.id + "%").fetch();
        for (UserBadge userBadge : badgesToDelete) {
            userBadge.delete();
        }

        //delete the old category
        categoryToDelete.delete();
    }

    @Override
    public boolean hasAccess(final User user) {
        //todo: implement once Private catgories are introduced
        return true;
    }

    public void exportToPrep(){
    	final String id = String.valueOf(this.id);
    	final String categoryName = this.name;
    	final String companyName = this.companyName;
    	new play.jobs.Job(){
    		public void doJob(){
    			models.prep.PrepCategory pc = new PrepCategory();
    			pc.categoryId = id;
    	    	pc.name = categoryName;
    	    	pc.companyName = companyName;
    	    	pc.save();
    		}
    	}.now();
    	exportedToPrep = true;
    	save();
    }
    
    public void deleteFromPrep(){
    	final String categoryName = this.name;
    	new play.jobs.Job(){
    		public void doJob(){
    			models.prep.PrepCategory pc = PrepCategory.find.where().eq("name", categoryName).findUnique();
    	    	if(pc != null){
    	    		pc.delete();
    	    	}
    		}
    	}.now();
    	
    	exportedToPrep = false;
    	save();
    }

    @Override
    public <T extends JPABase> T save() {
        T result =  super.save();
        //Temporary call to update cache. Can remove this call after we
        //remove the Categories.getCategoryNamesJSON() method.
        Categories.clearCachedNames();
        
        return result;
    }

    @PostPersist
    @PostUpdate
    private void recalculateStreetCred(){
        if(this.creator != null) {
            TaskArgs args = new TaskArgs();
            args.add(StreetCredUpdateTask.USER_ID_ARG, this.creator.id);
            TaskScheduler.schedule(StreetCredUpdateTask.class, args, CronTrigger.getASAPTrigger());
        }
    }


    @Override
    public <T extends JPABase> T delete() {
        //Temporary call to update cache. Can remove this call after we
        //remove the Categories.getCategoryNamesJSON() method.
        Categories.clearCachedNames();
        
        return super.delete();
    }
}
