
package models.filter.question;

import exceptions.CacheMiss;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import models.Interview;
import models.Question;
import models.cache.InterviewCache;
import models.filter.Filter;
import play.db.jpa.JPA;

public class NotInCachedInterview implements Filter<Question> {

    public Predicate asPredicate(Root<Question> root) {

        CriteriaBuilder builder = JPA.em().getCriteriaBuilder();
        Predicate result = builder.conjunction();
        
        try {
            Interview inProgress = InterviewCache.getInProgressInterview();
            
            for(Question q : inProgress.getQuestions()){
                
                result = builder.and(result, 
                        builder.not(builder.equal(root.get("id"), q.id)));
            }
        } catch (CacheMiss e) { }
        
        return result;
    }
    
    public Class<Question> getEntityClass() {
        return Question.class;
    }
}
