package models;

import org.apache.commons.lang.StringUtils;

import javax.persistence.Entity;
import javax.persistence.Query;

@Entity
public class GateKeeper extends ModelBase {
    public String restriction;


    public static boolean hasAccess(String key) {
        String alternateKey = null;
        if (key.contains(".")) {
            alternateKey = StringUtils.split(key, ".")[0];
        }
        Query query = em().createQuery("select count(*) from GateKeeper g where Restriction in (:key,:alt)");
        query.setParameter("key", key);
        query.setParameter("alt", alternateKey);
        query.setHint("org.hibernate.cacheable", true);
        long count = (Long)query.getSingleResult();
        return count == 0;
    }

    @Override
    public String toString() {
        return restriction;
    }
}
