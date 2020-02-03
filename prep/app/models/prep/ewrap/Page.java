package models.prep.ewrap;

import static play.libs.F.Option.None;
import static play.libs.F.Option.Some;

import java.util.Map;

import play.libs.F.Option;
import play.libs.F.Tuple;

/**
 * Represents one page of a query.
 * 
 * page=3,25
 * 
 * @author joel
 * 
 */
public class Page {

	/**
	 * Zero based page number
	 */
	public int index;

	/**
	 * The number of records per page
	 */
	public int size;

	public Option<Page> bind(String key, Map<String, String[]> data) {
		if ("page".equalsIgnoreCase(key)) {
			try {
				Tuple<Integer, Integer> result = parse(data.get(key)[0]);
				index = result._1;
				size = result._2;
				return Some(this);
			} catch (Exception e) {
				return None();
			}
		} else {
			return None();
		}
	}

	private Tuple<Integer, Integer> parse(String data) throws Exception {
		String[] parts = data.split(",");
		return new Tuple<Integer, Integer>(Integer.parseInt(parts[0]),
				Integer.parseInt(parts[1]));
	}

	public String unbind(String key) {
		return "";
	}

	public String javascriptUnbind() {
		return "";
	}
}
