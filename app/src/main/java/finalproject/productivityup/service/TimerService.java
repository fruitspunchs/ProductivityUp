package finalproject.productivityup.service;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.RemoteViews;

import java.io.IOException;
import java.io.Serializable;

import finalproject.productivityup.R;
import finalproject.productivityup.libs.Utility;
import finalproject.productivityup.ui.MainActivity;
import finalproject.productivityup.ui.PomodoroTimerCard;
import finalproject.productivityup.ui.UltradianRhythmTimerCard;
import finalproject.productivityup.widget.TimerAppWidgetProvider;

/**
 * Runs timer and updates widgets.
 */

public class TimerService extends Service {
    public final static String ACTION_START_SERVICE = "ACTION_START_SERVICE";
    public final static String ACTION_ON_UPDATE = "ACTION_ON_UPDATE";
    public final static String ACTION_START_PAUSE_TIMER = "ACTION_START_PAUSE_TIMER";
    public final static String ACTION_WORK_REST_TIMER = "ACTION_WORK_REST_TIMER";
    public static final String ACTION_REQUEST_POMODORO_STATE = "ACTION_REQUEST_POMODORO_STATE";
    public final static String APP_WIDGET_IDS_KEY = "APP_WIDGET_IDS_KEY";

    public static final String POMODORO_EVENT = "POMODORO_EVENT";
    public static final String POMODORO_EVENT_KEY = "POMODORO_EVENT_KEY";
    public static final String POMODORO_EVENT_TIME_LEFT = "POMODORO_EVENT_TIME_LEFT";
    public static final String POMODORO_EVENT_TIME_LEFT_KEY = "POMODORO_EVENT_TIME_LEFT_KEY";
    public static final String POMODORO_EVENT_BUTTON_STATE = "POMODORO_EVENT_BUTTON_STATE";
    public static final String POMODORO_EVENT_BUTTON_STATE_KEY = "POMODORO_EVENT_BUTTON_STATE_KEY";

    public static final String ULTRADIAN_EVENT = "ULTRADIAN_EVENT";
    public static final String ULTRADIAN_EVENT_KEY = "ULTRADIAN_EVENT_KEY";
    public static final String ULTRADIAN_EVENT_TIME_LEFT = "ULTRADIAN_EVENT_TIME_LEFT";
    public static final String ULTRADIAN_EVENT_TIME_LEFT_KEY = "ULTRADIAN_EVENT_TIME_LEFT_KEY";
    public static final String ULTRADIAN_EVENT_BUTTON_STATE = "ULTRADIAN_EVENT_BUTTON_STATE";
    public static final String ULTRADIAN_EVENT_BUTTON_STATE_KEY = "ULTRADIAN_EVENT_BUTTON_STATE_KEY";

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
    private long mPomodoroTimeLeft;
    private SharedPreferences mSharedPreferences;

    private MediaPlayer mMediaPlayer;
    private ServiceHandler mServiceHandler;

    private long mMinutesLeft = 0;
    private RemoteViews mRemoteViews;

    @Override
    public void onCreate() {
        super.onCreate();
        HandlerThread thread = new HandlerThread("TimerThread");
        thread.start();

        Looper serviceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(serviceLooper);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mAppWidgetIds = AppWidgetManager.getInstance(this).getAppWidgetIds(new ComponentName(getPackageName(), TimerAppWidgetProvider.class.getName()));

        mRemoteViews = new RemoteViews(getPackageName(), R.layout.timer_appwidget);
        Intent startActivityIntent = new Intent(this, MainActivity.class);
        startActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent startActivityPendingIntent = PendingIntent.getActivity(this, 0, startActivityIntent, 0);

        Intent startPauseTimerIntent = new Intent(this, TimerService.class);
        startPauseTimerIntent.setAction(TimerService.ACTION_START_PAUSE_TIMER);
        PendingIntent startPauseTimerPendingIntent = PendingIntent.getService(this, 1, startPauseTimerIntent, 0);

        mRemoteViews.setOnClickPendingIntent(R.id.layout_container, startActivityPendingIntent);
        mRemoteViews.setOnClickPendingIntent(R.id.start_pause_button, startPauseTimerPendingIntent);

        Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        mMediaPlayer = new MediaPlayer();
        try {
            mMediaPlayer.setDataSource(this, alert);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mMediaPlayer.setLooping(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null) {
            if (intent.getAction() != null) {
                sendMessageToServiceHandler(intent);
            }
        } else if (mAppWidgetIds.length > 0) {
            sendMessageToServiceHandler(new Intent(ACTION_START_SERVICE));
        }

        if (mAppWidgetIds.length > 0) {
            return START_STICKY;
        } else {
            return START_NOT_STICKY;
        }
    }

    @Override
    public void onDestroy() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void startUltradianRhythmTimer() {
        final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
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
            mRemoteViews.setImageViewResource(R.id.work_rest_button, R.drawable.ic_work_white_36dp);
            mRemoteViews.setContentDescription(R.id.work_rest_button, this.getString(R.string.cd_work_icon));
            broadcastUltradianMessage(ULTRADIAN_EVENT_BUTTON_STATE, ULTRADIAN_EVENT_BUTTON_STATE_KEY, WORK);
            timeLeft = WORK_DURATION - timeElapsed;
        } else if (mRhythmState == REST) {
            mRemoteViews.setImageViewResource(R.id.work_rest_button, R.drawable.ic_break_white_36dp);
            mRemoteViews.setContentDescription(R.id.work_rest_button, this.getString(R.string.cd_rest_icon));
            broadcastUltradianMessage(ULTRADIAN_EVENT_BUTTON_STATE, ULTRADIAN_EVENT_BUTTON_STATE_KEY, REST);
            timeLeft = REST_DURATION - timeElapsed;
        }

        if (sUltradianRhythmCountDownTimer != null) {
            sUltradianRhythmCountDownTimer.cancel();
        }

        sUltradianRhythmCountDownTimer = new CountDownTimer(timeLeft * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mMinutesLeft = millisUntilFinished / (1000 * 60);

                mRemoteViews.setTextViewText(R.id.ultradian_rhythm_timer_text_view, formatUltradianTimeString(mMinutesLeft));
                broadcastUltradianMessage(ULTRADIAN_EVENT_TIME_LEFT, ULTRADIAN_EVENT_TIME_LEFT_KEY, mMinutesLeft);

                for (int appWidgetId : mAppWidgetIds) {
                    appWidgetManager.partiallyUpdateAppWidget(appWidgetId, mRemoteViews);
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
            appWidgetManager.updateAppWidget(appWidgetId, mRemoteViews);
        }
    }

    private String formatUltradianTimeString(long minutesLeft) {
        String prefix = "";

        if (minutesLeft < 10) {
            prefix = "0";
        }

        return prefix + minutesLeft;
    }

    public void initializePomodoroTimer() {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);

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
            mPomodoroTimeLeft = mSharedPreferences.getLong(POMODORO_TIMER_TIME_LEFT_KEY, 0);
        } else {
            Log.d(LOG_TAG, "No preferences");
            startTime = currentTime;
            mStartPauseState = PAUSE;
            mPomodoroTimeLeft = TIMER_MAX_DURATION;
        }

        String minutesString = "";
        int buttonState = START;

        if (currentTime - startTime >= mPomodoroTimeLeft && mStartPauseState == START) {
            Log.d(LOG_TAG, "Loaded timer over");
            mPomodoroTimeLeft = 0;
            mStartPauseState = PAUSE;

            mRemoteViews.setImageViewResource(R.id.start_pause_button, R.drawable.ic_start_arrow_white_24dp);
            mRemoteViews.setContentDescription(R.id.start_pause_button, this.getString(R.string.cd_start_button));
            buttonState = START;
            minutesString = this.getString(R.string.timer_zero);
        } else if (mStartPauseState == PAUSE) {
            Log.d(LOG_TAG, "Loaded timer paused");
            mRemoteViews.setImageViewResource(R.id.start_pause_button, R.drawable.ic_start_arrow_white_24dp);
            mRemoteViews.setContentDescription(R.id.start_pause_button, this.getString(R.string.cd_start_button));
            buttonState = START;
            minutesString = Utility.formatPomodoroTimerString(mPomodoroTimeLeft);
        } else if (mStartPauseState == START) {
            Log.d(LOG_TAG, "Loaded timer started");
            mPomodoroTimeLeft = mPomodoroTimeLeft - (currentTime - startTime);
            Log.d(LOG_TAG, "Time left: " + mPomodoroTimeLeft);
            mRemoteViews.setImageViewResource(R.id.start_pause_button, R.drawable.ic_pause_white_24dp);
            mRemoteViews.setContentDescription(R.id.start_pause_button, this.getString(R.string.cd_pause_button));
            buttonState = PAUSE;
            minutesString = Utility.formatPomodoroTimerString(mPomodoroTimeLeft);
            startPomodoroTimer();
        } else if (mStartPauseState == STOP) {
            Log.d(LOG_TAG, "Loaded timer stopped");
            mRemoteViews.setImageViewResource(R.id.start_pause_button, R.drawable.ic_stop_white_24dp);
            mRemoteViews.setContentDescription(R.id.start_pause_button, this.getString(R.string.cd_stop_button));
            buttonState = STOP;
            mPomodoroTimeLeft = 0;
            minutesString = this.getString(R.string.timer_zero);
        }

        broadcastPomodoroMessage(POMODORO_EVENT_TIME_LEFT, POMODORO_EVENT_TIME_LEFT_KEY, mPomodoroTimeLeft);
        broadcastPomodoroMessage(POMODORO_EVENT_BUTTON_STATE, POMODORO_EVENT_BUTTON_STATE_KEY, buttonState);

        mRemoteViews.setTextViewText(R.id.pomodoro_timer_text_view, minutesString);


        for (int appWidgetId : mAppWidgetIds) {
            appWidgetManager.updateAppWidget(appWidgetId, mRemoteViews);
        }
    }

    private void startPomodoroTimer() {
        final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);

        if (sPomodoroCountDownTimer != null) {
            sPomodoroCountDownTimer.cancel();
        }

        sPomodoroCountDownTimer = new CountDownTimer(mPomodoroTimeLeft * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mPomodoroTimeLeft = millisUntilFinished / 1000;
                mRemoteViews.setTextViewText(R.id.pomodoro_timer_text_view, Utility.formatPomodoroTimerString(mPomodoroTimeLeft));

                broadcastPomodoroMessage(POMODORO_EVENT_TIME_LEFT, POMODORO_EVENT_TIME_LEFT_KEY, mPomodoroTimeLeft);

                for (int appWidgetId : mAppWidgetIds) {
                    appWidgetManager.updateAppWidget(appWidgetId, mRemoteViews);
                }
            }

            @Override
            public void onFinish() {
                mPomodoroTimeLeft = 0;
                mStartPauseState = STOP;

                mRemoteViews.setTextViewText(R.id.pomodoro_timer_text_view, Utility.formatPomodoroTimerString(mPomodoroTimeLeft));

                mRemoteViews.setImageViewResource(R.id.start_pause_button, R.drawable.ic_stop_white_24dp);
                mRemoteViews.setContentDescription(R.id.start_pause_button, TimerService.this.getString(R.string.cd_stop_button));

                broadcastPomodoroMessage(POMODORO_EVENT_TIME_LEFT, POMODORO_EVENT_TIME_LEFT_KEY, mPomodoroTimeLeft);
                broadcastPomodoroMessage(POMODORO_EVENT_BUTTON_STATE, POMODORO_EVENT_BUTTON_STATE_KEY, STOP);

                for (int appWidgetId : mAppWidgetIds) {
                    appWidgetManager.updateAppWidget(appWidgetId, mRemoteViews);
                }

                mSharedPreferences.edit()
                        .putInt(POMODORO_TIMER_START_PAUSE_KEY, mStartPauseState)
                        .putLong(POMODORO_TIMER_TIME_LEFT_KEY, mPomodoroTimeLeft)
                        .apply();

                startAlarm();
            }
        }.start();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);

        mRemoteViews.setTextViewText(R.id.ultradian_rhythm_timer_text_view, formatUltradianTimeString(mMinutesLeft));
        mRemoteViews.setTextViewText(R.id.pomodoro_timer_text_view, Utility.formatPomodoroTimerString(mPomodoroTimeLeft));

        switch (mStartPauseState) {
            case PAUSE:
                mRemoteViews.setImageViewResource(R.id.start_pause_button, R.drawable.ic_start_arrow_white_24dp);
                mRemoteViews.setContentDescription(R.id.start_pause_button, this.getString(R.string.cd_start_button));
                break;
            case START:
                mRemoteViews.setImageViewResource(R.id.start_pause_button, R.drawable.ic_pause_white_24dp);
                mRemoteViews.setContentDescription(R.id.start_pause_button, this.getString(R.string.cd_pause_button));
                break;
            case STOP:
                mRemoteViews.setImageViewResource(R.id.start_pause_button, R.drawable.ic_stop_white_24dp);
                mRemoteViews.setContentDescription(R.id.start_pause_button, this.getString(R.string.cd_stop_button));
                break;
        }

        if (mRhythmState == WORK) {
            mRemoteViews.setImageViewResource(R.id.work_rest_button, R.drawable.ic_work_white_36dp);
            mRemoteViews.setContentDescription(R.id.work_rest_button, this.getString(R.string.cd_work_icon));
        } else if (mRhythmState == REST) {
            mRemoteViews.setImageViewResource(R.id.work_rest_button, R.drawable.ic_break_white_36dp);
            mRemoteViews.setContentDescription(R.id.work_rest_button, this.getString(R.string.cd_rest_icon));
        }

        for (int appWidgetId : mAppWidgetIds) {
            appWidgetManager.updateAppWidget(appWidgetId, mRemoteViews);
        }
    }


    private void startAlarm() {
        try {
            mMediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            mMediaPlayer.start();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    private void stopAlarm() {
        try {
            mMediaPlayer.stop();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    private void onStartPauseButtonClick() {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        Log.d(LOG_TAG, "Start/Pause timer");

        if (sPomodoroCountDownTimer != null) {
            sPomodoroCountDownTimer.cancel();
        }

        try {
            if (mMediaPlayer.isPlaying()) {
                stopAlarm();
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }

        switch (mStartPauseState) {
            case START:
                mStartPauseState = PAUSE;
                broadcastPomodoroMessage(POMODORO_EVENT_BUTTON_STATE, POMODORO_EVENT_BUTTON_STATE_KEY, START);
                mRemoteViews.setImageViewResource(R.id.start_pause_button, R.drawable.ic_start_arrow_white_24dp);
                mRemoteViews.setContentDescription(R.id.start_pause_button, this.getString(R.string.cd_start_button));

                break;
            case PAUSE:
                mStartPauseState = START;

                if (mPomodoroTimeLeft == 0) {
                    mPomodoroTimeLeft = TIMER_MAX_DURATION;
                }

                broadcastPomodoroMessage(POMODORO_EVENT_BUTTON_STATE, POMODORO_EVENT_BUTTON_STATE_KEY, PAUSE);
                mRemoteViews.setImageViewResource(R.id.start_pause_button, R.drawable.ic_pause_white_24dp);
                mRemoteViews.setContentDescription(R.id.start_pause_button, this.getString(R.string.cd_pause_button));
                startPomodoroTimer();
                break;
            case STOP:
                mStartPauseState = PAUSE;
                broadcastPomodoroMessage(POMODORO_EVENT_BUTTON_STATE, POMODORO_EVENT_BUTTON_STATE_KEY, START);
                mRemoteViews.setImageViewResource(R.id.start_pause_button, R.drawable.ic_start_arrow_white_24dp);
                mRemoteViews.setContentDescription(R.id.start_pause_button, this.getString(R.string.cd_start_button));

                mPomodoroTimeLeft = TIMER_MAX_DURATION;

                broadcastPomodoroMessage(POMODORO_EVENT_TIME_LEFT, POMODORO_EVENT_TIME_LEFT_KEY, mPomodoroTimeLeft);
                String minutesString = Utility.formatPomodoroTimerString(mPomodoroTimeLeft);
                mRemoteViews.setTextViewText(R.id.pomodoro_timer_text_view, minutesString);

                break;

        }

        for (int appWidgetId : mAppWidgetIds) {
            appWidgetManager.updateAppWidget(appWidgetId, mRemoteViews);
        }

        mSharedPreferences.edit()
                .putLong(POMODORO_TIMER_START_TIME_KEY, Utility.getCurrentTimeInSeconds())
                .putInt(POMODORO_TIMER_START_PAUSE_KEY, mStartPauseState)
                .putLong(POMODORO_TIMER_TIME_LEFT_KEY, mPomodoroTimeLeft)
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


    private <T extends Serializable> void broadcastPomodoroMessage(String message, String key, T value) {
        if (!message.equals(POMODORO_EVENT_TIME_LEFT)) {
            Log.d(LOG_TAG, "Broadcasting message: " + message);
        }

        Intent intent = new Intent(POMODORO_EVENT);

        intent.putExtra(POMODORO_EVENT_KEY, message);
        intent.putExtra(key, value);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private <T extends Serializable> void broadcastUltradianMessage(String message, String key, T value) {
        if (!message.equals(ULTRADIAN_EVENT_TIME_LEFT)) {
            Log.d(LOG_TAG, "Broadcasting message: " + message);
        }

        Intent intent = new Intent(ULTRADIAN_EVENT);

        intent.putExtra(ULTRADIAN_EVENT_KEY, message);
        intent.putExtra(key, value);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void sendMessageToServiceHandler(Intent intent) {
        Message msg = mServiceHandler.obtainMessage();
        msg.obj = intent;
        mServiceHandler.sendMessage(msg);
    }

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            synchronized (this) {
                Intent intent = (Intent) msg.obj;
                String action = intent.getAction();

                switch (action) {
                    case ACTION_START_SERVICE:
                        startUltradianRhythmTimer();
                        initializePomodoroTimer();
                        break;
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
                    case ACTION_REQUEST_POMODORO_STATE:
                        switch (mStartPauseState) {
                            case START:
                                broadcastPomodoroMessage(POMODORO_EVENT_BUTTON_STATE, POMODORO_EVENT_BUTTON_STATE_KEY, PAUSE);
                                break;
                            case PAUSE:
                                broadcastPomodoroMessage(POMODORO_EVENT_BUTTON_STATE, POMODORO_EVENT_BUTTON_STATE_KEY, START);
                                break;
                            case STOP:
                                broadcastPomodoroMessage(POMODORO_EVENT_BUTTON_STATE, POMODORO_EVENT_BUTTON_STATE_KEY, STOP);
                        }
                        broadcastPomodoroMessage(POMODORO_EVENT_TIME_LEFT, POMODORO_EVENT_TIME_LEFT_KEY, mPomodoroTimeLeft);
                        break;
                }
            }
        }
    }

}
