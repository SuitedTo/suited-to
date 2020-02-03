package integration.taleo;

import controllers.Taleo;
import integration.taleo.generated.*;
import integration.taleo.types.EntityType;
import play.Logger;
import play.cache.Cache;

import javax.xml.ws.BindingProvider;
import java.io.IOException;
import java.net.URL;

public class TaleoService {
	private TaleoCredentials credentials;
	private String accessToken;
	private IWebAPI api;
	
	private static IWebAPIService service;
	
	public static TaleoService INSTANCE() throws WebServicesException{
		return INSTANCE(Taleo.getCredentialsForConnectedUser(), false);
	}
	
	public static TaleoService INSTANCE(boolean validate) throws WebServicesException{
		return INSTANCE(Taleo.getCredentialsForConnectedUser(), validate);
	}
	
	public static TaleoService INSTANCE(TaleoCredentials credentials) throws WebServicesException{
		return INSTANCE(credentials, false);
	}

	public static TaleoService INSTANCE(TaleoCredentials credentials, boolean validate) throws WebServicesException{
		
		if(credentials == null){
			throw new WebServicesException("Unable to get Taleo instance without credentials", null);
		}
		
		if(service == null){
			service = new IWebAPIService();
		}
		
		IWebAPI api = service.getPort(IWebAPI.class);
		
		if(!validate){
			Object cachedAccessToken = Cache.get(getAccessTokenCacheKey(credentials.getCompanyCode(), credentials.getUserName()));
			Object cachedLocationToken = Cache.get(getLocationCacheKey(credentials.getCompanyCode(), credentials.getUserName()));
			if((cachedAccessToken != null) && (cachedLocationToken != null)){
				
				return new TaleoService((String)cachedAccessToken, (String)cachedLocationToken, api,
						credentials);
				
			}
		}
		
		String url = getURL(credentials.getCompanyCode()).toString();

		
		TaleoService taleo = new TaleoService(null, url, api, credentials);
		
		Cache.set(getAccessTokenCacheKey(credentials.getCompanyCode(), credentials.getUserName()), taleo.accessToken, "24h");
		Cache.set(getLocationCacheKey(credentials.getCompanyCode(), credentials.getUserName()), url, "24h");
		
		return taleo;
	}
	
	private TaleoService(String accessToken, String url,
			TaleoCredentials credentials) throws WebServicesException{
		this (accessToken, url, new IWebAPIService().getPort(IWebAPI.class),
				credentials);
	}
	
	private TaleoService(String accessToken, String url, IWebAPI api,
			TaleoCredentials credentials) throws WebServicesException{
		
		
		this.credentials = credentials;
		this.api = api;
		
		BindingProvider bp = (BindingProvider)api;
        bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, url);

		if(accessToken == null){
			this.login();
		}else{
			this.accessToken = accessToken;
		}
	}
	
	private static String getAccessTokenCacheKey(String companyCode, String userName){
		return "_TaleoAccess" + companyCode + "_" + userName;
	}
	
	private static String getLocationCacheKey(String companyCode, String userName){
		return "_TaleoLocation" + companyCode + "_" + userName;
	}
	
	private String getFaultCacheKey(){
		return "_TaleoFault" + credentials.getCompanyCode() + "_" + credentials.getUserName();
	}
	
	private void login() throws WebServicesException{
		
		try{
			this.accessToken = api.login(credentials.getCompanyCode(),
					credentials.getUserName(),
					credentials.getPassword());
			
			if(!TaleoHelper.isAdmin(this, credentials.getUserName())){
				throw new WebServicesException("Only a Taleo administrator can connect", null);
			}		
			
			
		}catch(WebServicesException e){
			Logger.error("Could not log in to Taleo: %s", e.getMessage());
			throw e;
		}
		
	}
	
	private static URL getURL(String companyCode)  throws WebServicesException{
		
		try {
			return new TaleoLocation(companyCode).getURL();
		} catch (IOException e) {
			Logger.error("Unable to get Taleo URL for company %s", companyCode);
			throw new WebServicesException("Unable to get Taleo URL for company", null);
		}
	}
	
	private void recover(Exception e) throws WebServicesException{
		
		if(Cache.get(getFaultCacheKey()) != null){
			throw new WebServicesException("Error while accessing Taleo. Could not recover." + e.getMessage(), null);
		}
		
		/*
		 * Could look at the exception and try to figure out what went wrong
		 * but we only have one trick right now so we just try it.
		 */
		login();
		
		Cache.add(getFaultCacheKey(), e.getMessage(), "1mn");
	}
	
	
	//---------- API delegation methods- Add as needed ----------------
	
	
	public long createEvent(CalendarEventBean event)
			throws WebServicesException {
		try{
			return api.createEvent(accessToken, event);
		}catch(Exception e){
			recover(e);
			return createEvent(event);
		}
	}

	public void updateEvent(CalendarEventBean event)
			throws WebServicesException {
		try{
			api.updateEvent(accessToken, event);
		}catch(Exception e){
			recover(e);
			updateEvent(event);
		}
	}

	public void deleteEvent(long eventId)
			throws WebServicesException {
		try{
			api.deleteEvent(accessToken, eventId);
		}catch(Exception e){
			recover(e);
			deleteEvent(eventId);
		}
	}

	public CalendarEventBean getEventById(long eventId)
			throws WebServicesException {
		try{
			return api.getEventById(accessToken, eventId);
		}catch(Exception e){
			recover(e);
			return getEventById(eventId);
		}
	}

	public LongArr getEventByEntity(EntityType type, long entityId)
			throws WebServicesException {
		try{
			return api.getEventByEntity(accessToken, type.name(), entityId);
		}catch(Exception e){
			recover(e);
			return getEventByEntity(type, entityId);
		}
	}
	
	public CandidateBean getCandidateById(long candidateId)
			throws WebServicesException {
		try{
			return api.getCandidateById(accessToken, candidateId);
		}catch(Exception e){
			recover(e);
			return getCandidateById(candidateId);
		}
	}

	public SearchResultArr searchCandidate(Map args)
			throws WebServicesException {
		try{
			return api.searchCandidate(accessToken, args);
		}catch(Exception e){
			recover(e);
			return searchCandidate(args);
		}
	}
	
	public ByteArr getBinaryResume(long candidateId)
			throws WebServicesException {
		try{
			return api.getBinaryResume(accessToken, candidateId);
		}catch(Exception e){
			recover(e);
			return getBinaryResume(candidateId);
		}
	}
	
	public SearchResultArr searchUser(Map args)
			throws WebServicesException {
		try{
			return api.searchUser(accessToken, args);
		}catch(Exception e){
			recover(e);
			return searchUser(args);
		}
	}
	
	public UserBean getUserById(long id)
			throws WebServicesException {
		try{
			return api.getUserById(accessToken, id);
		}catch(Exception e){
			recover(e);
			return getUserById(id);
		}
	}
	
}
