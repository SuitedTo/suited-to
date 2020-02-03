package models.prep;

import java.lang.reflect.Type;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import modules.ebean.Finder;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import dto.prep.PrepJobCategoryDTO;
import enums.prep.Contribution;
import enums.prep.Difficulty;
import enums.prep.SkillLevel;

@Entity
@Table(name = "PREP_JobCategory")
public class PrepJobCategory extends EbeanModelBase {
    @Enumerated(EnumType.STRING)
    public SkillLevel prepJobLevel;
    @ManyToOne
    public PrepJob prepJob;
    @ManyToOne
    public PrepCategory prepCategory;
    public Boolean primaryCategory;
    @Enumerated(EnumType.STRING)
    public Difficulty difficulty;
    @Enumerated(EnumType.STRING)
    public Contribution weight;
    
    public static Finder<Long,PrepJobCategory> find = new Finder<Long,PrepJobCategory>(
            Long.class, PrepJobCategory.class
    );
    
    
    /**
     * Not really sure about how the jobCategory.difficulty long is supposed to be
     * interpreted. Might want to make it an enum?
     * 
     * @param difficulty
     * @return
     */
    public static Difficulty toDifficulty(long difficulty){
    	if(difficulty <= 1){
    		return Difficulty.EASY;
    	}
    	
    	if(difficulty >= 3){
    		return Difficulty.HARD;
    	}
    	
    	return Difficulty.MEDIUM;
    }
    
    /**
     * Not really sure about how the jobCategory.weight long is supposed to be
     * interpreted. Might want to make it an enum?
     * 
     * @param weight
     * @return
     */
    public static Contribution toContribution(long weight){
    	if(weight <= 1){
    		return Contribution.SMALL;
    	}
    	
    	if(weight >= 3){
    		return Contribution.LARGE;
    	}
    	
    	return Contribution.MEDIUM;
    }

    @Override
    public JsonSerializer<PrepJobCategory> serializer() {

        return new JsonSerializer<PrepJobCategory>() {
            public JsonElement serialize(PrepJobCategory pjc, Type type,
                                         JsonSerializationContext context) {

            	PrepJobCategoryDTO d = PrepJobCategoryDTO.fromPrepJobCategory(pjc);
                JsonObject result = (JsonObject) new JsonParser().parse(d.toJson());
                return result;
            }
        };
    }
}
