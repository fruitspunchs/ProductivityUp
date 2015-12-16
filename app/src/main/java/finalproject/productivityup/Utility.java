package finalproject.productivityup;

import java.text.DateFormat;
import java.util.Date;

/**
 * Created by User on 12/17/2015.
 */
public class Utility {
    public static String formatDate(long dateInSeconds) {
        Date date = new Date(dateInSeconds * 1000);
        return DateFormat.getDateInstance().format(date);
    }
}
