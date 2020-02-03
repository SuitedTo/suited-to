package models.prep.ewrap;

import static play.libs.F.Option.None;
import static play.libs.F.Option.Some;

import java.util.Iterator;
import java.util.Map;

import play.Logger;
import play.libs.F.Option;
import play.libs.F.Tuple;

/**
 * Represents a query search.
 * 
 * search=admin,tom:roles,firstName,email
 * 
 * @author joel
 * 
 */
public class Search implements Iterator<Search> {
	
    /**
     * Strings to search for
     */
    public String[] searchStrings;
    
    
    /**
     * Columns to search
     */
    public String[] columns;
    
    
    private Search next;
    

    public Option<Search> bind(String key, Map<String, String[]> data) {
    	if ("search".equalsIgnoreCase(key)) {
			try {
				return Some(parse(data.get(key)[0]));
			} catch (Exception e) {
				return None();
			}
		} else {
			return None();
		}
    }
    
    private static Search parse(String data) throws Exception{
    	Search head = new Search();
    	Search current = head;
    	String[] searchesStr = data.split("\\|");
    		for(String searchStr : searchesStr){
    			Tuple<String[], String[]> result = parseOne(searchStr);
				Search search = new Search();
				search.searchStrings = result._1;
				search.columns = result._2;
				current.next = search;
				current = search;
    		}
    	return head;
    }
    
    private static Tuple<String[], String[]> parseOne(String data) throws Exception {
    	String[] searchStrings = new String[0];
    	String[] columns = new String[0];
		String[] parts = data.split(":");
		if(parts.length > 0){
			searchStrings = parts[0].split(",");
		} else {
			Logger.error("Invalid params passed to search (" + data + ")");
			throw new Exception();
		}
		
		if(parts.length > 1){
			columns = parts[1].split(",");
		}
		return new Tuple<String[], String[]>(searchStrings, columns);
	}

    public String unbind(String key) {
       return "";
    }

    public String javascriptUnbind() {
        return "";
    }

    @Override
	public boolean hasNext() {
		return next != null;
	}

	@Override
	public Search next() {
		Search result = next;
		next = next.next;
		return result;
	}

	@Override
	public void remove() {
	}
}
