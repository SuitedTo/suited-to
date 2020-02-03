package models.filter.interview;

import controllers.Security;
import models.Company;
import models.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Filter questions by creator.
 *
 * @author joel
 */
public class ByCreator extends InterviewFilter<User> {


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

    public static ByCreator me() {
        ByCreator instance = new ByCreator();
        instance.include(String.valueOf(Security.connectedUser().id));
        return instance;
    }

    /**
     * @return Filter to include only interviews created by the company
     *         associated with the connected user. Null if connected user is not
     *         associated with a company.
     */
    public static ByCreator myCompany() {
        User me = Security.connectedUser();
        Company myCompany = me.company;
        if (myCompany != null) {
            List<User> coworkers = myCompany.getUsers();
            if ((coworkers != null) && (coworkers.size() > 0)) {
                ByCreator instance = new ByCreator();
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
    protected String toString(User user) {
        if (user == null) {
            return null;
        }
        return String.valueOf(user.id);
    }

    @Override
    public User fromString(String userId) {
        try {
            return User.findById(Long.parseLong(userId));
        } catch (Exception e) {
            return null;
        }
    }

}
