package models.query.question;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import controllers.Security;

import models.Question;
import models.QuestionMetadata;
import models.User;

public class RecentlyUsed extends AccessibleQuestions {

	
	public RecentlyUsed(){
		super();
	}

	public RecentlyUsed(User user, Integer iSortCol_0, String sSortDir_0, String sSearch,
            Integer iDisplayStart, Integer iDisplayLength) {
		super(user, iSortCol_0, sSortDir_0, sSearch, iDisplayStart, iDisplayLength);
	}

	@Override
	public Predicate buildCriteria(CriteriaBuilder criteriaBuilder,
			Root<Question> root, CriteriaQuery<Object> criteriaQuery) {
		
		Subquery<QuestionMetadata> questionMetadataSubquery = criteriaQuery.subquery(QuestionMetadata.class);
        Root fromQuestionMetadata = questionMetadataSubquery.from(QuestionMetadata.class);
        
        Predicate used = criteriaBuilder.and(criteriaBuilder.equal(fromQuestionMetadata.get("user"), user),
        		criteriaBuilder.isNotNull(fromQuestionMetadata.get("lastActivity")));
        
        Predicate usedRecently = criteriaBuilder.and(used,
        		criteriaBuilder.greaterThanOrEqualTo(fromQuestionMetadata.get("lastActivity"), thirtyDaysAgo()));
        
        questionMetadataSubquery.select(fromQuestionMetadata.get("question").get("id")).where(usedRecently);
        
        Predicate accessible = super.buildCriteria(criteriaBuilder, root, criteriaQuery);
        
		return criteriaBuilder.and(accessible,
				criteriaBuilder.in(root.get("id")).value(questionMetadataSubquery));
	}
	
	private static Date thirtyDaysAgo(){
		Calendar cal = new GregorianCalendar();
		cal.add(Calendar.DATE, -30);
		return cal.getTime();
	}

}
