package controllers;

import models.CandidateFile;
import play.data.validation.Required;
import controllers.deadbolt.RestrictedResource;
import enums.CandidateDocType;

public class CandidateFiles extends ControllerBase{


    
    @RestrictedResource(name = {"models.CandidateFile"}, staticFallback = true)
    public static void updateDocType(@Required Long id, @Required CandidateDocType docType){
    	if (validation.hasErrors()) {
    		badRequest();
    	}
    	
    	CandidateFile file = CandidateFile.findById(id);
    	if(file == null){
    		badRequest();
    	}
    	
    	file.docType = docType;
    	
    	file.save();
    	
    	ok();
    }
}