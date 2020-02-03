package models.prep.ewrap;

/**
 * Convert a string into some other type. Some of the more
 * common converters are available in the QueryFilter class
 * but you can also create your own custom converter to build
 * any sort of object from a string.
 *
 * @author joel
 *
 * @param <P>
 */
public interface ParamConverter<P>{
    public P convert(String value) throws QueryFilter.ParamConversionException;
}
