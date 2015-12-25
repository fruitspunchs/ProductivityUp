package finalproject.productivityup.libs;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by User on 12/17/2015.
 */
public class Utility {

    public static String formatDate(long dateInSeconds) {
        Date date = new Date(dateInSeconds * 1000);
        return DateFormat.getDateInstance().format(date);
    }

    public static String formatTime(long timeInSeconds) {
        Date date = new Date(timeInSeconds * 1000);
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm aa", Locale.US);
        String dateString = sdf.format(date);
        dateString = dateString.toLowerCase(Locale.US);
        return dateString;
    }
}
