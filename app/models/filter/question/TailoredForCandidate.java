package models.filter.question;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import models.Candidate;
import models.Question;
import play.db.jpa.JPA;

public class TailoredForCandidate extends QuestionFilter<Long> {

    public Predicate asPredicate(Root<Question> root) {

        CriteriaBuilder criteriaBuilder = JPA.em().getCriteriaBuilder();
        Predicate criteria = (include.size() == 0) ? criteriaBuilder.disjunction() : criteriaBuilder.conjunction();


        for (Long id : include) {
            Candidate candidate = Candidate.findById(id);
            if (candidate != null) {
            	List<Question> questions = candidate.getQuestionsToAvoid();
            	for(Question question : questions){
                criteria = criteriaBuilder.and(criteria,
                        	criteriaBuilder.not(
                                criteriaBuilder.equal(root, question)));
            	}
            }
        }

        for (Long id : exclude) {
            Candidate candidate = Candidate.findById(id);
            if (candidate != null) {
            	List<Question> questions = candidate.getQuestionsToAvoid();
            	for(Question question : questions){
            		criteria = criteriaBuilder.or(criteria, criteriaBuilder.equal(root, question));
            	}
            	
            }
        }

        return criteria;
    }
    
    @Override
    public String getAttributeName() {
        return "";
    }

    @Override
    protected String toString(Long id) {
        return String.valueOf(id);
    }

    @Override
    public Long fromString(String idStr) {
        return Long.parseLong(idStr);
    }
}
