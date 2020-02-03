package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import play.db.jpa.JPA;

@Entity
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Job extends ModelBase {
	
	public static final String defaultJobName = "To Be Determined";
	
	public static final String defaultJobDescription =
			"This job is for any candidates that have not been assigned to a job within the company yet.";

    @ManyToOne
    public Company company;
    
    public String name;
    
    public String description;


    /***********************************************************************************
     * Relationships to Child Entities                                                 *
     ***********************************************************************************/

    @ManyToMany
    @JoinTable(name = "JOB_CATEGORY")
    public List<Category> categories = new ArrayList<Category>();


    @OneToMany(cascade = CascadeType.REMOVE, mappedBy = "job")
    private List<CandidateJobStatus> candidateJobStatuses;  //just here to support cascade deletes



    public Job(Company company, String name, String description) {
        this.company = company;
        this.name = name;
        this.description = description;
    }

    public Job(Company company) {
        this(company, Job.defaultJobName, Job.defaultJobDescription);
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * Returns true if one or more candidates are associated with this job
     * @return
     */
    public boolean hasCandidates() {
        boolean debug =  CandidateJobStatus.find("byJob", this).first() != null;
        return debug;
    }

    @Override
    public int compareTo(Object j) {
        if (j == null)
            return 0;

        return name.compareTo(((Job) j).name);
    }

    public static List<String> getJobNamesByCompany(long id) {
        Query query = JPA.em().createQuery("SELECT DISTINCT name FROM Job WHERE company_id = :company_id  ORDER BY name");
        query.setParameter("company_id", id);

        List<String> results = query.getResultList();

        return results;
    }
}
