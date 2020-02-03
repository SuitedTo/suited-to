package models.embeddable;


import com.mchange.v2.lang.ObjectUtils;
import enums.ExternalLinkType;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class ExternalLink implements Comparable<ExternalLink>, Serializable {
    public ExternalLinkType externalLinkType;
    public String externalLinkValue;

    public ExternalLink() {
    }

    public ExternalLink(ExternalLinkType type, String value) {
        this.externalLinkType = type;
        this.externalLinkValue = value;
    }

    @Override
    public boolean equals(Object o) {
        boolean result = (o != null) && (o instanceof ExternalLink);
        
        if (result) {
            ExternalLink oAsExternalLink = (ExternalLink) o;
            
            result = ObjectUtils.eqOrBothNull(externalLinkValue, 
                    oAsExternalLink.externalLinkValue) && 
                    ObjectUtils.eqOrBothNull(externalLinkType, 
                    oAsExternalLink.externalLinkType);
        }
        
        return result;
    }

    @Override
    public int hashCode() {
        int result = externalLinkType != null ? externalLinkType.hashCode() : 0;
        result = 31 * result + (externalLinkValue != null ? externalLinkValue.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(ExternalLink o) {
        int result = externalLinkType.compareTo(o.externalLinkType);
        
        if (result == 0) {
            result = externalLinkValue.compareTo(o.externalLinkValue);
        }
        
        return result;
    }
}
