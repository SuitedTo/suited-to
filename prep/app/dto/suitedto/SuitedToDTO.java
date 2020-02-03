package dto.suitedto;

import com.google.gson.Gson;

import dto.DTO;

public abstract class SuitedToDTO extends DTO {
	@Override
	public String toJson() {
		return new Gson().toJson(this);
	}
}
