package models;

import models.prep.PrepUser;

/**
 * Created with IntelliJ IDEA for sparc-interview
 * User: phutchinson
 * Date: 3/15/13
 * Time: 4:47 PM
 */
public interface PrepRestrictable  {

    public boolean hasAccess(PrepUser user);
}
