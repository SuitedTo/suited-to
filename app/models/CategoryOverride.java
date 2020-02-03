package models;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 * Created with IntelliJ IDEA.
 * User: swilly
 * Date: 2012/12/4
 * Time: 11:01
 * To change this template use File | Settings | File Templates.
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class CategoryOverride extends ModelBase {
    @ManyToOne
    public User user;

    @ManyToOne
    public Category category;

    public Boolean reviewerAllowed;

    public Boolean proInterviewerAllowed;


    public CategoryOverride(User user, Category category, Boolean reviewerAllowed, Boolean proInterviewerAllowed) {
        super();

        this.user = user;
        this.category = category;
        this.reviewerAllowed = reviewerAllowed;
        this.proInterviewerAllowed = proInterviewerAllowed;
    }

    public static Boolean isReviewerAllowed(User user, Category category) {
        Boolean result = null;
        for (CategoryOverride categoryOverride : user.categoryOverrides) {
            if (categoryOverride.category == category) {
                result = categoryOverride.reviewerAllowed;
            }
        }
        return result;
    }

    public static Boolean isProInterviewerAllowed(User user, Category category) {
        Boolean result = null;
        for (CategoryOverride categoryOverride : user.categoryOverrides) {
            if (categoryOverride.category == category) {
                result = categoryOverride.proInterviewerAllowed;
            }
        }
        return result;
    }
}