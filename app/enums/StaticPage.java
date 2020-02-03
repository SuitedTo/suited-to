package enums;

public enum StaticPage {

	LEGAL_DISCLAIMER;
	
	public String getURL(){
		return urlLookup(name());
	}
	
	private static String urlLookup(String key){
		return play.Play.configuration.getProperty("static.page." + key);
	}
}
