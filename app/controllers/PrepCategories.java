package controllers;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import controllers.deadbolt.Deadbolt;
import controllers.deadbolt.Restrict;
import controllers.deadbolt.RestrictedResource;
import controllers.deadbolt.Restrictions;
import enums.RoleValue;
import models.Category;
import models.filter.category.CategoryFilter;
import models.prep.PrepCategory;
import models.query.QueryFilterListBinder;
import models.query.QueryResult;
import models.query.SearchQuery;
import models.query.prepcategory.PrepCategoryQueryHelper;
import play.db.jpa.JPA;
import play.mvc.With;
import utils.EscapeUtils;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

@With(Deadbolt.class)
@Restrictions({@Restrict(RoleValue.APP_ADMIN_STRING)})
public class PrepCategories extends ControllerBase {
    @RestrictedResource(name = {"models.prep.PrepCategory"}, staticFallback = true)
    public static void list(){
        render();
    }

    public static void getPrepCategoryList(String term) {

        EntityManager em = JPA.em();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<PrepCategory> cq = cb.createQuery(PrepCategory.class);

        Root<PrepCategory> pc = cq.from(PrepCategory.class);
        Path name = pc.get("name");

        Predicate criteria = cb.conjunction();
        criteria = cb.and(criteria, cb.like(cb.upper(name), "%" + term.toUpperCase() + "%"));
        cq.where(criteria);

        cq.orderBy(cb.asc(name));
        TypedQuery<PrepCategory> query = em.createQuery(cq);
        query.setMaxResults(10);
        query.setFirstResult(0);

        List<PrepCategory> results = query.getResultList();
        Gson gson = new GsonBuilder().create();
        renderJSON(gson.toJson(PrepCategoryListAutoCompleteResult.buildResultList(results)));

    }

    private static class PrepCategoryListAutoCompleteResult {
        private long id;
        private String label;

        private PrepCategoryListAutoCompleteResult(PrepCategory prepCategory) {
            this.id = prepCategory.id;
            this.label = prepCategory.name;

        }

        public static List<PrepCategoryListAutoCompleteResult> buildResultList(List<PrepCategory> prepCategories){
            List<PrepCategoryListAutoCompleteResult> results = new ArrayList<PrepCategoryListAutoCompleteResult>();
            for (PrepCategory prepCategory : prepCategories) {
                results.add(new PrepCategoryListAutoCompleteResult(prepCategory));
            }
            return results;
        }

    }
}
