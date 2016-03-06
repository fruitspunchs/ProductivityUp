package finalproject.productivityup.service;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.widget.RemoteViews;

import finalproject.productivityup.R;
import finalproject.productivityup.libs.Utility;
import finalproject.productivityup.ui.UltradianRhythmTimerCard;

/**
 * Created by User on 1/11/2016.
 */
public class TimerService extends Service {
    public final static String ACTION_ON_UPDATE = "ACTION_ON_UPDATE";
    public final static String APP_WIDGET_IDS_KEY = "APP_WIDGET_IDS_KEY";

    private final static String ULTRADIAN_RHYTHM_START_TIME_KEY = UltradianRhythmTimerCard.ULTRADIAN_RHYTHM_START_TIME_KEY;
    private final static String ULTRADIAN_RHYTHM_WORK_REST_KEY = UltradianRhythmTimerCard.ULTRADIAN_RHYTHM_WORK_REST_KEY;
    private final static int WORK = UltradianRhythmTimerCard.WORK;
    private final static int REST = UltradianRhythmTimerCard.REST;
    private final static int WORK_DURATION = UltradianRhythmTimerCard.WORK_DURATION;
    private final static int REST_DURATION = UltradianRhythmTimerCard.REST_DURATION;
    private int mRhythmState;
    private CountDownTimer mCountDownTimer;
    private int[] mAppWidgetIds;

    private AppWidgetManager mAppWidgetManager;

    @Override
    public void onCreate() {
        super.onCreate();
        mAppWidgetManager = AppWidgetManager.getInstance(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        RemoteViews views = new RemoteViews(getPackageName(), R.layout.timer_appwidget);

        if (intent != null) {
            if (intent.getAction() != null) {
                switch (intent.getAction()) {
                    case ACTION_ON_UPDATE:
                        mAppWidgetIds = intent.getIntArrayExtra(APP_WIDGET_IDS_KEY);
                        startUltradianRhythmTimer(this, views);
                        break;
                }
            }
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void startUltradianRhythmTimer(final Context context, final RemoteViews views) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        long startTime;

        if (prefs.contains(ULTRADIAN_RHYTHM_START_TIME_KEY)) {
            startTime = prefs.getLong(ULTRADIAN_RHYTHM_START_TIME_KEY, Utility.getCurrentTimeInSeconds());
            mRhythmState = prefs.getInt(ULTRADIAN_RHYTHM_WORK_REST_KEY, WORK);
        } else {
            startTime = Utility.getCurrentTimeInSeconds();
            mRhythmState = WORK;
            prefs.edit()
                    .putLong(ULTRADIAN_RHYTHM_START_TIME_KEY, Utility.getCurrentTimeInSeconds())
                    .putInt(ULTRADIAN_RHYTHM_WORK_REST_KEY, mRhythmState)
                    .apply();
        }

        long timeElapsed = Utility.getCurrentTimeInSeconds() - startTime;

        if (mRhythmState == REST) {
            if (timeElapsed >= REST_DURATION) {
                timeElapsed -= REST_DURATION;
                mRhythmState = WORK;
            }
        }

        if (mRhythmState == WORK) {
            while (timeElapsed >= WORK_DURATION) {
                timeElapsed -= WORK_DURATION;
                mRhythmState = REST;

                if (timeElapsed >= REST_DURATION) {
                    timeElapsed -= REST_DURATION;
                    mRhythmState = WORK;
                } else {
                    break;
                }
            }
        }

        long timeLeft = 0;
        if (mRhythmState == WORK) {
            views.setImageViewResource(R.id.work_rest_button, R.drawable.ic_work_white_36dp);
            timeLeft = WORK_DURATION - timeElapsed;
        } else if (mRhythmState == REST) {
            views.setImageViewResource(R.id.work_rest_button, R.drawable.ic_break_white_36dp);
            timeLeft = REST_DURATION - timeElapsed;
        }

        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }

        mCountDownTimer = new CountDownTimer(timeLeft * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long minutesLeft = millisUntilFinished / (1000 * 60);

                String prefix = "";

                if (minutesLeft < 10) {
                    prefix = "0";
                }

                String minutesString = prefix + minutesLeft;


                views.setTextViewText(R.id.ultradian_rhythm_timer_text_view, minutesString);

                for (int appWidgetId : mAppWidgetIds) {
                    mAppWidgetManager.updateAppWidget(appWidgetId, views);
                }
            }

            @Override
            public void onFinish() {
                if (mRhythmState == WORK) {
                    mRhythmState = REST;
                } else if (mRhythmState == REST) {
                    mRhythmState = WORK;
                }
                startUltradianRhythmTimer(context, views);
            }
        }.start();

        for (int appWidgetId : mAppWidgetIds) {
            mAppWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
}
