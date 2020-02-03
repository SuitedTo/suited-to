package controllers.prep.rest;


import models.prep.PrepJob;

public class Jobs extends PrepController{
    public static void get(Long id) {
        if (id == null) {
            emptyObject();
        }
        PrepJob prepJob = PrepJob.find.byId(id);
        if (prepJob != null) {
            renderRefinedJSON(prepJob.toJsonObject());
        }
        emptyObject();
    }
}
