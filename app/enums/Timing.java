package enums;

import play.i18n.Messages;

public enum Timing {
    SHORT(1),
    MEDIUM(5),
    LONG(10);
    
    private int duration;

    Timing(int duration){
    	this.duration = duration;
    }
    
    public int getDuration(){
    	return duration;
    }
    
    @Override
    public String toString() {
        return Messages.get(getClass().getName() + "." + name());
    }
}
