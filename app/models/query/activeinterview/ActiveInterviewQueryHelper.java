package models.query.activeinterview;

import models.ActiveInterview;
import play.db.jpa.JPA;
import utils.FilteredCriteriaHelper;

import javax.persistence.criteria.CriteriaBuilder;

public class ActiveInterviewQueryHelper extends FilteredCriteriaHelper {

    public ActiveInterviewQueryHelper() {
        CriteriaBuilder builder = JPA.em().getCriteriaBuilder();

        TableKey key = addSourceEntity(ActiveInterview.class);
    }

}
