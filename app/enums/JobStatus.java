package enums;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

public enum JobStatus {
	PENDING("To Be Screened"),
	QUALIFIED("Qualified"),
	INTERVIEWING("Interviewing"),
	DISQUALIFIED("Disqualified"),
	OFFERED("Offered"),
	HIRED("Hired");
	
	private String name;
	
	private JobStatus(String name){
		this.name = name;
	}
	
	public static List<String> names(){
		List<String> result = new ArrayList<String>();
		for(JobStatus status : JobStatus.values()){
			result.add(status.toString());
		}
        Collections.sort(result);

		return result;
	}
	
	public static JobStatus defaultStatus(){
		return PENDING;
	}
	
	public String toString(){
		return name;
	}
}
