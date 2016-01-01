package finalproject.productivityup.libs;

import android.content.Context;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import finalproject.productivityup.R;

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

    //TODO: Make date calculation more exact.
    public static String formatTimeLeft(Context context, long timeLeftInSeconds) {
        String formattedString = "";
        boolean hasFirstMatch = false;

        final int SECONDS_PER_YEAR = 31536000;
        final int SECONDS_PER_DAY = 86400;
        final int SECONDS_PER_MONTH = SECONDS_PER_DAY * 30;
        final int SECONDS_PER_HOUR = 3600;
        final int SECONDS_PER_MINUTE = 60;

        if (timeLeftInSeconds >= SECONDS_PER_YEAR) {
            int years = 0;

            while (timeLeftInSeconds >= SECONDS_PER_YEAR) {
                timeLeftInSeconds -= SECONDS_PER_YEAR;
                years++;
            }

            String suffix = "";
            if (years == 1) {
                suffix = context.getString(R.string.year);
            } else if (years > 1) {
                suffix = context.getString(R.string.years);
            }

            formattedString += years + suffix;

            hasFirstMatch = true;
        }

        if (timeLeftInSeconds >= SECONDS_PER_MONTH) {
            int months = 0;

            while (timeLeftInSeconds >= SECONDS_PER_MONTH) {
                timeLeftInSeconds -= SECONDS_PER_MONTH;
                months++;
            }

            String suffix = "";
            if (months == 1) {
                suffix = context.getString(R.string.month);
            } else if (months > 1) {
                suffix = context.getString(R.string.months);
            }

            if (hasFirstMatch) {
                formattedString += " ";
            }

            formattedString += months + suffix;

            if (hasFirstMatch) {
                return formattedString;
            }

            hasFirstMatch = true;
        }

        if (timeLeftInSeconds >= SECONDS_PER_DAY) {
            int days = 0;

            while (timeLeftInSeconds >= SECONDS_PER_DAY) {
                timeLeftInSeconds -= SECONDS_PER_DAY;
                days++;
            }

            String suffix = "";
            if (days == 1) {
                suffix = context.getString(R.string.day);
            } else if (days > 1) {
                suffix = context.getString(R.string.days);
            }

            if (hasFirstMatch) {
                formattedString += " ";
            }

            formattedString += days + suffix;

            if (hasFirstMatch) {
                return formattedString;
            }

            hasFirstMatch = true;
        }

        if (timeLeftInSeconds >= SECONDS_PER_HOUR) {
            int hours = 0;

            while (timeLeftInSeconds >= SECONDS_PER_HOUR) {
                timeLeftInSeconds -= SECONDS_PER_HOUR;
                hours++;
            }

            String suffix = "";
            if (hours == 1) {
                suffix = context.getString(R.string.hour);
            } else if (hours > 1) {
                suffix = context.getString(R.string.hours);
            }

            if (hasFirstMatch) {
                formattedString += " ";
            }

            formattedString += hours + suffix;

            if (hasFirstMatch) {
                return formattedString;
            }

            hasFirstMatch = true;
        }

        if (timeLeftInSeconds >= SECONDS_PER_MINUTE) {
            int minutes = 0;

            while (timeLeftInSeconds >= SECONDS_PER_MINUTE) {
                timeLeftInSeconds -= SECONDS_PER_MINUTE;
                minutes++;
            }

            String suffix = "";
            if (minutes == 1) {
                suffix = context.getString(R.string.minute);
            } else if (minutes > 1) {
                suffix = context.getString(R.string.minutes);
            }

            if (hasFirstMatch) {
                formattedString += " ";
            }

            formattedString += minutes + suffix;

            if (hasFirstMatch) {
                return formattedString;
            }

            hasFirstMatch = true;
        }

        //Display seconds if minutes first match
        if (hasFirstMatch) {
            formattedString += " ";
        }

        String suffix = "";
        if (timeLeftInSeconds == 1) {
            suffix = context.getString(R.string.second);
        } else if (timeLeftInSeconds > 1) {
            suffix = context.getString(R.string.seconds);
        }
        formattedString += timeLeftInSeconds + suffix;

        return formattedString;
    }
}
