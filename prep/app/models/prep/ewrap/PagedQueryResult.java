package models.prep.ewrap;

import java.util.List;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;

import common.utils.JsonUtil;

import play.libs.F.Option;

public class PagedQueryResult<T> extends QueryResult<T>{
	
	protected final Integer total;

	public PagedQueryResult(List<T> resultList, Integer total) {
		super(resultList);
		this.total = total;
	}
	
	@Override
	public JsonNode asJson(Option<String> maybePath){
		ObjectNode result = JsonUtil.newObject();
		JsonNode rows = toNode(resultList, maybePath);
		result.put("rows", rows);
		result.put("total", total);
		return result;
	}

}
