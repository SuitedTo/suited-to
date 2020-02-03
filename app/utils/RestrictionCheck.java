/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import models.deadbolt.AccessResult;

/**
 *
 * @author hamptos
 */
public interface RestrictionCheck {
    public AccessResult check(String key);
}
