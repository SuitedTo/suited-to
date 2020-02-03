package models;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import enums.RoleValue;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class CandidateJobStatus extends ModelBase implements Restrictable{
	
	@ManyToOne
	public Job job;
	
	@ManyToOne
	public Candidate candidate;
	
	@ManyToOne
	public CompanyJobStatus companyJobStatus;

    /**
     * If the user has access to the company the have access to its candidates
     * @param user User
     * @return whether the user has access to this candidate
     */
    @Override
    public boolean hasAccess(User user) {
        return (user != null) &&
                ((this.companyJobStatus != null &&
                        this.companyJobStatus.hasAccess(user)) ||
                        user.hasRole(RoleValue.APP_ADMIN));
    }

}
