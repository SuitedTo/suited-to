package models.tables.user;

import models.filter.user.ByStatus;
import com.google.gson.Gson;
import enums.UserStatus;
import models.Category;
import models.User;
import models.query.user.AllUsersHelper;
import models.tables.FilteredCriteriaHelperTable;
import utils.CriteriaHelper.RootTableKey;
import utils.ObjectTransformer;

import java.util.*;


public class PublicUserTable extends FilteredCriteriaHelperTable {

    public PublicUserTable() {
        super( new AllUsersHelper());

        RootTableKey root = (RootTableKey) getSoleEntityKey(User.class);

        addColumn(root, UserToPictureURL.INSTANCE);
        addColumn(root, "displayName");
        addColumn(root, "streetCred");
        addColumn(root, UserToReviewerBoolean.INSTANCE);
        addColumn(root, UserToProInterviewerBoolean.INSTANCE);
        addColumn(root, UserToTopCategories.INSTANCE);
        addColumn(root, "id");

        includeInSearch(field(root, "displayName", String.class));
        filterNotActive();

    }

    /**
     * Filter to only users of UserStatus.ACTIVE
     */
    private void filterNotActive() {
        ByStatus byActiveFilter = new ByStatus();
        byActiveFilter.include(UserStatus.ACTIVE.toString());
        addFilter(byActiveFilter);
    }

    @Override
    public boolean canAccess(User u) {
        return true;
    }

    private static class UserToPictureURL implements ObjectTransformer {

        public static final ObjectTransformer INSTANCE = new UserToPictureURL();

        public Object transform(Object input) {
            User user = (User) input;
            
            Object result;
            if (user.isProfilePicturePublic()) {
                result = user.getPublicPictureUrl();
            }
            else {
                result = null;
            }
            
            return result;
        }
    }

    private static class UserToReviewerBoolean implements ObjectTransformer {

        public static final ObjectTransformer INSTANCE = new UserToReviewerBoolean();

        public Object transform(Object input) {
            User user = (User) input;
            
            Object result;
            if (user.isUserStatusLevelPublic()) {
                result = user.isReviewer() ? "true" : "false"; 
            }
            else {
                result = null;
            }
            
            return result;
        }
    }

    private static class UserToProInterviewerBoolean implements ObjectTransformer {

        public static final ObjectTransformer INSTANCE = new UserToProInterviewerBoolean();

        public Object transform(Object input) {
            User user = (User) input;

            Object result;
            if (user.isUserStatusLevelPublic()) {
                result = user.isProInterviewer() ? "true" : "false";
            }
            else {
                result = null;
            }

            return result;
        }
    }

    private static class UserToTopCategories implements ObjectTransformer {

        public static final ObjectTransformer INSTANCE = new UserToTopCategories();
        private static final Gson gson = new Gson();
        private static final int maxResults = 3;

        public Object transform(Object input) {
            String result = "";

            User user = (User) input;

            if (user.isReviewer() && user.isCategorySetPublic()) {

                /**
                 *  Get the user's three most active categories ranked
                 *  by both question review and submission
                 */
                List<String> categoryNames = user.getTopCategoryNames(maxResults);

                if(categoryNames.size() < maxResults) {
                    /**
                     *  Check for manually added review categories.
                     *  the above query will fail to find them if
                     *  the user has not yet reviewed any questions
                     *  belonging to them.
                     */
                    Iterator<Category> iterator = user.reviewCategories.iterator();
                    while(categoryNames.size() < maxResults && iterator.hasNext()) {
                        String nextName = iterator.next().name;
                        if(!categoryNames.contains(nextName)) {
                            categoryNames.add(nextName);
                        }
                    }
                }

                for(int i = 0; i < categoryNames.size(); i++) {
                    result += categoryNames.get(i);
                    if(i < categoryNames.size()-1) result += ", ";
                }
            }

            return result;
        }
    }
}
