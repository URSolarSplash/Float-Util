import java.text.SimpleDateFormat;
import java.util.Date;

public class Utilities {
    public static String convertTime(long millis) {
        return (new SimpleDateFormat("mm:ss:SSS")).format(new Date(millis));
    }
}
