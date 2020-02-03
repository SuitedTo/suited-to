package models.filter.question;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import play.db.jpa.JPA;

import models.Question;
import models.filter.Filter;

public class RecentlyUsed extends models.query.question.RecentlyUsed implements Filter<Question> {

	@Override
	public Predicate asPredicate(Root<Question> root) {
		CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
		return super.buildCriteria(cb, root, cb.createQuery());
	}

}
