package dto.prep;

import java.util.Date;

import models.prep.PrepModelBase;


public class PrepModelBaseDTO extends PrepDTO{
	
	public long id;
	public Date created;
	public Date updated;
	
	public static PrepModelBaseDTO fromPrepModelBase(PrepModelBase base){
		PrepModelBaseDTO d = new PrepModelBaseDTO();
		d.id = base.id;
		d.created = base.created;
		d.updated = base.updated;
		return d;
	}
}
