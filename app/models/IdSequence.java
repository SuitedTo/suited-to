package models;

import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "ID_SEQ")
public class IdSequence extends Model {

    public String name;
    public Long value;

}

