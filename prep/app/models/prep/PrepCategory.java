package models.prep;

import java.lang.reflect.Type;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import dto.suitedto.SuitedToCategoryDTO;
import modules.ebean.Finder;
import play.cache.Cache;
import play.jobs.Job;
import play.jobs.On;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import controllers.prep.delegates.CategoriesDelegate;
import dto.prep.PrepCategoryDTO;
import exceptions.prep.MissingTwinException;


@Entity
@Table(name = "PREP_Category")
public class PrepCategory extends EbeanModelBase {
	

	@Column(name="category_id")
    public String categoryId;
    
	@Column(name="is_prep_searchable")
    public Boolean isPrepSearchable = false;
    
    public String name;
    
    @Column(name="company_name")
    public String companyName;
    
    @OneToMany(mappedBy = "prepCategory", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<PrepJobCategory> prepJobCategories;
    
    public static Finder<Long,PrepCategory> find = new Finder<Long,PrepCategory>(
            Long.class, PrepCategory.class
    );

    @Override
    public JsonSerializer<PrepCategory> serializer() {

        return new JsonSerializer<PrepCategory>() {
            public JsonElement serialize(PrepCategory pi, Type type,
                                         JsonSerializationContext context) {
            	if(pi == null){
            		return null;
            	}
            	
                PrepCategoryDTO d = PrepCategoryDTO.fromPrepCategory(pi);
                JsonObject result = (JsonObject) new JsonParser().parse(d.toJson());
                return result;

            }
        };
    }

    public PrepCategory sync() throws MissingTwinException{
    	
        SuitedToCategoryDTO sq = CategoriesDelegate.get(Long.valueOf(categoryId));
        
        if(sq == null){
            throw new MissingTwinException();
        }
        
        this.name = sq.name;
        
        this.companyName = sq.companyName;
        
        save();

        return this;
    }
    
    @On("cron.PrepCategorySync")
	public static class Sync extends Job{
		public void doJob(){
			if(Cache.safeAdd("PrepCategory.Sync", -1, "5s")){
				List<PrepCategory> categories = PrepCategory.findAll();
				//TODO: break this up into smaller chunks
				for(PrepCategory c : categories){
					try {
						c.sync();
					} catch (MissingTwinException e) {
						// TODO What do we want to do when we find that a suitedto category
						//has disappeared?
					}
				}

			}
		}
	}
}
