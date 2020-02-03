package dto.prep;

import com.google.gson.Gson;
import models.User;

public class PrepRegistrationDTO {

    public String id;

	public String email;

	public String password;

    public String confirmPassword;

    public String externalAuthProviderId;

    public String externalAuthProvider;

    public String firstName;

    public String profilePictureUrl;

	public static PrepRegistrationDTO fromJson(String json){
		try {
            Gson gson = new Gson();
            PrepRegistrationDTO pc = gson.fromJson(json, PrepRegistrationDTO.class);
            return pc;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
	}
}
