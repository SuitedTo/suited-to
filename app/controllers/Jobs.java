package controllers;


import com.google.gson.Gson;
import controllers.deadbolt.*;
import enums.RoleValue;
import models.Candidate;
import models.Category;
import models.Job;
import models.User;
import org.apache.commons.lang.StringUtils;
import org.hibernate.exception.ConstraintViolationException;

import play.data.validation.Required;
import play.data.validation.Validation;
import play.db.jpa.JPA;
import play.mvc.With;
import utils.SafeStringArrayList;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

@With(Deadbolt.class)
@RoleHolderPresent
@RestrictedResource(name="")
public class Jobs extends ControllerBase {

    @Restrictions({ @Restrict({RoleValue.APP_ADMIN_STRING}),
                    @Restrict({RoleValue.COMPANY_ADMIN_STRING}),
                    @Restrict({RoleValue.QUESTION_ENTRY_STRING})})
    public static void list(){render();}

    @Restrictions({ @Restrict({RoleValue.APP_ADMIN_STRING}),
                    @Restrict({RoleValue.COMPANY_ADMIN_STRING}),
                    @Restrict({RoleValue.QUESTION_ENTRY_STRING})})
    public static void show(Long id) {
        Job job = null;
        if(id != null) {
            job = Job.findById(id);
            String existingCategories = job != null ? Categories.getCommaSeparatedCategoryNames(job.categories) : null;
            render(job, existingCategories);
        } 
        
        render();
    }

    @Restrictions({@Restrict({RoleValue.APP_ADMIN_STRING}), @Restrict({RoleValue.COMPANY_ADMIN_STRING})})
    public static void save(Long id, @Required String jobTitle, String categories, String description){
        User user = Security.connectedUser();

        boolean create = false;
        Job job = null;
        if(id == null) {
            job = new Job(user.company);
            create = true;

        } else {
            job = Job.findById(id);
        }

        job.name = jobTitle;

        job.categories = Categories.categoriesFromStandardRequestParam(categories, false);

        job.description = description;

        if(validation.hasErrors()) {
            String existingCategories = job.categories != null ? new Gson().toJson(job.categories) : null;
            render("@show", job, existingCategories);
        }

        try{
        	job.save();
        } catch(Exception e){
			if(e.getCause() instanceof ConstraintViolationException){
				
				//At this time the only constraint that could have been violated is the unique(name, company_id) constraint
				Validation.addError("name", "Looks like that job name already exists! Please change the name to something a little different.");
				String existingCategories = job.categories != null ? new Gson().toJson(job.categories) : null;
				if(create){
					job.id = null;
				}
	            render("@show", job, existingCategories);
			}
		}

        list();
    }

    @Restrictions({ @Restrict({RoleValue.APP_ADMIN_STRING}),
                    @Restrict({RoleValue.COMPANY_ADMIN_STRING}),
                    @Restrict({RoleValue.QUESTION_ENTRY_STRING})})
    public static void delete(Long id){
        Job job = Job.findById(id);
        if(job != null) {
            job.delete();
        }
        
        list();   
    }
}
