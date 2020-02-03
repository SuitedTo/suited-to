
package models.tables;

import java.util.*;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;

import org.drools.util.StringUtils;

import play.db.jpa.JPA;
import utils.EscapeUtils;
import utils.ObjectTransformer;

/**
 * <p>This class collects <code>Expression</code>s that are to be exposed as 
 * either columns or searchable fields from a not-yet-known 
 * <code>CriteriaQuery</code>, which allows it to be used alongside, e.g., a 
 * {@link utils.CriteriaHelper CriteriaHelper} as the query is being built up.
 * </p>
 */
public class CriteriaQueryInfoExposer {
    
    private final CriteriaBuilder myBuilder = JPA.em().getCriteriaBuilder();
    
    private List<Object> myColumnNames = new LinkedList<Object>();
    private List<Expression> myExpressionSources = new LinkedList<Expression>();
    private List<ObjectTransformer> myExpressionTransformers = 
            new LinkedList<ObjectTransformer>();
    
    private Map<String,Expression<String>> myDynamicSearchTargets = 
            new HashMap<String,Expression<String>>();
    private List<Expression<String>> myGlobalSearchTargets = 
            new LinkedList<Expression<String>>();
    private List<ExactValueSearch> myGlobalExactMatchSearchTargets = 
            new LinkedList<ExactValueSearch>();
    
    public void addColumn(Expression value) {
        addColumn(value, ObjectTransformer.DummyTransformer.INSTANCE);
    }
    
    public void addColumn(Expression value, ObjectTransformer transform) {
        myColumnNames.add(new Object());
        myExpressionSources.add(value);
        myExpressionTransformers.add(transform);
    }
    
    public void includeInDynamicSearch(String name, Expression<String> searchable) {
        myDynamicSearchTargets.put(name, searchable);
    }
    
    public void includeInSearch(Expression<String> searchable) {
        myGlobalSearchTargets.add(searchable);
    }
    
    public <T> void includeAsExactMatchSearch(Expression<T> searchable, 
                Parser<T> parser) {
        myGlobalExactMatchSearchTargets.add(
                new ExactValueSearch(searchable, parser));
    }

    public Object[] getColumnOrder() {
        return myColumnNames.toArray();
    }
    
    public Expression<Boolean> getWhereClause(
            Expression<Boolean> baseRestriction, String searchString) {
    	
    	searchString = 
                EscapeUtils.safeSQLLikeString(searchString).toLowerCase();
        
        if (baseRestriction == null) {
            baseRestriction = myBuilder.conjunction();
        }
        
        if(StringUtils.isEmpty(searchString)){
        	return baseRestriction;
        }
        
        Expression<Boolean> searchRestriction;
        
        if (myGlobalSearchTargets.isEmpty() && 
                myGlobalExactMatchSearchTargets.isEmpty() &&
                myDynamicSearchTargets.isEmpty()) {
            
            searchRestriction = myBuilder.conjunction();
        }
        else {
            searchRestriction = myBuilder.disjunction();
            
            for (Expression<String> target : myGlobalSearchTargets) {
                searchRestriction = myBuilder.or(searchRestriction, 
                        myBuilder.like(myBuilder.lower(target), 
                            "%" + searchString + "%"));
            }
            
            for (ExactValueSearch search : myGlobalExactMatchSearchTargets) {
                try {
                    Object value = search.parser.parse(searchString);
                    searchRestriction = myBuilder.or(searchRestriction, 
                            myBuilder.equal(search.source, value));
                }
                catch (FormatException fe) {
                    
                }
            }
        }
        
        return myBuilder.and(baseRestriction, searchRestriction);
    }
    
    public Expression<Boolean> getWhereClause(
            Expression<Boolean> baseRestriction, Expression<Boolean> additionalRestrictions) {
    	
        
        if (baseRestriction == null) {
            baseRestriction = myBuilder.conjunction();
        }
        
        if (additionalRestrictions == null) {
        	additionalRestrictions = myBuilder.conjunction();
        }
        
        return myBuilder.and(baseRestriction, additionalRestrictions);
    }
    
    public Map<Object, ColumnSynthesisEntry> getSelectedColumns() {
        Map<Object, ColumnSynthesisEntry> columns = 
                new HashMap<Object, ColumnSynthesisEntry>();
        
        Iterator<Object> names = myColumnNames.iterator();
        Iterator<Expression> expressions = myExpressionSources.iterator();
        Iterator<ObjectTransformer> transformers = 
                myExpressionTransformers.iterator();
        while (expressions.hasNext()) {
            columns.put(names.next(), new ColumnSynthesisEntry(
                    expressions.next(), transformers.next()));
        }
        
        return columns;
    }
    
    public static interface Parser<T> {
        public T parse(String s) throws FormatException;
    }
    
    public static abstract class SearchOp{
    	protected CriteriaBuilder builder;
    	public SearchOp(){
    		this.builder = JPA.em().getCriteriaBuilder();
    	}
    	public abstract Expression<Boolean> evaluate(Expression<Boolean> a, Expression<Boolean> b);
    	
    	public static SearchOp fromString(String opString){
    		if(opString.equals("&&")){
				return new SearchAnd();
    		}
    		return new SearchOr();
    	}
    }
    
    public static class SearchAnd extends SearchOp{
		@Override
		public Expression<Boolean> evaluate(Expression<Boolean> a,
				Expression<Boolean> b) {
			if(a == null){
				a = builder.conjunction();
			}
			if(b == null){
				b = builder.conjunction();
			}
			return builder.and(a, b);
		}
    }
    
    public static class SearchOr extends SearchOp{
		@Override
		public Expression<Boolean> evaluate(Expression<Boolean> a,
				Expression<Boolean> b) {
			if(a == null){
				a = builder.disjunction();
			}
			if(b == null){
				b = builder.disjunction();
			}
			return builder.or(a, b);
		}
    }
    
    public static class TableSearch{
    	private String searchString;
    	private List<String> searchColumns;
		public TableSearch(String searchString,
				List<String> searchColumns) {
			this.searchString = searchString;
			this.searchColumns = searchColumns;
		}
    }
    
    public static class TableSearchAppendage{
    	private SearchOp searchOp;
    	private TableSearch tableSearch;
		public TableSearchAppendage(SearchOp searchOp, TableSearch tableSearch) {
			this.searchOp = searchOp;
			this.tableSearch = tableSearch;
		}
    }
    
    public Expression<Boolean> append(TableSearchAppendage tsa, Expression<Boolean> accumulator){
		Expression<Boolean> predicate = myBuilder.disjunction();
		for(String column : tsa.tableSearch.searchColumns){
			Expression<String> col = myDynamicSearchTargets.get(column).getJavaType().equals(String.class)?
					myBuilder.lower(myDynamicSearchTargets.get(column)):
						myDynamicSearchTargets.get(column);
			predicate = myBuilder.or(predicate,myBuilder.like(col.as(String.class), "%" + tsa.tableSearch.searchString + "%"));
		}
		if(tsa.searchOp != null){
			predicate = tsa.searchOp.evaluate(accumulator,predicate);
		}
		return predicate;
	}
    
    public class ExactValueSearch {
        public final Expression source;
        public final Parser parser;
        
        public ExactValueSearch(Expression source, Parser parser) {
            this.source = source;
            this.parser = parser;
        }
    }
    
    public static class ColumnSynthesisEntry {
        public final Expression source;
        public final ObjectTransformer transformer;
        
        public ColumnSynthesisEntry(Expression source, 
                ObjectTransformer transformer) {
            
            this.source = source;
            this.transformer = transformer;
        }
    }
}
