package controllers;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import controllers.deadbolt.Deadbolt;
import controllers.deadbolt.Restrict;
import controllers.deadbolt.RestrictedResource;
import controllers.deadbolt.Restrictions;
import enums.AccountType;
import enums.RoleValue;
import models.Candidate;
import models.Company;
import org.apache.commons.lang.StringUtils;
import play.db.jpa.JPA;
import play.mvc.With;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

@With(Deadbolt.class)
@Restrictions({@Restrict(RoleValue.APP_ADMIN_STRING)})
public class Companies extends ControllerBase {
	
	/**
     * Navigate to basic company list page
     * @see models.tables.company.CompanyTable for actually populating the data
     */
	@RestrictedResource(name = {"models.Company"}, staticFallback = true)
    public static void list(){
        render();
    }

    @RestrictedResource(name = {"models.Company"}, staticFallback = true)
    public static void delete(Long id){
        Company company = Company.findById(id);
        company.delete();
        list();
    }

    /**
     * Renders a list of Company names as JSON.  Known usages include User invitation page
     * @param term Search term
     */
    public static void getCompanyList(String term) {
        if (term != null) {
            term = term.toLowerCase();
        }
        List<Company> companies = Company.find("byNameLike", "%" + term + "%").fetch(10);
        Gson gson = new GsonBuilder().setExclusionStrategies(new ExclusionStrategy() {

            public boolean shouldSkipClass(Class<?> clazz) {
                return false;
            }

            /**
             * Custom field exclusion goes here
             */
            public boolean shouldSkipField(FieldAttributes f) {
                return !"name".equals(f.getName());
            }

        }).create();
        renderJSON(gson.toJson(companies));
    }


}