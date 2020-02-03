package models.prep;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import modules.ebean.Finder;

import com.google.gson.JsonSerializer;

@Entity
@Table(name = "PREP_JobName")
public class PrepJobName extends EbeanModelBase {
    @ManyToOne
    @JoinColumn(name="prep_job_id")
    public PrepJob prepJob;
    
    public String name;
    
    public static Finder<Long,PrepJobName> find = new Finder<Long,PrepJobName>(
            Long.class, PrepJobName.class
    );

    public PrepJobName(PrepJob prepJob, String name) {
        this.prepJob = prepJob;
        this.name = name;
    }

	@Override
	public JsonSerializer serializer() {
		// TODO Auto-generated method stub
		return null;
	}
}
