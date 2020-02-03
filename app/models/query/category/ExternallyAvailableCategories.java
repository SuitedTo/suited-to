package models.query.category;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import models.Category;
import models.Category.CategoryStatus;

public class ExternallyAvailableCategories extends AccessibleCategories{

	@Override
    public Predicate buildCriteria(CriteriaBuilder criteriaBuilder, Root<Category> root,
                                   CriteriaQuery<Object> criteriaQuery) {
		Predicate accessible = super.buildCriteria(criteriaBuilder, root, criteriaQuery);
		
		final Path<Boolean> isAvailableExternally = root.get("isAvailableExternally");
		
		return criteriaBuilder.and(accessible, criteriaBuilder.equal(isAvailableExternally, true));
	}
}
