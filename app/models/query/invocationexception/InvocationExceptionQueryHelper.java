package models.query.invocationexception;

import javax.persistence.criteria.CriteriaBuilder;

import models.InvocationException;
import play.db.jpa.JPA;
import utils.FilteredCriteriaHelper;

	public class InvocationExceptionQueryHelper extends FilteredCriteriaHelper {
	    protected CriteriaBuilder builder;
	    protected RootTableKey rootTableKey;

	    public InvocationExceptionQueryHelper() {
	        super();
	        builder = JPA.em().getCriteriaBuilder();
	        rootTableKey = addSourceEntity(InvocationException.class);
	    }
	}
