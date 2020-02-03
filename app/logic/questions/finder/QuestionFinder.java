package logic.questions.finder;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import logic.questions.finder.WeightedTable.Winner;
import models.Category;
import models.Interview;
import models.Question;
import models.filter.question.QuestionFilter;
import models.query.question.InterviewBuilderQuery;
import play.jobs.Job;

/**
 * 
 * Used to find questions
 * 
 * @author joel
 *
 */
public class QuestionFinder extends Job<List<Question>>{
	final Interview interview;
	final List<List<Request>> requestGroups;
	final ListStopper<String,Question, List<Winner<String,Question>>> stopper;
	final boolean fair;
	final String dbgName;

	public QuestionFinder(final Interview interview,
			final List<List<Request>> requestGroups,
			final ListStopper<String,Question, List<Winner<String,Question>>> stopper,
			final boolean fair,
			final String dbgName){
		this.interview = interview;
		this.requestGroups = requestGroups;
		this.stopper = stopper;
		this.fair = fair;
		this.dbgName = dbgName;
	}

	public final List<Question> doJobWithResult(){
		return findQuestions(interview,requestGroups, stopper, fair, dbgName);
	}
	
	public static List<Question> findQuestions(
			final List<List<Request>> requestGroups,
			final ListStopper<String,Question, List<Winner<String,Question>>> stopper,
			final boolean fair,
			final String dbgName){
		
		List<Question> initialQuestions = new ArrayList<Question>();
		
		return findQuestions(
				initialQuestions,
				requestGroups,
				stopper,
				fair,
				dbgName);
	}
	
	public static List<Question> findQuestions(
			final Interview interview,
			final List<List<Request>> requestGroups,
			final ListStopper<String,Question, List<Winner<String,Question>>> stopper,
			final boolean fair,
			final String dbgName){
		
		List<Question> initialQuestions = null;
		if(interview != null){
			initialQuestions = interview.getQuestions();
		}
		
		return findQuestions(
				initialQuestions,
				requestGroups,
				stopper,
				fair,
				dbgName);
	}

	/**
	 * 
	 * @param initialQuestions These are the questions that the finder is guaranteed to include
	 * in the resulting list.
	 * 
	 * @param requestGroups A prioritized list of request groups. The first group in the list 
	 * has the highest priority and the last group has the lowest. A request group is a list of
	 * requests. All requests within a given group have the same priority.
	 * 
	 * 
	 * @param stopper Tells the finder when to stop producing questions for the interview.
	 * 
	 * @param fair If set to true you're telling the finder that, during the process of collecting questions,
	 * you want questions from categories that were not chosen this time to be more likely to become
	 * chosen as the next question.
	 * 
	 * @return
	 */
	public static List<Question> findQuestions(
			final List<Question> initialQuestions,
			final List<List<Request>> requestGroups,
			final ListStopper<String,Question, List<Winner<String,Question>>> stopper,
			final boolean fair,
			final String dbgName){
		
		
		
		String dumpFile = null;
		if (play.Play.mode == play.Play.Mode.DEV) {
			dumpFile = dbgName + ".xls";
		}
		
		List<WeightedTable.Winner<String,Question>> initialWinners = null;
		if(initialQuestions != null){
			initialWinners = new ArrayList<WeightedTable.Winner<String,Question>>();
			for(Question q : initialQuestions){
				String name = (q.category == null)?null:q.category.name;
				initialWinners.add(new WeightedTable.Winner<String,Question>(name,"Question_" + q.id,
					q,
					0));
			}
		}
		
		WeightedTable<String, Question> table = new WeightedTable<String, Question>(
				initialWinners,
				stopper,
				fair,
				dumpFile);
		
		int weight = requestGroups.size();
		for (List<Request> group : requestGroups){
			
			for (Request request : group){
				List<WeightedTable.Cell<Question>> column = new ArrayList<WeightedTable.Cell<Question>>();
				if((request != null) && (request.category != null)){
					
					Category c = request.category;
					
					if (c != null){//The category may not exist
						
						InterviewBuilderQuery query = new InterviewBuilderQuery(c,request.filters);
						Date start = new Date();
						List<Question> questions = query.executeQuery();
						Date end = new Date();
				        System.out.println(end.getTime() - start.getTime());

						for (Question q : questions){
								column.add(new WeightedTable.Cell<Question>("Question_" + q.id,
										q,
										weight));
						}
						table.addColumn(c.name, column, request.qualifier);
					}
				}
				
			}
			--weight;
		}
		return table.getWinners();
	}

	public static class Request{
		final Category category;
		
		
		/**
		 * filters are applied to weed out members
		 * of this category before they ever enter
		 * the contest
		 */
		final List<QuestionFilter> filters;
		
		
		/**
		 * The eliminator is applied to each column during the contest
		 * as the list of winners is growing. This gives each column
		 * a chance to drop out of the contest at any point.
		 */
		final ListQualifier<String,Question, List<Winner<String,Question>>> qualifier;

		public Request(final Category category,
				final List<QuestionFilter> filters,
				final ListQualifier<String,Question, List<Winner<String,Question>>> qualifier){
			super();
			this.category = category;
			this.filters = filters;
			this.qualifier = qualifier;
		}
	}
    
}
