package models.prep;

import java.lang.reflect.Type;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import modules.ebean.Finder;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import dto.prep.PrepInterviewCategoryListDTO;

@Entity
@Table(name = "PREP_InterviewCategoryList")
public class PrepInterviewCategoryList extends EbeanModelBase{
	
	public String name;
	
	public static Finder<Long,PrepInterviewCategoryList> find = new Finder<Long,PrepInterviewCategoryList>(
            Long.class, PrepInterviewCategoryList.class
    );

	@ManyToMany(cascade={CascadeType.PERSIST})
    @JoinTable(name = "PREP_InterviewCategory_PREP_InterviewCategoryList")
	public List<PrepInterviewCategory> categories;// = new ArrayList<PrepInterviewCategory>();
	
	@Override
	public JsonSerializer<PrepInterviewCategoryList> serializer(){
		
		return new JsonSerializer<PrepInterviewCategoryList>(){
			public JsonElement serialize(PrepInterviewCategoryList pi, Type type,
					JsonSerializationContext context) {

				PrepInterviewCategoryListDTO d = PrepInterviewCategoryListDTO.fromPrepInterviewCategoryList(pi);
				return d.toJsonTree();

			}
		};
	}
}
