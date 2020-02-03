package controllers.prep.base;

public class Main extends PrepBaseController {

    /*************************************************
     * Static Fields                                 *
     *************************************************/

    public static final String OPEN_ID_EMAIL = "email";


    /*************************************************
     * Actions                                       *
     *************************************************/

    public static void home() {
        render();
    }

    public static void logout() {
        session.clear();
        home();
    }
}
