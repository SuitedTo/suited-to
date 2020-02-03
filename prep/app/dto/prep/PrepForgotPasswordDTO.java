package dto.prep;

public class PrepForgotPasswordDTO extends PrepDTO {
    public String email;
    public String temp;
    public String password;
    public String confirm;

    public static PrepForgotPasswordDTO request(String email, String temp, String password, String confirm){
        if (email == null || temp == null || password == null || confirm == null){
            return null;
        }

        PrepForgotPasswordDTO pf = new PrepForgotPasswordDTO();
        pf.email = email;
        pf.temp = temp;
        pf.password = password;
        pf.confirm = confirm;

        return pf;
    }
}
