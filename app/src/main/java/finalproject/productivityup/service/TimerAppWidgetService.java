package finalproject.productivityup.service;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.RemoteViews;

import finalproject.productivityup.R;
import finalproject.productivityup.libs.Utility;
import finalproject.productivityup.ui.PomodoroTimerCard;
import finalproject.productivityup.ui.UltradianRhythmTimerCard;

/**
 * Created by User on 1/11/2016.
 */

// TODO: 3/7/2016  fix widget sometimes not transitioning to work/rest

public class TimerAppWidgetService extends Service {
    public final static String ACTION_ON_UPDATE = "ACTION_ON_UPDATE";
    public final static String ACTION_START_PAUSE_TIMER = "ACTION_START_PAUSE_TIMER";
    public final static String ACTION_WORK_REST_TIMER = "ACTION_WORK_REST_TIMER";
    public final static String APP_WIDGET_IDS_KEY = "APP_WIDGET_IDS_KEY";
    private final static String ULTRADIAN_RHYTHM_START_TIME_KEY = UltradianRhythmTimerCard.ULTRADIAN_RHYTHM_START_TIME_KEY;
    private final static String ULTRADIAN_RHYTHM_WORK_REST_KEY = UltradianRhythmTimerCard.ULTRADIAN_RHYTHM_WORK_REST_KEY;
    private final static int WORK = UltradianRhythmTimerCard.WORK;
    private final static int REST = UltradianRhythmTimerCard.REST;
    private final static int WORK_DURATION = UltradianRhythmTimerCard.WORK_DURATION;
    private final static int REST_DURATION = UltradianRhythmTimerCard.REST_DURATION;
    private final static String POMODORO_TIMER_START_TIME_KEY = PomodoroTimerCard.POMODORO_TIMER_START_TIME_KEY;
    private final static String POMODORO_TIMER_START_PAUSE_KEY = PomodoroTimerCard.POMODORO_TIMER_START_PAUSE_KEY;
    private final static String POMODORO_TIMER_TIME_LEFT_KEY = PomodoroTimerCard.POMODORO_TIMER_TIME_LEFT_KEY;
    private final static int TIMER_MAX_DURATION = PomodoroTimerCard.TIMER_MAX_DURATION;
    private final static int START = PomodoroTimerCard.START;
    private final static int PAUSE = PomodoroTimerCard.PAUSE;
    private final static int STOP = PomodoroTimerCard.STOP;
    private static CountDownTimer sUltradianRhythmCountDownTimer;
    private static CountDownTimer sPomodoroCountDownTimer;
    private final String LOG_TAG = getClass().getSimpleName();
    private int mRhythmState;
    private int[] mAppWidgetIds;
    private int mStartPauseState;
    private long mTimeLeft;
    private SharedPreferences mSharedPreferences;

    private AppWidgetManager mAppWidgetManager;

    private String mTimerZeroString;

    @Override
    public void onCreate() {
        super.onCreate();
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mAppWidgetManager = AppWidgetManager.getInstance(this);
        mTimerZeroString = this.getString(R.string.timer_zero);
        mAppWidgetIds = new int[]{};
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null) {
            if (intent.getAction() != null) {
                switch (intent.getAction()) {
                    case ACTION_ON_UPDATE:
                        mAppWidgetIds = intent.getIntArrayExtra(APP_WIDGET_IDS_KEY);
                        startUltradianRhythmTimer();
                        initializePomodoroTimer();
                        break;
                    case ACTION_START_PAUSE_TIMER:
                        onStartPauseButtonClick();
                        break;
                    case ACTION_WORK_REST_TIMER:
                        onWorkRestButtonClick();
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

    public void startUltradianRhythmTimer() {

        final RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.timer_appwidget);

        long startTime;

        if (mSharedPreferences.contains(ULTRADIAN_RHYTHM_START_TIME_KEY)) {
            startTime = mSharedPreferences.getLong(ULTRADIAN_RHYTHM_START_TIME_KEY, Utility.getCurrentTimeInSeconds());
            mRhythmState = mSharedPreferences.getInt(ULTRADIAN_RHYTHM_WORK_REST_KEY, WORK);
        } else {
            startTime = Utility.getCurrentTimeInSeconds();
            mRhythmState = WORK;
            mSharedPreferences.edit()
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
            remoteViews.setImageViewResource(R.id.work_rest_button, R.drawable.ic_work_white_36dp);
            timeLeft = WORK_DURATION - timeElapsed;
        } else if (mRhythmState == REST) {
            remoteViews.setImageViewResource(R.id.work_rest_button, R.drawable.ic_break_white_36dp);
            timeLeft = REST_DURATION - timeElapsed;
        }

        if (sUltradianRhythmCountDownTimer != null) {
            sUltradianRhythmCountDownTimer.cancel();
        }

        sUltradianRhythmCountDownTimer = new CountDownTimer(timeLeft * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long minutesLeft = millisUntilFinished / (1000 * 60);

                String prefix = "";

                if (minutesLeft < 10) {
                    prefix = "0";
                }

                String minutesString = prefix + minutesLeft;


                remoteViews.setTextViewText(R.id.ultradian_rhythm_timer_text_view, minutesString);

                for (int appWidgetId : mAppWidgetIds) {
                    mAppWidgetManager.updateAppWidget(appWidgetId, remoteViews);
                }
            }

            @Override
            public void onFinish() {
                if (mRhythmState == WORK) {
                    mRhythmState = REST;
                } else if (mRhythmState == REST) {
                    mRhythmState = WORK;
                }
                startUltradianRhythmTimer();
            }
        }.start();

        for (int appWidgetId : mAppWidgetIds) {
            mAppWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        }
    }

    public void initializePomodoroTimer() {
        final RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.timer_appwidget);

        if (sPomodoroCountDownTimer != null) {
            sPomodoroCountDownTimer.cancel();
        }

        Log.d(LOG_TAG, "Pomodoro timer initialized");

        long startTime;
        long currentTime = Utility.getCurrentTimeInSeconds();
        if (mSharedPreferences.contains(POMODORO_TIMER_START_TIME_KEY)) {
            Log.d(LOG_TAG, "Found preferences");
            startTime = mSharedPreferences.getLong(POMODORO_TIMER_START_TIME_KEY, currentTime);
            mStartPauseState = mSharedPreferences.getInt(POMODORO_TIMER_START_PAUSE_KEY, START);
            mTimeLeft = mSharedPreferences.getLong(POMODORO_TIMER_TIME_LEFT_KEY, 0);
        } else {
            Log.d(LOG_TAG, "No preferences");
            startTime = currentTime;
            mStartPauseState = PAUSE;
            mTimeLeft = TIMER_MAX_DURATION;
        }

        if (currentTime - startTime >= mTimeLeft && mStartPauseState == START) {
            Log.d(LOG_TAG, "Loaded timer over");
            mTimeLeft = 0;
            mStartPauseState = PAUSE;

            remoteViews.setImageViewResource(R.id.start_pause_button, R.drawable.ic_play_arrow_white_24dp);
            String minutesString = this.getString(R.string.timer_zero);
            remoteViews.setTextViewText(R.id.pomodoro_timer_text_view, minutesString);
        } else if (mStartPauseState == PAUSE) {
            Log.d(LOG_TAG, "Loaded timer paused");
            remoteViews.setImageViewResource(R.id.start_pause_button, R.drawable.ic_play_arrow_white_24dp);
            String minutesString = Utility.formatPomodoroTimer(mTimeLeft);
            remoteViews.setTextViewText(R.id.pomodoro_timer_text_view, minutesString);
        } else if (mStartPauseState == START) {
            Log.d(LOG_TAG, "Loaded timer started");
            mTimeLeft = mTimeLeft - (currentTime - startTime);
            Log.d(LOG_TAG, "Time left: " + mTimeLeft);
            remoteViews.setImageViewResource(R.id.start_pause_button, R.drawable.ic_pause_white_24dp);
            String minutesString = Utility.formatPomodoroTimer(mTimeLeft);
            remoteViews.setTextViewText(R.id.pomodoro_timer_text_view, minutesString);
            startPomodoroTimer();
        } else if (mStartPauseState == STOP) {
            Log.d(LOG_TAG, "Loaded timer stopped");
            remoteViews.setImageViewResource(R.id.start_pause_button, R.drawable.ic_stop_white_24dp);
            mTimeLeft = 0;
            String minutesString = this.getString(R.string.timer_zero);
            remoteViews.setTextViewText(R.id.pomodoro_timer_text_view, minutesString);
        }

        for (int appWidgetId : mAppWidgetIds) {
            mAppWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        }
    }

    private void startPomodoroTimer() {
        final RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.timer_appwidget);

        if (sPomodoroCountDownTimer != null) {
            sPomodoroCountDownTimer.cancel();
        }

        sPomodoroCountDownTimer = new CountDownTimer(mTimeLeft * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                remoteViews.setTextViewText(R.id.pomodoro_timer_text_view, Utility.formatPomodoroTimer(millisUntilFinished / 1000));
                mTimeLeft = millisUntilFinished / 1000;

                for (int appWidgetId : mAppWidgetIds) {
                    mAppWidgetManager.updateAppWidget(appWidgetId, remoteViews);
                }
            }

            @Override
            public void onFinish() {
                remoteViews.setTextViewText(R.id.pomodoro_timer_text_view, mTimerZeroString);

                remoteViews.setImageViewResource(R.id.start_pause_button, R.drawable.ic_stop_white_24dp);
                mTimeLeft = 0;
                mStartPauseState = STOP;

                for (int appWidgetId : mAppWidgetIds) {
                    mAppWidgetManager.updateAppWidget(appWidgetId, remoteViews);
                }

                startAlarm();
            }
        }.start();
    }

    private void startAlarm() {
        Intent intent = new Intent(this, AlarmPlayerService.class);
        intent.setAction(AlarmPlayerService.ALARM_PLAYER_START);
        this.startService(intent);
    }

    private void stopAlarm() {
        Intent intent = new Intent(this, AlarmPlayerService.class);
        intent.setAction(AlarmPlayerService.ALARM_PLAYER_STOP);
        this.startService(intent);
    }

    private void onStartPauseButtonClick() {
        final RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.timer_appwidget);
        Log.d(LOG_TAG, "Start/Pause timer");

        if (sPomodoroCountDownTimer != null) {
            sPomodoroCountDownTimer.cancel();
        }

        switch (mStartPauseState) {
            case START:
                mStartPauseState = PAUSE;
                remoteViews.setImageViewResource(R.id.start_pause_button, R.drawable.ic_play_arrow_white_24dp);

                break;
            case PAUSE:
                mStartPauseState = START;

                if (mTimeLeft == 0) {
                    mTimeLeft = TIMER_MAX_DURATION;
                }

                remoteViews.setImageViewResource(R.id.start_pause_button, R.drawable.ic_pause_white_24dp);
                startPomodoroTimer();
                break;
            case STOP:
                stopAlarm();

                mStartPauseState = PAUSE;
                remoteViews.setImageViewResource(R.id.start_pause_button, R.drawable.ic_play_arrow_white_24dp);

                mTimeLeft = TIMER_MAX_DURATION;

                String minutesString = Utility.formatPomodoroTimer(mTimeLeft);
                remoteViews.setTextViewText(R.id.pomodoro_timer_text_view, minutesString);

                break;

        }

        for (int appWidgetId : mAppWidgetIds) {
            mAppWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        }

        mSharedPreferences.edit()
                .putLong(POMODORO_TIMER_START_TIME_KEY, Utility.getCurrentTimeInSeconds())
                .putInt(POMODORO_TIMER_START_PAUSE_KEY, mStartPauseState)
                .putLong(POMODORO_TIMER_TIME_LEFT_KEY, mTimeLeft)
                .apply();
    }

    public void onWorkRestButtonClick() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        if (sUltradianRhythmCountDownTimer != null) {
            sUltradianRhythmCountDownTimer.cancel();
        }

        if (mRhythmState == WORK) {
            mRhythmState = REST;
        } else if (mRhythmState == REST) {
            mRhythmState = WORK;
        }

        prefs.edit()
                .putLong(ULTRADIAN_RHYTHM_START_TIME_KEY, Utility.getCurrentTimeInSeconds())
                .putInt(ULTRADIAN_RHYTHM_WORK_REST_KEY, mRhythmState)
                .apply();

        startUltradianRhythmTimer();
    }
}
