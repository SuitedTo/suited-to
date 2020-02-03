package dto.prep;

import com.google.gson.Gson;
import com.stripe.Stripe;
import com.stripe.model.Customer;
import models.prep.PrepUser;
import play.Play;

public class PrepAccountUpdateDTO extends PrepDTO {

    public Long id;

    public String password;

    public String firstName;

    public String stripeToken;

    public String lastFourCardDigits;

    public String email;

    public static PrepAccountUpdateDTO fromJson(String json){
        try {
            Gson gson = new Gson();
            PrepAccountUpdateDTO pc = gson.fromJson(json, PrepAccountUpdateDTO.class);
            return pc;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static PrepAccountUpdateDTO fromModel(PrepUser pu) {
        PrepAccountUpdateDTO dto = new PrepAccountUpdateDTO();
        dto.id = pu.id;
        dto.firstName = pu.firstName;
        dto.email = pu.email;
        return dto;
    }
}
