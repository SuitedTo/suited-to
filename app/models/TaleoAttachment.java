package models;

import models.ModelBase;
import play.db.jpa.Blob;

import javax.persistence.Entity;

/**
 * Created with IntelliJ IDEA.
 * User: michellerenert
 * Date: 9/14/12
 * Time: 9:49 AM
 * To change this template use File | Settings | File Templates.
 */
@Entity
public class TaleoAttachment extends ModelBase {
    public String taleoFileName;
    public Blob taeloResume;

}

