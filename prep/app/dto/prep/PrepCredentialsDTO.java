package dto.prep;

import com.google.gson.Gson;


public class PrepCredentialsDTO extends PrepDTO{
	
    public String email;

    public String password;

    public String externalAuthProviderId;
    public String externalAuthProviderAccessToken;
    public String externalAuthProviderAccessTokenSecret;
    public String externalAuthProvider;
    
    public static PrepCredentialsDTO fromJson(String json) {
        try {
            Gson gson = new Gson();
            PrepCredentialsDTO pc = gson.fromJson(json, PrepCredentialsDTO.class);
            return pc;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
