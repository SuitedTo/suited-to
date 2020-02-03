package data.validation;


import net.sf.oval.Validator;
import net.sf.oval.configuration.annotation.AbstractAnnotationCheck;
import net.sf.oval.context.OValContext;
import play.db.jpa.Blob;

import java.util.Map;
import java.util.TreeMap;

public class ImageCheck extends AbstractAnnotationCheck<Image> {

    String alias;
    boolean typeCheckOnly;
    boolean required;
	
	//defaults
	final static String ALIAS = "Image";
    final static boolean REQUIRED = true;
    final static boolean TYPE_CHECK_ONLY = false;

    @Override
    public void configure(Image annotation) {
        this.alias = annotation.alias();
        this.required = annotation.required();
        this.typeCheckOnly = annotation.typeCheckOnly();
        setMessage(typeCheckOnly?"validation.image.type":"validation.image");
    }
    
	@Override
    public boolean isSatisfied(Object validatedObject, Object image, OValContext context, Validator validator) {
		requireMessageVariablesRecreation();
		if (!(image instanceof Blob)) {
			return false;
		}

		if (!((Blob) image).type().equals("image/jpeg") &&
				!((Blob) image).type().equals("image/png") &&
				!((Blob) image).type().equals("image/gif") &&
				!((Blob) image).type().equals("image/tiff")) {
			return false;
		}

		return true;
	}
	
	@Override
    protected Map<String, String> createMessageVariables(){
     		Map<String, String> vars = new TreeMap<String, String>();
     		
     		vars.put("1", String.valueOf(alias));
     		
     		return vars;
     	}
}

