package models.query.question;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import models.Category;
import models.Question;
import models.User;
import models.filter.question.QuestionFilter;
import play.db.jpa.JPA;

import com.googlecode.hibernate.memcached.utils.StringUtils;

import controllers.Security;
import enums.RoleValue;

public class InterviewBuilderQuery {
	private final static int MAX_RESULTS = 100;
	private User user;
	private Category category;
	private List<QuestionFilter> filters;
	private Root<Question> root;
	
	public InterviewBuilderQuery(Category category, List<QuestionFilter> filters) {
		this.user = Security.connectedUser();
		this.filters = filters;
		
		this.category = category;
		
		CriteriaBuilder criteriaBuilder = JPA.em().getCriteriaBuilder();

        CriteriaQuery<Object> entityQuery = criteriaBuilder.createQuery();

        this.root = entityQuery.from(Question.class);
	}
	
	private String getMetadataJoin(){
		StringBuilder userMatch = new StringBuilder(" left outer join question_metadata md on q.id=md.question_id and (");
		if(user != null){
			userMatch.append("md.user_id=").append(user.id).append(" or ");
		}
		return  userMatch.append("md.id is null) ").toString();
	}
	
	private String accessible(){
		StringBuilder sb = new StringBuilder("(q.status='ACCEPTED'");
		if((user != null) && user.company != null){
			sb.append(" or u.company_id=").append(user.company.id);
		}
		sb.append(")");
		return sb.toString();
	}

	public List<Question> executeQuery(){
		StringBuilder query = new StringBuilder();
		query.append("select* from Question q left outer join QUESTION_CATEGORY qc on q.id=qc.Question_id ")
		.append(getMetadataJoin());


		if(user != null){
			query.append(" cross join app_user u ")
			.append(" where qc.categories_id=")
			.append(category.id)
			.append(" and q.user_id=u.id and ").append(accessible());
		} else {
			query.append(" where qc.categories_id=")
			.append(category.id)
			.append(" and ")
			.append(accessible());
		}
		query.append(" and q.active=1 and q.status<>'QUICK'");



		if((filters != null) && !filters.isEmpty()){
        	for (QuestionFilter filter : filters) {
        		query.append(" and " + filter.asNativeMySQL(root,"q"));
        	}
        }
		query.append(" order by ")
				.append("(CASE WHEN md.ratingPoints IS NULL THEN q.interviewPoints ELSE md.ratingPoints + q.interviewPoints END) desc,")
				.append("(CASE WHEN q.status='PRIVATE' THEN 1 ELSE 0 END) desc,")
				.append("(CASE WHEN q.time='LONG' THEN 1 ELSE 0 END) desc,")
				.append("(CASE WHEN q.time is null or q.time='MEDIUM' THEN 1 ELSE 0 END) desc,")
				.append("(CASE WHEN q.time='SHORT' THEN 1 ELSE 0 END) desc ")
				.append("limit ").append(MAX_RESULTS).append(";");
		
		List<Question> results = JPA.em().createNativeQuery(query.toString(), Question.class).getResultList();
		
		return results;
	}
}
