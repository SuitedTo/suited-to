package models.prep;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import play.Play;
import play.PlayPlugin;
import play.data.binding.BeanWrapper;
import play.data.binding.Binder;
import play.data.validation.Validation;
import play.db.Model;
import play.exceptions.UnexpectedException;
import play.mvc.Scope.Params;

import models.ModelBase;
import modules.ebean.EbeanContext;
import modules.ebean.EbeanSupport;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.Query;
import com.avaje.ebean.Update;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import common.utils.JsonUtil;

import dto.prep.PrepModelBaseDTO;

@MappedSuperclass
//Extending SuitedTo's ModelBase to make a few things easier for now
public abstract class EbeanModelBase extends EbeanSupport implements SerializerProvider{

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	public Long id;

	@Temporal(TemporalType.TIMESTAMP)
	public Date created;

	@Temporal(TemporalType.TIMESTAMP)
	public Date updated;
		
		public JsonObject toJsonObject(){
			return toJsonObject(this);
		}
		
		public static JsonObject toJsonObject(EbeanModelBase entity){
			
			JsonObject serializedBase = (JsonObject) entity.superSerializer().serialize(entity, null, null);
			JsonObject serializedEntity = (JsonObject)entity.serializer().serialize(entity, null,null);
			
			return JsonUtil.merge(serializedBase, serializedEntity);
		}
		
		public String toJson(){
			String json = toJsonObject().toString();
			if(json == null){
				return "{}";
			}
			return json;
		}
		
		private JsonSerializer<EbeanModelBase> superSerializer(){
			return new JsonSerializer<EbeanModelBase>(){
				@Override
				public JsonElement serialize(EbeanModelBase entity, Type type,
						JsonSerializationContext context) {
					//TODO
					return null;
				}
			};
		}

		public abstract JsonSerializer serializer();
		
		public boolean hasBeenSaved() {
			return this.id != null;
		}


		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			EbeanModelBase other = (EbeanModelBase) obj;
			if (id == null) {
				if (other.id != null)
					return false;
			} else if (!id.equals(other.id))
				return false;
			return true;
		}

    @Override
    public <T extends EbeanSupport> T save(){
        if(!hasBeenSaved()){
            created = new Date();
        }

        updated = new Date();

        return super.save();
    }
		
}
