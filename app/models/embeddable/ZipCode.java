package models.embeddable;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class ZipCode implements Serializable {
    public String zip;
    public String plusFour;

    public ZipCode() {
        super();
    }

    public ZipCode(String zip) {
        this();
        this.zip = zip;
    }

    public ZipCode(String zip, String plusFour) {
        this(zip);
        this.plusFour = plusFour;
    }


    @Override
    public String toString() {
        String suffix = (plusFour == null) ? "" : "-" + plusFour;
        return zip + suffix;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((plusFour == null) ? 0 : plusFour.hashCode());
        result = prime * result + ((zip == null) ? 0 : zip.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ZipCode other = (ZipCode) obj;
        if (plusFour == null) {
            if (other.plusFour != null)
                return false;
        } else if (!plusFour.equals(other.plusFour))
            return false;
        if (zip == null) {
            if (other.zip != null)
                return false;
        } else if (!zip.equals(other.zip))
            return false;
        return true;
    }
}
