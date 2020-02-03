
package data.binding.types;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import play.data.binding.TypeBinder;

/**
 * <p>Binds a string by simply passing it through as the value.</p>
 */
public class DummyBinder implements TypeBinder<String> {

    public Object bind(String name, Annotation[] annotations, String value, 
            Class actualClass, Type genericType) throws Exception {
        return value;
    }
    
}
