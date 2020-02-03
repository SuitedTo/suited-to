package models.prep.mappedsuperclasses;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import models.prep.ewrap.DefaultQueryResultBuilder;
import models.prep.ewrap.MapColumn;
import models.prep.ewrap.QueryBuildContext;

import org.apache.commons.lang.StringUtils;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.ebean.RawSql;
import com.avaje.ebean.RawSql.ColumnMapping;
import com.avaje.ebean.RawSqlBuilder;

/**
 * An SqlEntity is an entity that is based on a RawSql query.
 * 
 * Implement a getter to transform the value of any field.
 * 
 * Be sure to annotate your SqlEntity class with both @Entity and @Sql 
 * 
 * @author joel
 *
 * @param <T>
 */
@MappedSuperclass
public abstract class SqlEntity<T>{

	@Transient
	protected RawSqlBuilder builder;

	@Transient
	protected RawSql rawSql;

	@Transient
	Query<T> query;


	private final SqlEntity<T> init(){
		this.builder = RawSqlBuilder.parse(getSql());
		addColumnMappings();
		this.rawSql = this.builder.create();
		this.query = Ebean.find(getEntityClass()).setRawSql(rawSql);
		return this;
	}

	/**
	 * 
	 * Map db columns to bean properties.
	 * 
	 * @param dbColumn
	 * @param propertyName
	 * @return
	 */
	public SqlEntity<T> addColumnMapping(String dbColumn, String propertyName){
		builder = builder.columnMapping(dbColumn, propertyName);
		return this;
	}

	/**
	 * Get a result builder for the given SqlEntity
	 * 
	 * @param entity
	 * @return
	 */
	public static <T> DefaultQueryResultBuilder<T> getQueryResultBuilder(SqlEntity<T> entity, 
			QueryBuildContext context){

		return DefaultQueryResultBuilder.instance(entity.init().query, context);

	}

	/**
	 * Return the sql statement that this entity is built on.
	 * 
	 * @return
	 */
	protected abstract String getSql();

	/**
	 * Return the entity class.
	 * 
	 * @return
	 */
	protected abstract Class<T> getEntityClass();

	/**
	 * Process all MapColumn annotations to map all db columns to bean fields.
	 * 
	 * @return
	 */
	protected void addColumnMappings(){
		Field[] fields = getEntityClass().getDeclaredFields();
		for (Field field : fields) {
			Annotation annotation = field.getAnnotation(MapColumn.class);
			if(annotation != null){
				MapColumn column = (MapColumn)annotation;
				if(StringUtils.isEmpty(column.propertyName())){
					addColumnMapping(column.dbColumnName(), field.getName());
				} else {
					addColumnMapping(column.dbColumnName(), column.propertyName());
				}
			}
		}
	}

	ColumnMapping columnMapping() {
		return rawSql.getColumnMapping();
	}

}
