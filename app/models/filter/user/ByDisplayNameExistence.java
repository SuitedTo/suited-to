package models.filter.user;

import controllers.Security;
import enums.RoleValue;
import models.User;
import play.db.jpa.JPA;
import play.mvc.With;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;


public class ByDisplayNameExistence extends UserFilter<Boolean> {

    private boolean exists;

    protected List<String> interpret(String key) {
        List<String> result = new ArrayList<String>();
        exists = fromString(key);
        return result;
    }

    @Override
    public Predicate asPredicate(Root<User> root) {

        Path<String> displayName = root.get("displayName");
        Predicate  criteria;
        if(exists) {
            criteria = JPA.em().getCriteriaBuilder().isNotNull(displayName);
        }
        else {
            criteria = JPA.em().getCriteriaBuilder().isNull(displayName);
        }

        return criteria;
    }

    @Override
    public String getAttributeName() {
        return "displayName";
    }

    @Override
    protected String toString(Boolean b) {
        return b.toString();
    }

    @Override
    public Boolean fromString(String str) {
        boolean fromStr;

        if(str.toLowerCase().equals("true"))
            fromStr = true;
        else if (str.toLowerCase().equals("false"))
            fromStr = false;
        else throw new RuntimeException("String was not 'true' or 'false'.");

        return fromStr;
    }
}