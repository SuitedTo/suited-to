package models.query.company;

import models.Company;

import play.db.jpa.JPA;
import utils.FilteredCriteriaHelper;

import javax.persistence.criteria.CriteriaBuilder;

/**
 * Created with IntelliJ IDEA.
 * User: Mike Havens
 * Date: 7/27/12
 * Time: 2:14 PM
 * To change this template use File | Settings | File Templates.
 */
public class CompanyQueryHelper extends FilteredCriteriaHelper {

    public CompanyQueryHelper() {
        CriteriaBuilder builder = JPA.em().getCriteriaBuilder();

        TableKey companyKey = addSourceEntity(Company.class);
    }


}
