package models.tables.activeinterview;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import models.ActiveInterview;
import models.Candidate;
import models.Question;
import models.User;
import models.filter.activeinterview.ById;
import models.filter.activeinterview.ByInterviewee;
import models.query.activeinterview.AccessibleActiveInterviewsHelper;
import models.tables.DefaultTableObjectTransformer;
import models.tables.FilteredCriteriaHelperTable;
import play.db.jpa.JPA;
import utils.CriteriaHelper.RootTableKey;
import utils.CriteriaHelper.TableKey;
import utils.ObjectTransformer;
import enums.Timing;

public class ActiveInterviewTable  extends FilteredCriteriaHelperTable {

    public ActiveInterviewTable(){
    	super(new AccessibleActiveInterviewsHelper());
    	CriteriaBuilder builder = JPA.em().getCriteriaBuilder();
    	RootTableKey interview = (RootTableKey) getSoleEntityKey(ActiveInterview.class);
    	
    	TableKey interviewQuestions = join(interview, "interviewQuestions");
    	addGroup(field(interviewQuestions, "interview"));
    	
    	TableKey question = join(interviewQuestions, "question");
    	
    	addColumn(interview, "id");
    	addColumn(interview, "name");
    	addColumn(interview, "interviewee", DefaultTableObjectTransformer.INSTANCE);
    	addColumn(interview, "anticipatedDate",DefaultTableObjectTransformer.INSTANCE);
    	addColumn(builder.count(field(interviewQuestions, "id")));
    	
    	Subquery longSubquery = createSubquery();
    	Root longRoot = longSubquery.from(Question.class);
    	
    	Subquery mediumSubquery = createSubquery();
    	Root mediumRoot = mediumSubquery.from(Question.class);
    	
    	Subquery shortSubquery = createSubquery();
    	Root shortRoot = shortSubquery.from(Question.class);
    	Expression duration =  builder.sum(
				 builder.sum(
				 builder.sum(
					longSubquery.select(builder.prod(Timing.LONG.getDuration(), builder.count(longRoot)))
						.where(builder.and(builder.equal(field(question,"time", Timing.class), Timing.LONG),builder.in(longRoot).value(field(interviewQuestions, "question")))),
					mediumSubquery.select(builder.prod(Timing.MEDIUM.getDuration(), builder.count(mediumRoot)))
						.where(builder.and(builder.equal(field(question,"time", Timing.class), Timing.MEDIUM),builder.in(mediumRoot).value(field(interviewQuestions, "question"))))
						),
					shortSubquery.select(builder.prod(Timing.SHORT.getDuration(), builder.count(shortRoot)))
						.where(builder.and(builder.equal(field(question,"time", Timing.class), Timing.SHORT),builder.in(shortRoot).value(field(interviewQuestions, "question"))))
						 )
		);
		 
		 
    	addColumn(duration, new ObjectTransformer(){

			@Override
			public Object transform(Object input) {
				return String.valueOf(input) + " min";
			}
    		
    	});
    	addColumn(interview, "categories");
    	addColumn(interview, "interviewer", DefaultTableObjectTransformer.INSTANCE);
    	addColumn(interview, "active",
    			new ObjectTransformer(){

					@Override
					public Object transform(Object input) {
						return ((Boolean)input)?"Active":"Inactive";
					}
    		
    	});
    	
    	includeAsExactMatchSearch(interview, new ById());
    	includeInSearch(field(interview, "name", String.class));
    }

	@Override
	public boolean canAccess(User u) {
		return u != null;
	}

}
