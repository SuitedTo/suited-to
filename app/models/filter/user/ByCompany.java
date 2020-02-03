package models.filter.user;

import controllers.Security;
import models.Company;
import models.User;
import play.db.jpa.JPA;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

/**
 * Filter user by company
 *
 * @author joel
 */
public class ByCompany extends UserFilter<Long> {

    protected List<String> interpret(String key) {
        List<String> result = new ArrayList<String>();
        final User me = Security.connectedUser();
        if(key.equals("myCompany") && me != null && me.company != null) {
            result.add(String.valueOf(me.company.id));
        }

        return result;
    }

    public Predicate asPredicate(Root<User> root) {

        CriteriaBuilder criteriaBuilder = JPA.em().getCriteriaBuilder();
        Predicate criteria = (include.size() == 0) ? criteriaBuilder.conjunction() : criteriaBuilder.disjunction();


        for (Long id : include) {
            Company company = Company.findById(id);
            if (company != null) {
                criteria = criteriaBuilder.or(criteria, criteriaBuilder.equal(root.<Company>get("company"), company));
            }
        }

        for (Long id : exclude) {
        	Company company = Company.findById(id);
            if (company != null) {
                criteria = criteriaBuilder.and(criteria,
                        criteriaBuilder.not(
                        		criteriaBuilder.equal(root.<Company>get("company"), company)));

            }
        }

        return criteria;
    }

    public boolean willAccept(User user) {
        if ((user != null) && (user.company != null)) {

            if (!include.contains(user.company.id)) {
                return false;
            }
            if (exclude.contains(user.company.id)) {
                return false;
            }

            return true;
        }
        return false;
    }

    @Override
    public String getAttributeName() {
        return "company";
    }

    @Override
    protected String toString(Long id) {
        return String.valueOf(id);
    }

    @Override
    public Long fromString(String idStr) {
        return Long.parseLong(idStr);
    }

}
