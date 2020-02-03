package data.validation;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import net.sf.oval.Validator;
import net.sf.oval.configuration.annotation.AbstractAnnotationCheck;
import net.sf.oval.context.OValContext;
import play.data.validation.MinSize;
import play.data.validation.Required;
import play.db.Model.BinaryField;
import play.exceptions.UnexpectedException;

public class PasswordCheck  extends AbstractAnnotationCheck<Password> {
	
    int min;
    int max;
    String alias;
    boolean required;
    
    //defaults
    final static String ALIAS = "password";
    public final static int MIN = 6;
    public final static int MAX = 20;
    final static boolean REQUIRED = true;
    final static String mes = "validation.password";
    
    @Override
    public void configure(Password annotation) {
        this.alias = annotation.alias();
        this.min = annotation.min();
        this.max = annotation.max();
        this.required = annotation.required();
        setMessage(annotation.message());
    }
    
    @Override
    public boolean isSatisfied(Object validatedObject, Object value, OValContext context, Validator validator) {
    	requireMessageVariablesRecreation();
        if ((value == null) || (String.valueOf(value).length() == 0)) {
            return !required;
        }
        if (value instanceof String) {
        	String pwdStr = value.toString().trim();
            return (pwdStr.length() >= min) && (pwdStr.length() <= max);
        }
        return false;
    }
    
    @Override
    protected Map<String, String> createMessageVariables(){
     		Map<String, String> vars = new TreeMap<String, String>();
     		
     		vars.put("1", String.valueOf(alias));
     		
     		vars.put("2", String.valueOf(min));
     		
     		vars.put("3", String.valueOf(max));
     		
     		return vars;
     	}
}