/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package enums;

import play.i18n.Messages;

/**
 *
 * @author hamptos
 */
public enum FeedbackSummary {
    HIRE, MAYBE_HIRE, MORE_INTERVIEWS, NO_HIRE;
    
    @Override
    public String toString() {
        return Messages.get(
                "feedback.summary." + super.toString());
    }
    
    public static FeedbackSummary getDefault() {
        return HIRE;
    }
}
