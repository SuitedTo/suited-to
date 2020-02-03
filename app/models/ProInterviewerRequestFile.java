package models;

import db.jpa.S3Blob;
import enums.RoleValue;

import javax.persistence.Entity;
import javax.persistence.OneToOne;

@Entity
public class ProInterviewerRequestFile extends File {
    @OneToOne
    public ProInterviewerRequest proInterviewerRequest;

    public ProInterviewerRequestFile(String name, String type, S3Blob contents) {
        super(name, type, contents);
    }

    @Override
    public boolean hasAccess(User user) {
        Boolean allowed = user.hasRole(RoleValue.APP_ADMIN);
        ProInterviewerRequest proInterviewerRequest =
                ProInterviewerRequest.find("byProInterviewerRequestFile", this).first();
        if (proInterviewerRequest != null) {
            allowed = allowed || user.equals(proInterviewerRequest.user);
        }
        return allowed;
    }

    public String getTemporaryURL() {
        return contents.getTemporarySignedUrl(name).toString();
    }
}
