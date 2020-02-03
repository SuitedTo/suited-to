package dto.prep;

import models.prep.PrepClientSession;

public class PrepSessionDTO extends PrepDTO{
	
	public String sessionId;
	
	public static PrepSessionDTO fromPrepSession(PrepClientSession session){
		PrepSessionDTO dto = new PrepSessionDTO();
		dto.sessionId = session.sessionId;
		return dto;
	}
}
