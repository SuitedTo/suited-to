package models.query.question;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import controllers.Security;

import models.Company;
import models.Question;
import models.QuestionMetadata;
import models.User;

import play.db.jpa.JPA;

/**
 * Misc queries related to question ratings
 *  
 * @author joel
 *
 */
public final class QuestionRatings {
	
	private QuestionRatings(){}
	
	private static CriteriaBuilder criteriaBuilder = JPA.em().getCriteriaBuilder();
	
	/**
	 * Runs a sum query to get the user rating for the given user. A sum query will
	 * be faster than making hibernate load the QuestionMetadata object.
	 * 
	 * @param user
	 * @param question
	 * @return
	 */
	public static int userRating(User user, Question question){
		
		if(user == null){
        	return 0;
        }

        CriteriaQuery<Object> entityQuery = criteriaBuilder.createQuery();

        Root<QuestionMetadata> qmRoot = entityQuery.from(QuestionMetadata.class);
        

        Predicate questionMatch = criteriaBuilder.equal(qmRoot.get("question"),question);
        Predicate givenUser = criteriaBuilder.equal(qmRoot.get("user"),user);     	
        Predicate criteria = criteriaBuilder.and(givenUser,questionMatch);

        
        Expression<Integer> totalRating = criteriaBuilder.sum(qmRoot.get("rating").as(Integer.class));
        entityQuery.select(totalRating).where(criteria);
        
        //This query is not cacheable at this time. If the interview builder is too slow then
        //we can consider caching it.
        Object result = JPA.em().createQuery(entityQuery).getSingleResult();
        if(result != null){
        	return (Integer)result;
        }
        return 0;
	}
	
	public static int coworkerRating(Question question){
		return coworkerRating(Security.connectedUser(), question);
	}
	
	/**
	 * Runs one query to get the total rating by all users who are associated with the
	 * same company as the given user. If the given user is not associated with a company
	 * then the result will be 0. The given user's rating is not included in the
	 * calculation.
	 * 
	 * @param user
	 * @param question
	 * @return
	 */
	public static int coworkerRating(User user, Question question){
		
		if(user.company == null){
        	return 0;
        }

        CriteriaQuery<Object> entityQuery = criteriaBuilder.createQuery();

        Root<QuestionMetadata> qmRoot = entityQuery.from(QuestionMetadata.class);
        
        Subquery<User> subquery = entityQuery.subquery(User.class);
        Root fromUser = subquery.from(User.class);
        
        

        Predicate questionMatch = criteriaBuilder.equal(qmRoot.get("question"),question);
        Predicate notGivenUser = criteriaBuilder.notEqual(qmRoot.get("user"),user);

        Predicate hasCompany = criteriaBuilder.isNotNull(qmRoot.get("user").get("company"));
        subquery.select(fromUser).where(criteriaBuilder.and(hasCompany,criteriaBuilder.equal(fromUser.get("company"), user.company)));        	
        Predicate criteria = criteriaBuilder.and(notGivenUser,criteriaBuilder.and(questionMatch, criteriaBuilder.in(qmRoot.get("user")).value(subquery)));

        
        Expression<Integer> totalRating = criteriaBuilder.sum(qmRoot.get("rating").as(Integer.class));
        entityQuery.select(totalRating).where(criteria);
        
        //This query is not cacheable at this time. If the interview builder is too slow then
        //we can consider caching it.
        Object result = JPA.em().createQuery(entityQuery).getSingleResult();
        if(result != null){
        	return (Integer)result;
        }
        return 0;
	}
	
	public static int nonCoworkerRating(Question question){
		return nonCoworkerRating(Security.connectedUser(), question);
	}
	
	/**
	 * Runs one query to get the total rating by all users who are not associated with the
	 * same company as the given user. If the given user is not associated with a company
	 * then all other users are considered non-coworkers.
	 * 
	 * @param user
	 * @param question
	 * @return
	 */
	public static int nonCoworkerRating(User user, Question question){

        CriteriaQuery<Object> entityQuery = criteriaBuilder.createQuery();

        Root<QuestionMetadata> qmRoot = entityQuery.from(QuestionMetadata.class);
        
        Subquery<User> subquery = entityQuery.subquery(User.class);
        Root fromUser = subquery.from(User.class);
        
        

        Predicate questionMatch = criteriaBuilder.equal(qmRoot.get("question"),question);
        Predicate criteria;
        
        if(user.company == null){
        	Predicate notUser = criteriaBuilder.notEqual(qmRoot.get("user"), user);
        	criteria = criteriaBuilder.and(questionMatch, notUser);
        }else{
        	Predicate hasNoCompany = criteriaBuilder.isNull(qmRoot.get("user").get("company"));
        	subquery.select(fromUser).where(criteriaBuilder.or(hasNoCompany,criteriaBuilder.notEqual(fromUser.get("company"), user.company)));        	
        	criteria = criteriaBuilder.and(questionMatch, criteriaBuilder.in(qmRoot.get("user")).value(subquery));
        }
        
        Expression<Integer> totalRating = criteriaBuilder.sum(qmRoot.get("rating").as(Integer.class));
        entityQuery.select(totalRating).where(criteria);
        
        //This query is not cacheable at this time. If the interview builder is too slow then
        //we can consider caching it.
        Object result = JPA.em().createQuery(entityQuery).getSingleResult();
        if(result != null){
        	return (Integer)result;
        }
        return 0;
	}
	
	
	
	
	
	
	
}