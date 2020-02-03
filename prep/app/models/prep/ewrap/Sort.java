package models.prep.ewrap;

import static play.libs.F.Option.None;
import static play.libs.F.Option.Some;

import java.util.Iterator;
import java.util.Map;

import play.Logger;
import play.libs.F.Option;

/**
 * 
 * Sort column and direction
 * 
 * sort=author,desc|title,asc,
 * 
 * @author joel
 *
 */
public class Sort implements Iterator<String> {
	
	public String orderBy;
	private Sort next = null;

    public Option<Sort> bind(String key, Map<String, String[]> data) {
        if ("sort".equalsIgnoreCase(key)) {
            try {
            	return Some(parse(data.get(key)[0]));
            } catch (Exception e) {
                return None();
            }
        } else {
            return None();
        }
    }
    
    private static Sort parse(String data){
    	Sort head = new Sort();
    	Sort current = head;
    	String[] sortsStr = data.split("\\|");
    		for(String sortStr : sortsStr){
    			String[] sortParts = sortStr.split(",");
				if(sortParts.length != 2){
					Logger.error("Invalid params passed to sort (" + sortStr + ")");
					continue;
				}
				Sort sort = new Sort();
				sort.orderBy = sortParts[0] + " " + sortParts[1];
				current.next = sort;
				current = sort;
    		}
    	return head;
    }

    public String unbind(String key) {
    	//TODO
       return "";
    }

    public String javascriptUnbind() {
    	//TODO
        return "function(k,v) {\n" +
            "    return encodeURIComponent(k+'.index')+'='+v.index+'&amp;'+encodeURIComponent(k+'.size')+'='+v.size;\n" +
            "}";
    }

	@Override
	public boolean hasNext() {
		return next != null;
	}

	@Override
	public String next() {
		String result = next.orderBy;
		next = next.next;
		return result;
	}

	@Override
	public void remove() {
		
	}
}
