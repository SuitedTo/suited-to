/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.filter.interview;

/**
 *
 * @author hamptos
 */
public class ByActive extends InterviewFilter<Boolean> {

    @Override
    public String getAttributeName() {
        return "active";
    }

    @Override
    protected String toString(Boolean active) {
        if (active == null) {
            return null;
        }
        
        return active.toString();
    }

    @Override
    public Boolean fromString(String isActive) {
        try {
            return Boolean.valueOf("true".equals(isActive));
        } catch (Exception e) {
            return null;
        }
    }
    
}
