package utils;

import play.Logger;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: phutchinson
 * Date: 3/5/13
 * Time: 8:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class LoggingUtil {
    public static void logElapsedTime(String message, Date start, Date end){
        StringBuilder outputBuilder = new StringBuilder();
        if(message != null){
            outputBuilder.append(message);
            outputBuilder.append(" ~ ");
        }

        outputBuilder.append(end.getTime() - start.getTime());
        outputBuilder.append(" milliseconds");

        Logger.info(outputBuilder.toString());
    }

    public static void logElapsedTime(Date start, Date end){
        logElapsedTime(null, start, end);
    }

}
