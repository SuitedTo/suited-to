package models.filter.activeinterview;

import controllers.Security;
import models.ActiveInterview;
import models.Company;
import models.User;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import play.db.jpa.JPA;

/**
 * Filter interviews by involved users (you're involved if you're the creator or interviewer).
 *
 * @author joel
 */
public class ByInvolved extends ActiveInterviewFilter<Long> {

	 public Predicate asPredicate(Root<ActiveInterview> root) {

	        CriteriaBuilder criteriaBuilder = JPA.em().getCriteriaBuilder();
	        Predicate criteria = (include.size() == 0) ? criteriaBuilder.conjunction() : criteriaBuilder.disjunction();


	        for (Long id : include) {
	            User user = User.findById(id);
	            if (user != null) {
	                criteria = criteriaBuilder.or(criteria, criteriaBuilder.equal(root.<User>get("user"), user));
	                criteria = criteriaBuilder.or(criteria, criteriaBuilder.equal(root.<User>get("interviewer"), user));
	            }
	        }

	        for (Long id : exclude) {
	        	User user = User.findById(id);
	            if (user != null) {
	                criteria = criteriaBuilder.and(criteria,
	                        criteriaBuilder.not(
	                                criteriaBuilder.equal(root.<User>get("user"), user)));
	                criteria = criteriaBuilder.and(criteria,
	                        criteriaBuilder.not(
	                                criteriaBuilder.equal(root.<User>get("interviewer"), user)));

	            }
	        }

	        return criteria;
	    }

    protected List<String> interpret(String key) {
        List<String> result = new ArrayList<String>();
        final User me = Security.connectedUser();
        if (key.equals("me")) {
            result.add(String.valueOf(me.id));
        } else if (key.equals("myCompany")) {
            Company myCompany = me.company;
            if (myCompany != null) {
                List<User> coworkers = myCompany.getUsers();
                if ((coworkers != null) && (coworkers.size() > 0)) {
                    for (User coworker : coworkers) {
                        result.add(String.valueOf(coworker.id));
                    }
                }
            }
        } else if (key.startsWith("company.")) {
            String idStr = key.substring("company.".length());
            long id = Long.parseLong(idStr);
            Company company = Company.findById(id);
            if (company != null) {
                List<User> coworkers = company.getUsers();
                if ((coworkers != null) && (coworkers.size() > 0)) {
                    for (User coworker : coworkers) {
                        result.add(String.valueOf(coworker.id));
                    }
                }
            }
        } else if (key.matches("user.[0-9]+")) {
            String idStr = key.substring("user.".length());
            long id = Long.parseLong(idStr);
            result.add(String.valueOf(id));
        }
        return result;
    }

    public static ByInvolved me() {
        ByInvolved instance = new ByInvolved();
        instance.include(String.valueOf(Security.connectedUser().id));
        return instance;
    }

    /**
     * @return Filter to include only interviews created by the company
     *         associated with the connected user. Null if connected user is not
     *         associated with a company.
     */
    public static ByInvolved myCompany() {
        User me = Security.connectedUser();
        Company myCompany = me.company;
        if (myCompany != null) {
            List<User> coworkers = myCompany.getUsers();
            if ((coworkers != null) && (coworkers.size() > 0)) {
                ByInvolved instance = new ByInvolved();
                for (User coworker : coworkers) {
                    instance.include(String.valueOf(coworker.id));
                }
                return instance;
            }
        }
        return null;
    }

    @Override
    public String getAttributeName() {
        return "user";
    }

    @Override
    protected String toString(Long id) {
        return String.valueOf(id);
    }

    @Override
    public Long fromString(String userId) {
        try {
            return Long.valueOf(userId);
        } catch (Exception e) {
            return null;
        }
    }

}
