package models.prep;

import java.lang.reflect.Type;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import modules.ebean.Finder;
import play.data.validation.Required;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import dto.prep.PrepJobDTO;

@Entity
@Table(name = "PREP_Job")
public class PrepJob extends EbeanModelBase {
    @OneToOne
    @JoinColumn(name="primary_name_id")
    @Required
    public PrepJobName primaryName;
    // prepJobNames includes the primaryName
    @OneToMany(mappedBy = "prepJob", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<PrepJobName> prepJobNames;
    @OneToMany(mappedBy = "prepJob", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<PrepJobCategory> prepJobCategories;
    
    public static Finder<Long,PrepJob> find = new Finder<Long,PrepJob>(
            Long.class, PrepJob.class
    );

    @Override
    public JsonSerializer<PrepJob> serializer() {

        return new JsonSerializer<PrepJob>() {
            public JsonElement serialize(PrepJob pj, Type type,
                                         JsonSerializationContext context) {

                PrepJobDTO d = PrepJobDTO.fromPrepJob(pj);
                JsonObject result = (JsonObject) new JsonParser().parse(d.toJson());
                return result;

            }
        };
    }
}