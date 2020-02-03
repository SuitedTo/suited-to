package models;

import db.jpa.S3Blob;
import enums.CandidateDocType;
import enums.RoleValue;
import play.db.jpa.JPABase;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;

@Entity
public class CandidateFile extends File{

    /**
     * The candidate associated with this Candidate file. This may actually be null.  The candidate will be null when
     * uploading a new file for a candidate that has not yet been saved.  If the user continues and saves the new
     * candidate, this value will be populated, but if the do not eventually save the candidate then this field will
     * remain null.  CandidateFiles with null candidates that are more than a day or so old can be considered
     * "orphaned" and may be safely removed
     */
	@ManyToOne
	public Candidate candidate;

    /**
     * The user that created this CandidateFile
     */
    @ManyToOne
    public User creator;
    
    @Enumerated(EnumType.STRING)
	public CandidateDocType docType;

    public CandidateFile(String name, String type, CandidateDocType docType, S3Blob contents, User creator) {
        super(name, type, contents);
        this.creator = creator;
        this.docType = docType;
    }
    
	
	public CandidateFile(String name, String type, S3Blob contents, User creator) {
		this(name, type, CandidateDocType.OTHER, contents, creator);
		
	}


    /**
     * A user has access if he is the creator of this file, an application admin or has access to the candidate
     * @param user
     * @return
     */
	@Override
	public boolean hasAccess(User user) {

		return user.hasRole(RoleValue.APP_ADMIN) ||
                user.equals(creator) ||
                (candidate != null && candidate.hasAccess(user));
	}
	
        public String getTemporaryURL() {
            return contents.getTemporarySignedUrl(name).toString();
        }
        
	public String getDisplayType(){
		if(type.equals("application/msword")){
			return "text/html";
		}
		if(type.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")){
			return "text/html";
		}
		if(type.equals("text/plain")){
			return "text/html";
		}
		return type;
	}

    /**
     * Removes the File contents from S3 and deletes the CandidateFile record
     * @param <T> CandidateFile to delete
     * @return The CandidateFile that was just deleted
     */
    @Override
    public <T extends JPABase> T delete() {
        this.contents.delete();
        return super.delete();
    }
}
