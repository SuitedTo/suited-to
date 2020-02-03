package models.query.candidate;

import models.Candidate;
import play.db.jpa.JPA;
import utils.FilteredCriteriaHelper;

import javax.persistence.criteria.CriteriaBuilder;

/**
 * Created with IntelliJ IDEA.
 * User: perryspyropoulos
 * Date: 8/13/13
 * Time: 11:36 AM
 * To change this template use File | Settings | File Templates.
 */
public class CandidateQueryHelper extends FilteredCriteriaHelper {

    CriteriaBuilder builder = JPA.em().getCriteriaBuilder();
    RootTableKey candidateKey = addSourceEntity(Candidate.class);

    public CandidateQueryHelper() {
        super();
    }

}
