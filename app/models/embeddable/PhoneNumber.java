package models.embeddable;

import com.mchange.v2.lang.ObjectUtils;
import enums.PhoneType;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class PhoneNumber implements Comparable<PhoneNumber>, Serializable {
    public PhoneType phoneNumberType;
    public String phoneNumberValue;

    public PhoneNumber() {
    }

    public PhoneNumber(PhoneType type, String value) {
        this.phoneNumberType = type;
        this.phoneNumberValue = value;
    }

    @Override
    public boolean equals(Object o) {
        boolean result = (o != null) && (o instanceof PhoneNumber);
        
        if (result) {
            PhoneNumber oAsPhoneNumber = (PhoneNumber) o;
            result = ObjectUtils.eqOrBothNull(phoneNumberType, 
                    oAsPhoneNumber.phoneNumberType) && 
                    ObjectUtils.eqOrBothNull(phoneNumberValue,
                    oAsPhoneNumber.phoneNumberValue);
        }
        
        return result;
    }

    @Override
    public int hashCode() {
        int result = phoneNumberType != null ? phoneNumberType.hashCode() : 0;
        result = 31 * result + (phoneNumberValue != null ? phoneNumberValue.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(PhoneNumber o) {
        int result = phoneNumberType.compareTo(o.phoneNumberType);
        
        if (result == 0) {
            result = phoneNumberValue.compareTo(o.phoneNumberValue);
        }
        
        return result;
    }
}
