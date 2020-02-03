/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

import java.security.SecureRandom;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import play.data.validation.Required;

/**
 * <p>This represents a token granting a certain e-mail address the right to
 * provide feedback for a particular candidate without logging in.  It should be
 * one-time-use only and should also expire after a period of time.</p>
 */
@Entity
public class TemporaryFeedbackAuthorization extends ModelBase {
    
    @Required
    @ManyToOne
    public Candidate candidate;
    
    @Required
    public String email;
    
    @ManyToOne
    public ActiveInterview activeInterview = null;

    public int nonce;
    
    /**
     * <p>Creates a new temporary authorization token to provide one-time 
     * feedback for <code>candidate</code>.  This constructor will save the
     * object to the database as well.</p>
     */
    public TemporaryFeedbackAuthorization(Candidate candidate, String email,
                ActiveInterview regarding) {
        
        SecureRandom random = new SecureRandom();
        
        byte[] bytes = new byte[4];
        random.nextBytes(bytes);
        
        long value = 0;
        for (int i = 0; i < 4; i++) {
            value += ((long) bytes[i] & 0xffL) << (8 * i);
        }
        
        nonce = (int) value;
        
        this.candidate = candidate;
        this.email = email;
        this.activeInterview = regarding;
        
        save();
    }
}
