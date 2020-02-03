package enums;

public enum US_State {
    AL,
    AK,
    AZ,
    AR,
    CA,
    CO,
    CT,
    DE,
    DC,
    FL,
    GA,
    HI,
    ID,
    IL,
    IN,
    IA,
    KS,
    KY,
    LA,
    ME,
    MT,
    NE,
    NV,
    NH,
    NJ,
    NM,
    NY,
    NC,
    ND,
    OH,
    OK,
    OR,
    MD,
    MA,
    MI,
    MN,
    MS,
    MO,
    PA,
    RI,
    SC,
    SD,
    TN,
    TX,
    UT,
    VT,
    VA,
    WA,
    WV,
    WI,
    WY;
    
    public static US_State fromString (String state){
    	if(state != null){
    		for(US_State s : US_State.values()){
    			if(s.toString().equals(state)){
    				return s;
    			}
    		}
    	}
    	return null;
    }

}
