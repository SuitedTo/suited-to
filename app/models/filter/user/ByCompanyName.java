package models.filter.user;

import models.User;
import models.Company;
import play.db.jpa.JPA;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;


public class ByCompanyName extends UserFilter<String> {

    public Predicate asPredicate(Root<User> root) {

        CriteriaBuilder criteriaBuilder = JPA.em().getCriteriaBuilder();
        Predicate criteria = (include.size() == 0) ? criteriaBuilder.conjunction() : criteriaBuilder.disjunction();


        for (String name : include) {
            List<Company> companies = Company.find("byNameIlike", "%" + name + "%").fetch();
            if (companies != null) {
                for (Company company : companies) {
                    criteria = criteriaBuilder.or(criteria, criteriaBuilder.equal(root.get("company"), company));
                }
            }
        }

        for (String name : exclude) {
            List<Company> companies = Company.find("byNameIlike", "%" + name + "%").fetch();
            if (companies != null) {
                for (Company company : companies) {
                    criteria = criteriaBuilder.and(criteria,
                            criteriaBuilder.not(
                                    criteriaBuilder.isMember(company, root.<List<Company>>get("companies"))));
                }
            }
        }

        return criteria;
    }

    public boolean willAccept(User user) {

        final Company company = user.company;
        if ((user != null) && (company != null)) {

            if (!include.contains(company.name)) {
                return false;
            }
            if (exclude.contains(company.name)) {
                return false;
            }

            return true;
        }
        return false;
    }

    @Override
    public String getAttributeName() {
        return "company" +
                "";
    }

    @Override
    protected String toString(String name) {
        return name;
    }

    @Override
    public String fromString(String name) {
        return name;
    }

}
