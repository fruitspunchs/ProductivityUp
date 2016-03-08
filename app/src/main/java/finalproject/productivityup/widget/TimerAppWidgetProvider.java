package finalproject.productivityup.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import finalproject.productivityup.R;
import finalproject.productivityup.service.TimerService;
import finalproject.productivityup.ui.MainActivity;

public class TimerAppWidgetProvider extends AppWidgetProvider {

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Intent serviceIntent = new Intent(context, TimerService.class);
        serviceIntent.setAction(TimerService.ACTION_ON_UPDATE);
        serviceIntent.putExtra(TimerService.APP_WIDGET_IDS_KEY, appWidgetIds);
        context.startService(serviceIntent);

        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int appWidgetId : appWidgetIds) {
            // Create an Intent to launch ExampleActivity
            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            // Get the layout for the App Widget and attach an on-click listener
            // to the button

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.timer_appwidget);
            views.setOnClickPendingIntent(R.id.layout_container, pendingIntent);

            Intent startPauseIntent = new Intent(context, TimerService.class);
            startPauseIntent.setAction(TimerService.ACTION_START_PAUSE_TIMER);
            PendingIntent startPausePendingIntent = PendingIntent.getService(context, 1, startPauseIntent, 0);
            views.setOnClickPendingIntent(R.id.start_pause_button, startPausePendingIntent);

            // Tell the AppWidgetManager to perform an update on the current app timer_appwidget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
}