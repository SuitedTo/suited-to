package dto.prep;

import java.util.ArrayList;
import java.util.List;

import enums.prep.JobMatchType;

public class PrepJobMatchDTO extends PrepDTO{
	
	public String name;

	public String type;
	
	public String hint;
	
	public String value;
	
	public String data;
	
	public List<String> tokens = new ArrayList<String>();
}
