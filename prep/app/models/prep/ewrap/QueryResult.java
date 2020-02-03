package models.prep.ewrap;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;

import com.avaje.ebean.text.json.JsonValueAdapter;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

import play.libs.F.Option;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.text.json.JsonContext;
import com.avaje.ebean.text.json.JsonWriteOptions;
import common.utils.JsonUtil;

/**
 * Represents the result of a query
 * 
 * @author joel
 *
 * @param <T>
 */
public class QueryResult<T> {

    private static final SimpleDateFormat standardDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

	protected final List<T> resultList;

	public QueryResult(List<T> resultList) {
		super();
		this.resultList = resultList;
	}

	public List<T> getResultList() {
		return resultList;
	}
	
	private static Option noneIfNull(Option option){
		return (option == null)?play.libs.F.None:option;
	}
	
	protected JsonNode toNode(List<T> list, Option<String> maybePath){
		
		if(maybePath != null){
			for(String p : maybePath){
				JsonContext jsonContext = Ebean.createJsonContext();
				JsonWriteOptions o = new JsonWriteOptions().parsePath("(" + p + ")");

	            String listAsString = jsonContext.toJsonString(list, true, o);
				try {
					return new ObjectMapper().getJsonFactory()
							.createJsonParser(listAsString).readValueAsTree();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setDateFormat(standardDateFormat);
		return JsonUtil.toJson(objectMapper.valueToTree(list));
	}
	
	public JsonNode asJson(Option<String> maybePath){
		maybePath = noneIfNull(maybePath);
		return toNode(resultList, maybePath);
	}
}
