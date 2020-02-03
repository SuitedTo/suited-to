package models.embeddable;

import com.mchange.v2.lang.ObjectUtils;
import enums.EmailType;

import javax.persistence.Embeddable;
import java.io.Serializable;


@Embeddable
public class Email implements Comparable<Email>, Serializable {
    public EmailType emailType;

    @play.data.validation.Email
    public String emailAddress;

    public Email(EmailType emailType, String emailAddress) {
        this.emailType = emailType;
        this.emailAddress = emailAddress;
    }

    @Override
    public boolean equals(Object o) {
        boolean result = (o != null) && (o instanceof Email);
        
        if (result) {
            Email oAsEmail = (Email) o;
            
            result = (ObjectUtils.eqOrBothNull(emailType, oAsEmail.emailType) &&
                    ObjectUtils.eqOrBothNull(emailAddress, oAsEmail.emailAddress));
        }
        
        return result;
    }

    @Override
    public int hashCode() {
        int result = emailType != null ? emailType.hashCode() : 0;
        result = 31 * result + (emailAddress != null ? emailAddress.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(Email o) {
        int result = emailType.compareTo(o.emailType);
        
        if (result == 0) {
            result = emailAddress.compareTo(o.emailAddress);
        }
        
        return result;
    }
}
