package controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.util.StringUtils;
import org.hibernate.exception.ConstraintViolationException;

import models.Company;
import models.CompanyJobStatus;
import play.mvc.Util;
import play.mvc.With;

import com.google.gson.Gson;

import controllers.deadbolt.Deadbolt;
import controllers.deadbolt.Restrict;
import controllers.deadbolt.RestrictedResource;
import controllers.deadbolt.Restrictions;
import enums.RoleValue;

@With(Deadbolt.class)
@Restrictions({@Restrict(RoleValue.APP_ADMIN_STRING), @Restrict(RoleValue.COMPANY_ADMIN_STRING)})
public class CompanyJobStatuses extends ControllerBase {

	@Util
	public static List<CompanyJobStatus> getStatusesForCompany(Company company){
		if(company != null){
			company = company.merge();
			return CompanyJobStatus.find("byCompany", company).fetch();
		}
		return new ArrayList<CompanyJobStatus>();
    }
	
    public static void create(Long companyId, String name){
    	if((companyId == null) || (name == null)){
    		badRequest();
    	}
    	Company company = Company.findById(companyId);
    	if(company == null){
    		badRequest();
    	}
    	
    	if(!company.hasAccess(Security.connectedUser())){
    		forbidden();
    	}
    	
    	Map<String, Object> result = new HashMap<String, Object>();
    	result.put("id", ((CompanyJobStatus)new CompanyJobStatus(company, name).save()).id);
    	renderJSON(new Gson().toJson(result));
    }
    
    @RestrictedResource(name = {"models.CompanyJobStatus"})
    public static void update(Long id, String name){
    	
    	CompanyJobStatus status = CompanyJobStatus.findById(id);
    	if(status == null){
    		notFound();
    	}
    	
    	if(StringUtils.isEmpty(name)){
    		badRequest();
    	}
    	status.name = name;
    	status.save();
    	
    	ok();
    }
	
	@RestrictedResource(name = {"models.CompanyJobStatus"})
    public static void delete(Long id){
		CompanyJobStatus status = CompanyJobStatus.findById(id);
		
		try{
			status.delete();
		} catch(Exception e){
			if(e.getCause() instanceof ConstraintViolationException){
				error(409, "Constraint violation");
			}
			error();
		}
        ok();
    }
}
