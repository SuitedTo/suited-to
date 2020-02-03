package models.tables;

import java.util.Date;

import models.Candidate;
import models.User;
import models.tables.CriteriaHelperTable.DateToFormattedDateString;
import utils.ObjectTransformer;

public class DefaultTableObjectTransformer implements ObjectTransformer {

	public static final ObjectTransformer INSTANCE = new DefaultTableObjectTransformer();
	private static final DateToFormattedDateString dateToStr = new DateToFormattedDateString();

	public Object transform(Object input) {
		if(input == null){
			return "";
		}
		if(input instanceof Date){
			return dateToStr.transform(input);
		}
		
		if(input instanceof User){
			return ((User)input).email;
		}
		
		if(input instanceof Candidate){
			return ((Candidate)input).name;
		}
		
		return String.valueOf(input);
	}

}
