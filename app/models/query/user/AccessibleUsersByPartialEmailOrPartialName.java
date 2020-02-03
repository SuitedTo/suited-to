package models.query.user;

import enums.UserStatus;

import javax.persistence.criteria.Expression;


/**
 * Created with IntelliJ IDEA.
 * User: Mike Havens
 * Date: 8/15/12
 * Time: 3:44 PM
 * To change this template use File | Settings | File Templates.
 */
public class AccessibleUsersByPartialEmailOrPartialName  extends AccessibleUsersHelper {

    public AccessibleUsersByPartialEmailOrPartialName(String nameOrEmail) {
        super();

        Expression name = builder.lower(this.<String>field(userKey, "fullName"));
        Expression email = builder.lower(this.<String>field(userKey, "email"));
        Expression active = builder.lower(this.<String>field(userKey, "status"));

        Expression<Boolean> nameLike = builder.like(name, "%" + nameOrEmail.toLowerCase() + "%");
        Expression<Boolean> emailLike = builder.like(email, "%" + nameOrEmail.toLowerCase() + "%");
        Expression<Boolean> activeOnly = builder.equal(active, UserStatus.ACTIVE);

        Expression<Boolean> condition;
        if(user != null) {
            Expression displayName = builder.lower(this.<String>field(userKey, "displayName"));
            Expression<Boolean> displayNameLike = builder.like(displayName, "%" + nameOrEmail.toLowerCase() + "%");
            condition = builder.and(builder.or(emailLike, builder.or(nameLike, displayNameLike)), activeOnly);
        }
        else {
            condition = builder.and(builder.or(emailLike, nameLike), activeOnly);
        }
        addCondition(condition);


    }
}
