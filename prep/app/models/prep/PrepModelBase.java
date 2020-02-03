package models.prep;

import java.lang.reflect.Type;

import javax.persistence.MappedSuperclass;

import models.ModelBase;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import common.utils.JsonUtil;

import dto.prep.PrepModelBaseDTO;

@MappedSuperclass
//Extending SuitedTo's ModelBase to make a few things easier for now
public abstract class PrepModelBase<T extends PrepModelBase> extends ModelBase implements SerializerProvider<T>{

		
		public T update(T other){
			//TODO:
			return null;
		}
		
		public JsonObject toJsonObject(){
			return toJsonObject(this);
		}
		
		public static JsonObject toJsonObject(PrepModelBase entity){
			
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
		
		private JsonSerializer<PrepModelBase> superSerializer(){
			return new JsonSerializer<PrepModelBase>(){
				@Override
				public JsonElement serialize(PrepModelBase entity, Type type,
						JsonSerializationContext context) {
					PrepModelBaseDTO data = PrepModelBaseDTO.fromPrepModelBase(entity);
					return new JsonParser().parse(data.toJson());
				}
			};
		}

		public abstract JsonSerializer<T> serializer();
		
}
