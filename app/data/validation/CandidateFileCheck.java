package data.validation;


import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import javax.imageio.ImageIO;

import net.sf.oval.Validator;
import net.sf.oval.configuration.annotation.AbstractAnnotationCheck;
import net.sf.oval.context.OValContext;

import play.Logger;
import play.data.validation.Check;
import play.db.jpa.Blob;
import play.i18n.Messages;

public class CandidateFileCheck extends AbstractAnnotationCheck<CandidateFile> {
	
	int size;
    String alias;
    boolean required;
	
	//defaults
	final static String ALIAS = "Candidate file";
	final static int MAX_SIZE = 1;//mb
    final static boolean REQUIRED = true;
    
    final static String[] supportedFormats = new String[]{
    	"application/msword",
    	"application/vnd.openxmlformats-officedocument.wordprocessingml.document",
    	"application/pdf",
    	"image/jpg",
    	"image/jpeg",
    	"image/png",
    	"text/plain"
    };

    @Override
    public void configure(CandidateFile annotation) {
        this.alias = annotation.alias();
        this.size = annotation.size();
        this.required = annotation.required();
        setMessage("validation.file");
    }
    
	@Override
    public boolean isSatisfied(Object validatedObject, Object blob, OValContext context, Validator validator) {
		requireMessageVariablesRecreation();
		if (!(blob instanceof Blob)) {
			return false;
		}

		long size = ((Blob) blob).getFile().length();
		if (size > (MAX_SIZE * 1000000)) {
			return false;
		}

		boolean formatAcceptable = false;
		for(String format : supportedFormats){
			if (((Blob) blob).type().equals(format)) {
				formatAcceptable = true;
				break;
			}
		}
		return formatAcceptable;
	}
	
	@Override
    protected Map<String, String> createMessageVariables(){
     		Map<String, String> vars = new TreeMap<String, String>();
     		
     		vars.put("1", String.valueOf(alias));
     		
     		vars.put("2", String.valueOf(MAX_SIZE));
     		
     		return vars;
     	}
}

