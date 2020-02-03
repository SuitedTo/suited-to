package models.query.prepinterview;

import javax.persistence.criteria.CriteriaBuilder;

import models.prep.PrepInterview;
import play.db.jpa.JPA;
import utils.FilteredCriteriaHelper;

public class PrepInterviewQueryHelper extends FilteredCriteriaHelper {
    protected CriteriaBuilder builder;
    protected RootTableKey rootTableKey;

    public PrepInterviewQueryHelper() {
        super();
        builder = JPA.em().getCriteriaBuilder();
        rootTableKey = addSourceEntity(PrepInterview.class);
    }
}