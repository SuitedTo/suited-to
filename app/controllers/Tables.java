package controllers;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import models.filter.EntityAttributeFilter;
import models.filter.Filter;
import models.tables.AjaxTable;
import models.tables.CriteriaQueryInfoExposer.SearchOp;
import models.tables.CriteriaQueryInfoExposer.TableSearch;
import models.tables.CriteriaQueryInfoExposer.TableSearchAppendage;
import models.tables.Filterable;
import models.tables.Paginator;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.ejb.criteria.expression.function.AggregationFunction;
import org.hibernate.ejb.criteria.path.AbstractPathImpl;

import play.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class Tables extends ControllerBase {


    private static final Gson SAFE_SERIALIZER;

    static {
        GsonBuilder builder = new GsonBuilder();
        //Gson 2.2 and above will not let you register a TypeAdapter for String class. So we cannot automatically escape
        //html from strings.  Logic was added to AjaxTable to sanitize Strings but this static field was left in place
        //for performance and historical purposes in case we want to figure out a different way to sanitize string before
        //they are rendered to the page.
//        builder.registerTypeAdapter(String.class, new StringSanitizer());

        SAFE_SERIALIZER = builder.create();
    }

    public static final String TABLE_PREFIX = 
            AjaxTable.class.getName().substring(
                    0, AjaxTable.class.getName().lastIndexOf('.') + 1); 
    
    public static final String FILTER_PREFIX =
            EntityAttributeFilter.class.getName().substring(0, 
                    EntityAttributeFilter.class.getName().lastIndexOf('.') + 1);
    
    /**
     * <p>Generates a JSON response containing data pulled from a named
     * {@link models.tables.AjaxTable AjaxTable}, sorted and sliced as 
     * specified, and containing only entries that match the filters in the
     * given string and contain the specified search text.</p>
     * 
     * <p>The data will be formatted as an object containing a "data" field,
     * which will be an array of arrays, and a "matchingCount" field, which will
     * be an integer indicating how many total matching entries are available.
     * </p>
     * 
     * <p>If an error occurs, the returned object will instead define a field
     * named "error" that maps to an error message.</p>
     * 
     * @param reportName The class name of a table from which to derive data,
     *              rooted in models.tables.
     * @param sortColumn The index of the column on which to sort.  May be null
     *              if the order is undefined.
     * @param ascending True if sorting ascending, false otherwise.
     * @param startIndex The entry index of the first entry to return.
     * @param runLength The number of entries to return.
     * @param searchString The search string an entry must match.
     * @param filters Any filters that must apply.
     */
    public static void getReport(String reportName, Integer sortColumn, 
                Boolean ascending, Integer startIndex, Integer runLength,
                String searchString, String filters, String format) {
        
        Object result = null;
        Map<String, Object> error = new HashMap<String, Object>();

        try {
        	List<TableSearchAppendage> searches = new ArrayList<TableSearchAppendage>();
        	
        	if (searchString == null) {
        		searchString = "";
        	} else {

        		String[] searchStrings = searchString.split("(\\|{2}|\\&{2})");
        		int position = 0;
        		for(String search : searchStrings){
        			if(search.contains(":")){
        				TableSearch ts = new TableSearch(search.substring(0,search.indexOf(":")) ,
        						Arrays.asList(search.substring(search.indexOf(":") + 1).split(",")));
        				if(position == 0){
        					searches.add(new TableSearchAppendage(null, ts));
        				} else {
        					int endPrev = position - 2;
        					String opString = searchString.substring(endPrev,endPrev + 2);
        					SearchOp searchOp = SearchOp.fromString(opString);
        					searches.add(new TableSearchAppendage(searchOp, ts));
        				}
        			} else {
        				searches = null;
        			}
        			position += search.length() + 2;
        		}
        	}

            if (filters == null) {
                filters = "";
            }
            
            String packageRefinement;
            if (reportName.contains(".")) {
                packageRefinement = reportName.substring(
                        0, reportName.lastIndexOf('.') + 1);
            }
            else {
                packageRefinement = "";
            }
            
            List<Filter> decodedFilters = 
                    parseFilters(filters, packageRefinement);
            
            Class<AjaxTable> reportClass = (Class<AjaxTable>) Class.forName(
                    TABLE_PREFIX + reportName, false, Tables.class.getClassLoader());
            
            if (!AjaxTable.class.isAssignableFrom(reportClass)) {
                throw new RuntimeException("Attempted to load a class that " +
                        "was not a " + AjaxTable.class + ".");
            }
            
            Constructor<AjaxTable> construct = reportClass.getConstructor();
            AjaxTable report = construct.newInstance();
            
            if (!report.canAccess(Security.connectedUser())) {
                throw new RuntimeException("Forbidden.");
            }
            
            if (decodedFilters != null && !decodedFilters.isEmpty()) {
                if (!(report instanceof Filterable)) {
                    throw new RuntimeException("Query not filterable.");
                }
                
                List<Filter> nonNullFilters = new LinkedList<Filter>();
                for (Filter f : decodedFilters) {
                    if (f != null) {
                        nonNullFilters.add(f);
                    }
                }
                
                ((Filterable) report).addFilters(nonNullFilters);
            }
            
            Paginator p = (searches == null)?report.getPaginatableData(searchString):report.getPaginatableData(searches);
            
            Object sortColumnName = null;
            
            if (sortColumn != null) {
                sortColumnName = report.getColumnOrder()[sortColumn];
            }
            
            if (startIndex == null) {
                startIndex = 0;
            }
            
            if (runLength == null) {
                runLength = 10;
            }
            
            if (ascending == null){
            	ascending = false;
            }
            
            List<Map<Object, Object>> data = p.view(sortColumnName, ascending, startIndex, runLength);
            
            if(TableFormat.fromString(format).equals(TableFormat.DATA_TABLE)){//table json
            	Map<String, Object> payload = new HashMap<String, Object>();
            	List<List<Object>> tabularData = new LinkedList<List<Object>>();
            	for (Map<Object, Object> entry : data) {
            		tabularData.add(report.orderMap(entry));
            	}
            	
            	payload.put("data", tabularData);
                payload.put("matchingCount", p.getTotalEntryCount());
                result = payload;
            }else{//plain json
            	
            	// I think we probably want to refactor to have string keys by this point so
            	// this conversion is probably a temporary hack
            	List<Map<Object, Object>> converted = new ArrayList<Map<Object, Object>>();
            	for (Map<Object, Object> entry : data) {
            		Map<Object, Object> convertedEntry = new Hashtable<Object, Object>();
            		Set<Object> keys = entry.keySet();
            		for(Object key : keys){
            			Object value = entry.get(key);
            			if(value != null){
            				if(key instanceof AbstractPathImpl){

            					convertedEntry.put(((AbstractPathImpl)key).getAttribute().getName(),
            							value);

            				}else if(key instanceof AggregationFunction){
            					convertedEntry.put(((AggregationFunction)key).getFunctionName(),
            							entry.get(key));
            				} else{
            					convertedEntry.put(key,
            							entry.get(key));
            				}
            			}
            		}
            		converted.add(convertedEntry);
            	}

            	result = converted;
            }
        }
        catch (ClassNotFoundException cnfe) {
            Logger.warn("A client requested a non-existent report: \"" + 
                    reportName + "\"");
            error.put("error", "Could not execute report.");
        }
        catch (RuntimeException re) {
            Logger.warn(re, "Setting up a report, \"" + reportName + 
                    "\", went awry: " + re.getMessage());
            error.put("error", "Could not execute report.");
        }
        catch (ExceptionInInitializerError eiie) {
            Logger.warn(eiie, "Setting up a report, \"" + reportName + 
                    "\", went awry.");
            error.put("error", "Could not execute report.");
        }
        catch (InvocationTargetException ite) {
            Logger.warn(ite, "An exception occurred in the constructor of " +
                    "the report: \"" + reportName + "\".");
            error.put("error", "Could not execute report.");
        }
        catch (NoSuchMethodException nsme) {
            Logger.warn(nsme, "The requested report, \"" + reportName + 
                    "\"., does not have a no-arg constructor.");
            error.put("error", "Could not execute report.");
        }
        catch (InstantiationException ie) {
            Logger.warn(ie, "The requested report, \"" + reportName + 
                    "\"., could not be instantiated.");
            error.put("error", "Could not execute report.");
        }
        catch (IllegalAccessException iae) {
            Logger.warn(iae, "The requested report, \"" + reportName + 
                    "\"., could not be accessed.");
            error.put("error", "Could not execute report.");
        }

        if(error.isEmpty()){
        	renderJSON(SAFE_SERIALIZER.toJson(result));
        }else{
        	renderJSON(SAFE_SERIALIZER.toJson(error));
        }
    }
    
    private static List<Filter> parseFilters(String consolidatedFilterString, 
                String packageRefinement) {
        
        List<Filter> result = new LinkedList<Filter>();
        
        String[] filterStrings = consolidatedFilterString.split(",");
        
        for (String filterString : filterStrings) {
            if (!StringUtils.isBlank(filterString)) {
                try {
                    //todo: this looks like a close (but not exact) duplicate of FilterBinder.bind method, see if those
                    //2 methods can be refactored into a common method
                    Pattern p = Pattern.compile("(include|exclude)(\\w+)(:)([\\s\\.\\|\\$\\w]+)");
                    Matcher matcher = p.matcher(filterString);
                    if(matcher.matches()){
                        boolean include = matcher.group(1).equals("include");
                        String filterClassName = FILTER_PREFIX + 
                                packageRefinement + matcher.group(2);
                        EntityAttributeFilter filter = (EntityAttributeFilter)
                                Class.forName(filterClassName).newInstance();
                        if(include){
                            String[] includes = matcher.group(4).split("\\|");
                            for(String s : includes){
                                    filter.include(s);
                            }
                        }else{
                            String[] excludes = matcher.group(4).split("\\|");
                            for(String s : excludes){
                                    filter.exclude(s);
                            }
                        }
                        result.add(filter);
                    } else {
                        String filterClassName = FILTER_PREFIX + packageRefinement + 
                                filterString;

                        //try block is really just for readability/debugging
                        Filter filter = (Filter)Class.forName(
                                        filterClassName).newInstance();

                        result.add(filter);
                    }
                }
                catch (ClassNotFoundException cnfe) {
                    throw new RuntimeException("No such filter: " + packageRefinement + 
                            filterString);
                }
                catch (InstantiationException ie) {
                    throw new RuntimeException("Couldn't instantiate " + 
                            packageRefinement + filterString);
                }
                catch (IllegalAccessException iae) {
                    throw new RuntimeException("Couldn't instantiate " + 
                            packageRefinement + filterString);
                }
            }
        }
        
        return result;
    }
    
    public static class StringSanitizer implements JsonSerializer<String> {

        public JsonElement serialize(String t, Type type, 
                    JsonSerializationContext jsc) {
            
            return new JsonPrimitive(StringEscapeUtils.escapeHtml(t));
        }
        
    }
    
    public enum TableFormat {
    	DATA_TABLE, JSON;

    	public static TableFormat fromString(String string){
    		if(string != null){				
    			if(string.equalsIgnoreCase("json")){
    				return JSON;
    			}
    		}
    		return DATA_TABLE;
    	}
    };
}
