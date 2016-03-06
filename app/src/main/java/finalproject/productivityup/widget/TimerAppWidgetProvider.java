package finalproject.productivityup.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import finalproject.productivityup.R;
import finalproject.productivityup.ui.MainActivity;
import finalproject.productivityup.ui.UltradianRhythmTimerCard;

public class TimerAppWidgetProvider extends AppWidgetProvider {

    private final static String ULTRADIAN_RHYTHM_START_TIME_KEY = UltradianRhythmTimerCard.ULTRADIAN_RHYTHM_START_TIME_KEY;
    private final static String ULTRADIAN_RHYTHM_WORK_REST_KEY = UltradianRhythmTimerCard.ULTRADIAN_RHYTHM_WORK_REST_KEY;

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int N = appWidgetIds.length;

        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int appWidgetId : appWidgetIds) {
            // Create an Intent to launch ExampleActivity
            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            // Get the layout for the App Widget and attach an on-click listener
            // to the button

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.timer_appwidget);
            views.setOnClickPendingIntent(R.id.layout_container, pendingIntent);


            // Tell the AppWidgetManager to perform an update on the current app timer_appwidget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
}