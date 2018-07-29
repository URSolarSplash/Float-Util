package Utility;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Log {

    private static long startTime = System.currentTimeMillis();

    public static void log(String text){
        String formattedTime = "[ " + convertTime(System.currentTimeMillis() - startTime)+" ] ";
        System.out.println(formattedTime + text);
    }

    public static String convertTime(long millis) {
        return (new SimpleDateFormat("mm:ss:SSS")).format(new Date(millis));
    }

    public static void resetTime(){

        startTime = System.currentTimeMillis();
    }
}
