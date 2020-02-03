package dto.prep;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import dto.DTO;

public abstract class PrepDTO extends DTO{

	@Override
	public String toJson() {
		return new Gson().toJson(this);
	}
	
	public JsonElement toJsonTree() {
		return new Gson().toJsonTree(this);
	}
}
