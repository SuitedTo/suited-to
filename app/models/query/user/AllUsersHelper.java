package models.query.user;

import models.Feedback;
import models.User;
import play.db.jpa.JPA;
import utils.FilteredCriteriaHelper;

import javax.persistence.criteria.CriteriaBuilder;


public class AllUsersHelper extends FilteredCriteriaHelper {

    private TableKey feedbackKey = addSourceEntity(User.class);
    private CriteriaBuilder builder = JPA.em().getCriteriaBuilder();

    public AllUsersHelper() {
        addCondition(builder.conjunction());
    }

}
