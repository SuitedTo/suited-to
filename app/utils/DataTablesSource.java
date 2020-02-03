package utils;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import models.Company;
import models.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Parent class for DataTablesSource implementations which are custom data representations for consumption by the
 * JQuery Datatables plugin.
 */
public abstract class DataTablesSource {

    /**
     * Total number of records in the database;
     */
    protected Long iTotalRecords;

    /**
     * Total number of records being displayed
     */
    protected Long iTotalDisplayRecords;

    /**
     * Just something for datatables.
     */
    protected Long sEcho;

    /**
     * The internal representation of the collection.
     */
    protected List<SafeStringArrayList> aaData;

    protected DataTablesSource(Long totalRecords, Long sEcho){
        this(totalRecords, totalRecords, sEcho);
    }

    protected DataTablesSource(Long iTotalRecords, Long iTotalDisplayRecords, Long sEcho) {
        this.aaData = new ArrayList<SafeStringArrayList>();
        this.iTotalRecords = iTotalRecords;
        this.iTotalDisplayRecords = iTotalDisplayRecords;
        this.sEcho = sEcho;
    }

    /**
     * Custom Gson Serializer that filters out troublesome Model classes with circular references
     */
    private static final Gson serializer = new GsonBuilder().setExclusionStrategies(new ExclusionStrategy() {

        public boolean shouldSkipClass(Class<?> clazz) {
            return (clazz == Company.class) || (clazz == User.class);
        }

        /**
         * Custom field exclusion goes here
         */
        public boolean shouldSkipField(FieldAttributes f) {
            return false;
        }

    }).create();

    /**
     * Renders this DataTablesSource as JSON
     * @return String representation of the DatatablesSource as JSON
     */
    public String toJson() {
        return serializer.toJson(this);
    }
}
