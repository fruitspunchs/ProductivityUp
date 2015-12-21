package finalproject.productivityup.libs;

import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

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
        Log.d("Format time", "Date: " + date);
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm aa");
        return sdf.format(date);
    }
}
