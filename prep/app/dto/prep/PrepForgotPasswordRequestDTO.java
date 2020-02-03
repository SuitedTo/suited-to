package dto.prep;


public class PrepForgotPasswordRequestDTO extends PrepDTO {
    public String email;

    public static PrepForgotPasswordRequestDTO fromPrepEmail(String email){
        if(email == null){
            return null;
        }
        PrepForgotPasswordRequestDTO pf = new PrepForgotPasswordRequestDTO();
        pf.email = email;

        return pf;
    }
}
