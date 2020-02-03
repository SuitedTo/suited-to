package integration.taleo;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

public class TaleoCredentials {

	private String companyCode;
	private String userName;
	private String password;
	
	private static XStream xstream = new XStream(new StaxDriver());
	
	public TaleoCredentials(String companyCode, String userName, String password) {
		super();
		if((companyCode == null) || (userName == null) || (password == null)){
			throw new IllegalArgumentException();
		}
		this.companyCode = companyCode;
		this.userName = userName;
		this.password = password;
	}

	public String getCompanyCode() {
		return companyCode;
	}

	public void setCompanyCode(String companyCode) {
		this.companyCode = companyCode;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	public String toXML(){
		return xstream.toXML(this);
	}
	
	public static TaleoCredentials fromXML(String xml){
		return (TaleoCredentials) new XStream(new StaxDriver()).fromXML(xml);
	}
	
}
