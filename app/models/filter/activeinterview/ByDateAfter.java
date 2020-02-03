package models.filter.activeinterview;

import models.ActiveInterview;
import org.apache.commons.lang.time.DateUtils;
import play.Logger;
import play.db.jpa.JPA;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Includes ActiveInterviews that are after any date specified in the includes and before any date specified in the
 * excludes.  ActiveInterviews that do not have a date specified will not be included in the results.
 * Date arguments must be in the format MMddyyyy
 */
public class ByDateAfter extends ActiveInterviewFilter<Date> {

    public Predicate asPredicate(Root<ActiveInterview> root) {

        CriteriaBuilder criteriaBuilder = JPA.em().getCriteriaBuilder();
        final Path<Date> anticipatedDate = root.<Date>get("anticipatedDate");

        Predicate criteria = include.isEmpty()?criteriaBuilder.disjunction():criteriaBuilder.conjunction();

        for (Date includedDate : include) {
            criteria = criteriaBuilder.and(criteria, criteriaBuilder.greaterThan(anticipatedDate, includedDate));
        }

        for (Date excludedDate : exclude) {
            criteria = criteriaBuilder.or(criteria, criteriaBuilder.not(criteriaBuilder.greaterThan(anticipatedDate, excludedDate)));
        }
        
        criteria  = criteriaBuilder.or(criteria, criteriaBuilder.isNull(anticipatedDate));

        return criteria;
    }

    public boolean willAccept(ActiveInterview activeInterview) {
        if ((activeInterview != null) && (activeInterview.anticipatedDate != null)) {
            for (Date anticaptedDateMustBeAfter : include) {
                if(!activeInterview.anticipatedDate.after(anticaptedDateMustBeAfter)){
                    return false;
                }
            }

            for (Date anticipatedDateMustNotBeAfter : exclude) {
                if(activeInterview.anticipatedDate.after(anticipatedDateMustNotBeAfter)){
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public String getAttributeName() {
        return "created";
    }

    @Override
    protected String toString(Date date) {
        return new SimpleDateFormat("MMddyyyy").format(date);
    }

    @Override
    public Date fromString(String dateString) {
        try {
            return DateUtils.parseDate(dateString, new String[]{"MMddyyyy"});
        } catch (ParseException e) {
            Logger.error("Could Not Parse Date value: " + dateString, e);
            throw new IllegalArgumentException("Illegal date format: " + dateString, e);
        }
    }
}
