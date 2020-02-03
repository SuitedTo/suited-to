package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import enums.JobStatus;
import enums.RoleValue;

import play.data.validation.Required;

@Entity
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class CompanyJobStatus extends ModelBase implements Restrictable{
	
	@ManyToOne
	public Company company;

	/**
     * The status name
     */
    @Required
    public String name = JobStatus.defaultStatus().toString();
    
    
    public CompanyJobStatus(Company company, String name){
    	this.company = company;
    	this.name = name;
    }


	@Override
	public boolean hasAccess(User user) {
		if(user == null){
			return false;
		}
		if(user.hasRole(RoleValue.APP_ADMIN)){
			return true;
		}
		return user.hasRole(RoleValue.COMPANY_ADMIN) && company.equals(user.company);
	}
    
    
}
